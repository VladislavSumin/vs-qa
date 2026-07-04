# План: перенос (transfer) экранов между инстансами навигации с сохранением состояния

## 1. Контекст и цель

Сейчас таб логов можно **открыть в новом окне** (shift+click по элементу в списке недавних),
но это создаёт **новый** экран с нуля. Задача — уметь **«откреплять» уже открытый таб**
в новое окно так, чтобы:

- состояние экрана НЕ сбрасывалось;
- `viewModel` НЕ пересоздавалась;
- в идеале сохранялось всё поддерево компонента (вложенные компоненты и их viewModel).

Ограничение: навигация vs-core и навигация decompose «из коробки» не умеют переносить
элементы между разными инстансами навигации. Мы **можем дорабатывать vs-core**, но **не decompose**.

Принятые решения (по итогам обсуждения):

1. Сохраняем **всё поддерево** компонента, а не только верхнюю viewModel.
2. Инстанс передаётся **«из рук в руки»**: внутренний `close(keepInstance=true)` возвращает
   удержанный инстанс, внутренний `open(savedInstance=…)` его усыновляет. Порядок — `close → open`.
3. Корневой API навигации называется **`transfer`** (а не `detach`) и является **общим**:
   перенос технически возможен из любой ноды графа в любую (окно — это лишь app-level понятие,
   выражаемое хинтом `WindowScreenParams`).
4. Реализуем **универсально**: любой экран переносим «из коробки», без маркер-интерфейса.
5. Поддерживаем во всех трёх типах host-навигации: **Pages, Stack, Slot**.
6. UI-триггер: **отдельная кнопка «открепить»** рядом с кнопкой закрытия таба.

> Репозитории: приложение — `vs-qa` (этот репозиторий), ядро — `vs-core`
> (клонируется в соседнюю папку `../vs-core`; часть изменений идёт туда).

---

## 2. Текущая архитектура (как есть)

### 2.1 Иерархия окон/табов

```
MultiWindowRootScreen         childNavigationPages  (host = MultiWindowNavigationHost)  ← одна «страница» на окно
 └─ WindowScreen              childNavigationSlot   (host = WindowNavigationHost)
     └─ RootScreen            childNavigationPages  (host = TabNavigationHost)          ← ТАБЫ
         ├─ HomeScreen (таб)
         ├─ DebugScreen (таб)
         └─ LogViewerScreen (таб, params = LogViewerScreenParams(logPath))
```

Единый граф навигации на всё приложение и **единый** `GlobalNavigator`
(создаётся один раз в `MultiWindowRootScreenComponent` через `context.childNavigationRoot(navigation)`).
Каждый экран получает собственный `ScreenNavigatorImpl`, но все они сходятся к одному корню и
одному `GlobalNavigator`.

Файлы:
- `vs-qa` `feature/multi-window/impl/.../ui/screen/multiWindowRoot/MultiWindowRootScreen.kt`
- `vs-qa` `feature/multi-window/impl/.../ui/screen/window/WindowScreen.kt`
- `vs-qa` `feature/root-screen/impl/.../ui/screen/root/RootScreen.kt`
- `vs-qa` `feature/log-viewer/impl/.../ui/screen/logViewer/LogViewerScreen.kt`
- `vs-qa` `feature/multi-window/impl/.../ui/component/multiWindowRootScreen/MultiWindowRootScreenComponent.kt`

### 2.2 Как хранится состояние (viewModel / InstanceKeeper)

`Component.viewModel { … }` (vs-core `core/decompose/components/.../Component.kt`) хранит
`ViewModelHolder` в `context.instanceKeeper.getOrCreate(key)`. Для экрана-таба `context` — это
decompose-child-контекст, созданный `childNavigationPages` (принадлежит навигации таба).

Ключевые связки в `Component.viewModel`:
- `context.instanceKeeper.getOrCreate(key)` — хранение viewModel;
- `context.stateKeeper.consume/register(key)` — сохранение состояния viewModel при смерти процесса;
- `context.lifecycle.subscribe(viewModelUiLifecycle)` — связывание lifecycle.

`ViewModel.onDestroy()` (vs-core `core/decompose/components/.../ViewModel.kt`) отменяет `viewModelScope`.

### 2.3 Навигация vs-core поверх decompose

- `ScreenNavigatorImpl<Ctx>` (`vs-core core/navigation/impl/.../navigator/ScreenNavigatorImpl.kt`) —
  навигатор уровня экрана. Хранит `parentNavigator`, `screenPath`, `node`, `globalNavigator`,
  карту дочерних `childScreenNavigators` (ключ — `screenParams`), карты `navigationHosts` и
  `customFactories`, а также `lateinit var screen`.
  - Регистрация в родителе: в `init` → `parentNavigator?.registerScreenNavigator(this, lifecycle)`.
  - Дерегистрация: внутри `registerScreenNavigator` через `lifecycle.doOnDestroy { childScreenNavigators.remove(...) }`.
  - `open/close` делегируют в `globalNavigator`, передавая `startScreenPath = screenPath`.
- `GlobalNavigator<Ctx>` (`.../navigator/GlobalNavigator.kt`) — резолвинг путей (`createOpenPath`),
  `open`, `close`, сериализация навигации через `UnsafeRelay` (навигация внутри навигации запрещена).
- `HostNavigator` (`.../navigator/HostNavigator.kt`) — интерфейс: `open(params, intent)`,
  `open(screenKey, defaultParams)`, `close(params): Boolean`, `close(screenKey): Boolean`.
- Реализации host-навигации:
  - `NavigationPages.kt` → `PagesHostNavigator`, создаёт `ConfigurationHolder` в трансформерах `PagesNavigation`.
  - `NavigationStack.kt` → `StackHostNavigator`.
  - `NavigationSlot.kt` → `SlotHostNavigator`.
- `ConfigurationHolder` (`.../host/ConfigurationHolder.kt`) — обёртка над `screenParams` + канал интентов;
  `equals/hashCode` только по `screenParams`.
- `childScreenFactory` (`.../host/ChildScreenFactory.kt`) — «фабрика» дочерних экранов для decompose:
  создаёт `ScreenNavigatorImpl` через `internalNavigator.createChildNavigator(...)`, ставит
  `ScreenNavigatorHolder`, вызывает `screenFactory.create(...)`.
- `GenericScreen<Ctx>` (`.../screen/Screen.kt`) — базовый экран; `internalNavigator` берётся из
  thread-local-подобного `ScreenNavigatorHolder` в момент конструирования.

### 2.4 Как сейчас работает «открыть в новом окне»

`vs-qa feature/home-screen/impl/.../HomeScreen.kt`:

```kotlin
onOpenLogRecent = { path, openInNewWindow ->
    if (openInNewWindow) {
        navigator.open(
            screenParams = LogViewerScreenParams(path),
            hints = listOf(WindowScreenParams(Random.nextLong().toString())),
        )
    } else {
        navigator.open(LogViewerScreenParams(path))
    }
}
```

Хинт `WindowScreenParams(random)` заставляет резолвер пройти через **новый** инстанс `WindowScreen`
→ создаётся новое окно, новый `RootScreen`, новый `LogViewerScreen` с нуля.

---

## 3. Корень проблемы

decompose владеет `InstanceKeeper` каждого таба. Поведение, которое мы **не можем** изменить:

- при удалении `ConfigurationHolder` из `PagesNavigation`/`StackNavigation`/`SlotNavigation`
  decompose синхронно вызывает `destroy()` у `InstanceKeeper` этого child → `ViewModelHolder.onDestroy()`
  → `viewModel.onDestroy()` → `viewModelScope.cancel()` (состояние теряется);
- при добавлении конфигурации в **другой** инстанс навигации (табы нового окна) decompose создаёт
  **свежий** child-контекст → новый компонент, новая viewModel.

Вывод: чтобы состояние пережило перенос, удерживаемое состояние (viewModel и весь подкомпонентный
subtree) должно жить в контексте, который **не является** per-child контекстом, которым владеет
навигация таба, и который можно **отвязать от одного родителя и привязать к другому**.

---

## 4. Проверенные факты API (essenty 2.5.0 / decompose 3.5.0)

Всё нужное есть в **публичном** API — форк decompose не требуется.

- `com.arkivanov.essenty.instancekeeper.InstanceKeeper`:
  - `fun get(key): Instance?`
  - `fun put(key, instance)` — бросает, если ключ занят;
  - **`fun remove(key): Instance?` — «This does not destroy the instance.»**
  - `interface Instance { fun onDestroy() }`
- `InstanceKeeperDispatcher : InstanceKeeper { fun destroy() }`, фабрика `InstanceKeeperDispatcher()`.
  `DefaultInstanceKeeperDispatcher`: `remove` просто убирает из map; `destroy` вызывает `onDestroy` у всех.
- `com.arkivanov.essenty.statekeeper.StateKeeper`:
  - `consume(key, strategy)`, `register(key, strategy, supplier)`, **`unregister(key)`**, `isRegistered(key)`.
  - `StateKeeperDispatcher(savedState?)`, `SerializableContainer`.
- `com.arkivanov.essenty.lifecycle.Lifecycle`:
  - `subscribe(Callbacks)` / **`unsubscribe(Callbacks)`**; состояния INITIALIZED→CREATED→STARTED→RESUMED и обратно, DESTROYED — терминальное.
  - `LifecycleRegistry : Lifecycle, Lifecycle.Callbacks` — управляется вручную; `LifecycleRegistry(initialState)`.
- `com.arkivanov.decompose.GenericComponentContext<T>` наследует `ComponentContextFactoryOwner<T>`
  → есть `val componentContextFactory: ComponentContextFactory<T>`.
- `ComponentContextFactory<T>.invoke(lifecycle, stateKeeper, instanceKeeper, backHandler): T`
  — позволяет собрать контекст типа `Ctx` из **наших** keeper'ов (все 4 параметра обязательны,
  значит holder'у нужен ещё и `BackHandler`, например `BackDispatcher()` из `essenty.backhandler`).

Итого механика «отвязать от родителя → перенести → привязать к новому родителю»:
`instanceKeeper.remove/put` + `stateKeeper.unregister/register` + `lifecycle.unsubscribe/subscribe`.

---

## 5. Целевая архитектура: ре-парентящийся управляемый контекст (universal)

### 5.1 Принцип

Каждый экран, создаваемый через `childScreenFactory`, строится **не** на decompose-child-контексте
таба, а на **самовладеющем контексте** (`holder`) с собственными `LifecycleRegistry`,
`InstanceKeeperDispatcher`, `StateKeeperDispatcher`, `BackHandler`. Тонкий decompose-child (далее
**mount**) в навигации таба служит лишь «точкой привязки»: он драйвит lifecycle holder'а и
обеспечивает удержание holder'а (retention) через свой `instanceKeeper`/`stateKeeper`.

В обычном режиме holder **привязан к mount'у**, поэтому ведёт себя как штатная часть иерархии
(смена конфигурации Android и смерть процесса работают как раньше — **без регресса**). При переносе
holder **отвязывается** от исходного mount'а и **привязывается** к новому.

Ключевое следствие: весь subtree компонента (вложенные компоненты, их контексты и viewModel) живёт
**внутри** контекста holder'а, поэтому перенос одной holder-обёртки переносит всё поддерево целиком.

### 5.2 `TransferableScreenHolder`

Новый класс (vs-core, `core/navigation/impl`, пакет `ru.vladislavsumin.core.navigation.transfer`).
Реализует `InstanceKeeper.Instance`, чтобы жить в `instanceKeeper` mount'а и получать `onDestroy`
только при **реальном** уничтожении (не при `remove`).

```kotlin
internal class TransferableScreenHolder<Ctx : GenericComponentContext<Ctx>>(
    val key: Any,                    // идентификатор в keeper'ах mount'а (обычно screenParams)
) : InstanceKeeper.Instance {

    val lifecycle = LifecycleRegistry()                 // собственный lifecycle
    val instanceKeeper = InstanceKeeperDispatcher()     // тут живут все viewModel subtree
    val stateKeeper = StateKeeperDispatcher(/* restored */)
    val backHandler = BackDispatcher()

    lateinit var screen: GenericScreen<Ctx>
    lateinit var navigator: ScreenNavigatorImpl<Ctx>

    // текущая привязка
    private var boundHostInstanceKeeper: InstanceKeeper? = null
    private var boundHostStateKeeper: StateKeeper? = null
    private var mirror: Lifecycle.Callbacks? = null
    private var boundHostLifecycle: Lifecycle? = null

    fun createContext(factory: ComponentContextFactory<Ctx>): Ctx =
        factory(lifecycle, stateKeeper, instanceKeeper, backHandler)

    fun bindTo(host: Ctx, stateKey: String) { /* см. 5.3 */ }
    fun unbind() { /* см. 5.5 (для transfer, без destroy) */ }

    override fun onDestroy() {          // вызовется при destroy() host-keeper'а (реальное удаление)
        lifecycle.destroy()             // → отмена всех scope subtree
        instanceKeeper.destroy()        // → onDestroy у всех viewModel
    }
}
```

### 5.3 Привязка к mount (retention без регресса)

`bindTo(host, stateKey)`:

1. **InstanceKeeper**: holder кладётся в `host.instanceKeeper`
   - при первом создании — через `getOrCreate(key) { holder }` (см. `childScreenFactory`);
   - при усыновлении — через `host.instanceKeeper.put(key, holder)`.
   На Android смена конфигурации сохраняет `host.instanceKeeper` → holder и его `instanceKeeper` (а значит и все viewModel) удержаны.
2. **StateKeeper**: `if (!host.stateKeeper.isRegistered(stateKey)) host.stateKeeper.register(stateKey, SerializableContainer.serializer()) { holder.stateKeeper.save() }`.
   Начальное состояние (смерть процесса) читается один раз при создании holder'а:
   `val saved = host.stateKeeper.consume(stateKey, SerializableContainer.serializer())` → `StateKeeperDispatcher(saved)`.
3. **Lifecycle**: создаём `Lifecycle.Callbacks`, который зеркалит состояния host-lifecycle в
   `holder.lifecycle`, и `host.lifecycle.subscribe(mirror)` (см. 5.4). Сохраняем ссылки для последующего `unbind`.

Замечание: `host` здесь — это `childScreenContext` (mount-контекст decompose), т.к. именно его
`instanceKeeper`/`stateKeeper` удерживаются decompose при смене конфигурации и сериализуются при
смерти процесса, и уничтожаются при удалении конфигурации.

### 5.4 Зеркалирование lifecycle и различение config-change vs removal

Проблема: `mount.lifecycle` уходит в DESTROYED и при смене конфигурации, и при реальном удалении.
Но `mount.instanceKeeper.destroy()` вызывается **только** при реальном удалении (при смене
конфигурации keeper удержан). Поэтому:

- **Зеркало lifecycle** транслирует `onCreate/onStart/onResume/onPause/onStop` один-в-один, а
  **`onDestroy` host-lifecycle трактует как `onStop`** (никогда не переводит `holder.lifecycle` в DESTROYED).
- **Реальный DESTROY** holder'а выполняется исключительно из `TransferableScreenHolder.onDestroy()`
  (т.е. из `host.instanceKeeper.destroy()`), см. 5.2.

Это чисто разделяет «конфиг-ченж / временная деактивация» и «настоящее закрытие».

### 5.5 Перенос: unbind → move → rebind

Внутри `UnsafeRelay` (полностью синхронно), порядок `close → open`:

`close(keepInstance = true)` для исходного экрана:
- `holder.unbind()`:
  - `boundHostInstanceKeeper.remove(key)` — **без destroy** (holder вынимается из keeper'а mount'а);
  - `boundHostStateKeeper.unregister(stateKey)`;
  - `boundHostLifecycle.unsubscribe(mirror)`; перевести `holder.lifecycle` вниз до CREATED (pause/stop),
    чтобы UI-подписки встали на паузу, но scope не отменялись;
  - навигатор: `navigator.detachFromParent()` (дерегистрация из родителя, см. 5.6);
- затем `hostNavigator.close(params)` удаляет конфигурацию → decompose уничтожает уже «пустой» mount
  (holder'а там нет → он не уничтожается);
- вернуть `holder`.

`open(savedInstance = holder)` для цели:
- целевой `childScreenFactory` получает `savedInstance` → **не** создаёт новый экран, а:
  - `holder.bindTo(newMountContext, stateKey)` (put + register + subscribe, см. 5.3);
  - `holder.navigator.rebase(newParent = internalNavigator, newScreenPath = internalNavigator.screenPath + params)`
    + регистрация в новом родителе (см. 5.6);
  - `holder.lifecycle` доводится до текущего состояния нового mount'а;
  - вернуть `holder.screen`.

Гарантии порядка: пре-валидация целевого пути (чистый `createOpenPath`) выполняется **до** `close`;
если путь не резолвится — прерываемся, ничего не закрыв. Если `open` всё же упадёт после `close` —
defensive-fallback: повторно `open` в исходную локацию тем же `savedInstance`.

### 5.6 Ре-парентинг навигатора (`rebase`) и модель регистрации

При переносе `screenPath` устаревает у **всех** узлов поддерева (меняется префикс), а `parentNavigator`
меняется у **корня** поддерева. Поэтому:

Изменения в `ScreenNavigatorImpl`:
- `screenPath` и `parentNavigator` делаем **`var`** (сейчас `val`).
- Добавляем `internal var holder: TransferableScreenHolder<Ctx>? = null` (обратная ссылка).
- Добавляем рекурсивный `rebase`:

```kotlin
fun rebase(newParent: ScreenNavigatorImpl<Ctx>?, newScreenPath: ScreenPath) {
    parentNavigator = newParent
    screenPath = newScreenPath
    childScreenNavigators.forEach { (params, child) ->
        child.rebase(this, newScreenPath + params)
    }
}
```

- Меняем модель **регистрации в родителе**. Сейчас (см. `ScreenNavigatorImpl.kt:62-64, 119-128`)
  регистрация делается в `init`, а дерегистрация — через `lifecycle.doOnDestroy`, которая
  захватывает конкретного родителя. Для переносимого навигатора это ломается (родитель меняется,
  а `holder.lifecycle` переживает mount). Делаем **явную** регистрацию:
  - `attachToParent(parent)` → `parent.childScreenNavigators[screenParams] = this` (с проверкой отсутствия).
  - `detachFromParent()` → `parentNavigator.childScreenNavigators.remove(screenParams)`.
  - Дерегистрация при реальном уничтожении — из `holder.onDestroy()` (а не из `lifecycle.doOnDestroy`),
    т.к. holder знает свой текущий `parentNavigator`.
  - Так как переходим на **universal**, этот путь становится единым для всех экранов, создаваемых
    через `childScreenFactory`. Корневой навигатор (`childNavigationRoot`) родителя не имеет — не затрагивается.

Замечания:
- `globalNavigator` и `node` при переносе **не меняются** (единый граф, тот же `screenKey`).
- `screenParams` (вычисляется из `screenPath.last()`) остаётся валидным — последний элемент не меняется.
- `createOpenPath`/`findByPath` работают по ключам пути; после `rebase` ключи корректны, а инстансы
  предков в пути — из новой локации (нужно для корректного выбора среди нескольких инстансов).

### 5.7 Перенос поддеревьев (не-листов)

Т.к. весь subtree живёт внутри `holder.instanceKeeper`/`holder`-контекста, перенос корневого holder'а
через `remove`/`put` переносит поддерево целиком. Вложенным экранам **не нужно** ничего переносить
по отдельности — они «едут» внутри holder'а. Единственное, что требуется для вложенных экранов —
рекурсивный `rebase` путей навигаторов (см. 5.6). Плоские компоненты (без навигатора, напр.
`LogsComponent`) едут автоматически (их контексты производны от контекста holder'а).

Из-за этого `LogViewerScreen` (лист) и любой не-лист обслуживаются одним механизмом.

---

## 6. Изменения в vs-core (по файлам)

Репозиторий `../vs-core`.

### 6.1 `core/navigation/impl` — новый пакет `transfer`

- **Новый файл** `.../navigation/transfer/TransferableScreenHolder.kt` — класс из 5.2–5.5
  (владение keeper'ами, `createContext`, `bindTo`, `unbind`, зеркало lifecycle, `onDestroy`).
- **Новый файл** `.../navigation/transfer/LifecycleMirror.kt` (по необходимости) — реализация
  `Lifecycle.Callbacks`, транслирующая состояния в `holder.lifecycle` с правилом `onDestroy → onStop`.
- Ключ состояния `stateKey` — стабильная строка вида `"transferable_screen"` (в рамках mount'а достаточно).

### 6.2 `core/navigation/impl/.../host/ConfigurationHolder.kt`

- Добавить **transient** внутреннее поле для передачи усыновляемого инстанса:
  ```kotlin
  internal var savedInstance: TransferableScreenHolder<*>? = null
  ```
  Не участвует в `equals/hashCode` и **не** сериализуется.

### 6.3 `core/navigation/impl/.../navigator/HostNavigator.kt`

Расширить интерфейс (единообразно для Pages/Stack/Slot):

```kotlin
fun open(params: IntentScreenParams<*>, intent: ScreenIntent?, savedInstance: TransferableScreenHolder<*>? = null)
fun close(params: IntentScreenParams<*>, keepInstance: Boolean): CloseResult
// close(screenKey) — оставить как есть либо аналогично при необходимости
```

Ввести тип результата:

```kotlin
internal class CloseResult(val closed: Boolean, val instance: TransferableScreenHolder<*>?)
```

### 6.4 `core/navigation/impl/.../host/NavigationPages.kt`, `NavigationStack.kt`, `NavigationSlot.kt`

- В `open(..., savedInstance)` — прокидывать `savedInstance` в создаваемый `ConfigurationHolder`
  (в соответствующих трансформерах `navigate { … }`). `savedInstance` актуален только для «нового»
  элемента; при попадании в уже существующий — это ошибка использования (перенос в ту же локацию,
  где экран уже есть) → защищается гвардом в `transfer` (см. 6.7).
- Для `close(params, keepInstance)` сам host извлекать инстанс не обязан — извлечение делается на
  уровне `ScreenNavigatorImpl.closeInsideThisScreen` (там есть `childScreenNavigators[params].holder`);
  host лишь удаляет конфигурацию. Т.е. `HostNavigator.close` может остаться `Boolean`, а `keepInstance`
  обрабатывается выше. (Оставляем сигнатуру `HostNavigator.close(params): Boolean`, `CloseResult`
  формируется в `ScreenNavigatorImpl`.)

> Итоговое решение по слоям: `keepInstance`/возврат инстанса живут в `ScreenNavigatorImpl`
> (`closeChain`/`closeInsideThisScreen`) и `GlobalNavigator`; `savedInstance` при `open` — прокидывается
> вплоть до `HostNavigator.open`, т.к. `ConfigurationHolder` создаётся именно там.

### 6.5 `core/navigation/impl/.../host/ChildScreenFactory.kt`

Переписать под universal-holder:

```kotlin
internal fun <Ctx> GenericScreen<Ctx>.childScreenFactory(
    configuration: ConfigurationHolder,
    childScreenContext: Ctx,
): GenericScreen<Ctx> {
    val stateKey = "transferable_screen"
    val saved = configuration.savedInstance
    val holder: TransferableScreenHolder<Ctx> = if (saved != null) {
        @Suppress("UNCHECKED_CAST")
        (saved as TransferableScreenHolder<Ctx>).also { h ->
            childScreenContext.instanceKeeper.put(h.key, h)          // повторная привязка
            h.navigator.rebase(internalNavigator, internalNavigator.screenPath + configuration.screenParams)
            internalNavigator.attachChild(h.navigator)               // регистрация в новом родителе
            h.bindTo(childScreenContext, stateKey)                   // register stateKeeper + subscribe lifecycle
        }
    } else {
        childScreenContext.instanceKeeper.getOrCreate(configuration.screenParams) {
            val h = TransferableScreenHolder<Ctx>(key = configuration.screenParams)
            // восстановление состояния из mount.stateKeeper (смерть процесса)
            val holderContext = h.createContext(childScreenContext.componentContextFactory)
            val childNavigator = internalNavigator.createChildNavigator(configuration.screenParams, holderContext)
            val factory = internalNavigator.getChildScreenFactory(configuration.screenParams.asKey())
            val screen = try {
                ScreenNavigatorHolder = childNavigator
                factory.create(holderContext, configuration.screenParams as IntentScreenParams<ScreenIntent>, configuration.intentReceiveChannel)
            } finally { ScreenNavigatorHolder = null }
            childNavigator.screen = screen
            childNavigator.holder = h
            h.screen = screen; h.navigator = childNavigator
            h
        }.also { h -> h.bindTo(childScreenContext, stateKey) }
    }
    return holder.screen
}
```

Тонкости:
- `createChildNavigator` теперь получает **holder-контекст** (не mount). Значит навигатор привязан к
  `holder.lifecycle` (переживает mount) — это ожидаемо (см. 5.6, регистрация становится явной).
- При первом создании навигатор регистрируется в родителе **не** через старый `doOnDestroy`, а через
  явный `attachChild` (нужно поправить `createChildNavigator`/`init`, см. 6.6).
- Состояние stateKeeper holder'а восстанавливается один раз (в `getOrCreate`) из
  `childScreenContext.stateKeeper.consume(stateKey, …)` — детально см. 6.1/5.3.

### 6.6 `core/navigation/impl/.../navigator/ScreenNavigatorImpl.kt`

- `screenPath`, `parentNavigator` → `var`.
- Новое поле `internal var holder: TransferableScreenHolder<Ctx>? = null`.
- Новые методы: `rebase(newParent, newScreenPath)` (5.6), `attachChild(child)`, `detachChild(child)`,
  `attachToParent(parent)`, `detachFromParent()`.
- Заменить регистрацию: убрать `lifecycle.doOnDestroy`-дерегистрацию в `registerScreenNavigator`;
  регистрация — явная (`attachToParent` при создании/усыновлении), дерегистрация — из `holder.onDestroy()`
  (реальное удаление) и из `detachFromParent()` (при переносе-out).
- Проверки консистентности в `init` (`doOnCreate`, проверка hosts/factories) — оставить; они выполняются
  один раз при конструировании holder-контекста и не перезапускаются при переносе.
- Добавить `transfer` в публичный интерфейс навигатора (см. 6.8) и реализацию делегирования в `GlobalNavigator`.
- `closeInsideThisScreen(params, keepInstance): CloseResult`:
  - если `keepInstance` — взять `holder = childScreenNavigators[params]?.holder`, вызвать `holder.unbind()`
    и `holder.navigator.detachFromParent()`, затем `hostNavigator.close(params)`; вернуть `CloseResult(true, holder)`;
  - иначе — как сейчас, `CloseResult(closed, null)`.
- `closeChain(screenPath, keepInstance): CloseResult` — пробросить `keepInstance` и инстанс наверх.
- `openChain(screenPath, intent, savedInstance)` / `openInsideThisScreen(element, intent, savedInstance)` —
  `savedInstance` применять **только** к последнему хопу цепочки (по аналогии с `intent?.takeIf { size == 1 }`).

### 6.7 `core/navigation/impl/.../navigator/GlobalNavigator.kt`

- Добавить `fun transfer(startScreenPath, targetScreenParams, hints)`:
  ```kotlin
  relay.accept {
      val targetPath = createOpenPath(startScreenPath, targetScreenParams, hints)  // пре-валидация
      // локация цели должна отличаться от текущей локации источника (иначе no-op)
      val closeResult = rootNavigator.closeChain(sourcePath.drop(1), keepInstance = true)
      val holder = closeResult.instance ?: return@accept
      rootNavigator.openChain(targetPath.drop(1), intent = null, savedInstance = holder)
  }
  ```
  где `sourcePath` вычисляется аналогично `close` (поиск ноды источника от `startScreenPath`).
- Гварды: если целевой host совпадает с исходным (перенос «на себя») — no-op; если в целевом родителе
  уже есть экран с теми же `screenParams` — no-op/ошибка (иначе конфликт `attachChild`).
- Defensive-fallback при исключении в `openChain` после успешного `close` — повторное открытие в источнике.

### 6.8 `core/navigation/impl/.../navigator/ScreenNavigator.kt`

Публичный интерфейс — добавить:

```kotlin
public fun <S : IntentScreenParams<I>, I : ScreenIntent> transfer(
    screenParams: S,
    hints: List<IntentScreenParams<*>> = emptyList(),
)
```

`ScreenNavigatorImpl.transfer` делегирует в `globalNavigator.transfer(screenPath, screenParams, hints)`.

### 6.9 `core/navigation/impl/.../Navigation.kt`

- (Опционально) поддержать `transfer` на уровне `GenericNavigation` (глобальный вход), добавив
  `NavigationEvent.Transfer` и обработку в `childNavigationRoot`/`handleNavigation`
  (`.../host/NavigationRoot.kt`). Нужно, если перенос будет инициироваться из глобального навигатора,
  а не только из `ScreenNavigator`. Для текущей задачи достаточно `ScreenNavigator.transfer`.

### 6.10 Возможные затрагиваемые места

- `.../host/NavigationRoot.kt` — если добавляем глобальный `transfer` (6.9).
- Тесты навигации в `core/navigation/impl/src/commonTest/...` — правки под новую модель регистрации
  и новые тесты (см. раздел 10).
- `core/decompose/components/.../Component.kt`, `ViewModel.kt` — **изменений не требуют**: `context`
  теперь holder-контекст, а API `viewModel {}` работает как раньше (viewModel живёт в
  `holder.instanceKeeper`, состояние — в `holder.stateKeeper`, lifecycle — `holder.lifecycle`).

---

## 7. Изменения в vs-qa (по файлам)

### 7.1 UI-триггер «открепить»

- `feature/tabs/api/.../ui/component/tabs/TabSupport.kt` — добавить флаг в `TabState`:
  ```kotlin
  data class TabState(
      val icon: ImageVector? = null,
      val name: String? = null,
      val windowName: String? = name,
      val allowClose: Boolean = true,
      val allowDetach: Boolean = false,   // ← новое
  )
  ```
- `feature/tabs/api/.../ui/component/tabs/TabsComponentFactory.kt` — добавить колбэк:
  ```kotlin
  onTabClickDetach: (IntentScreenParams<*>) -> Unit,
  ```
- `feature/tabs/impl/.../ui/component/tabs/TabsComponentImpl.kt` — принять и пробросить `onTabClickDetach`.
- `feature/tabs/impl/.../ui/component/tabs/TabsContent.kt` — в `Tab` добавить кнопку «открепить»
  рядом с кнопкой закрытия, показывать при `state.allowDetach`:
  ```kotlin
  if (state.allowDetach) {
      QaIconButton(onClick = { onTabClickDetach(item.configuration.screenParams) },
          modifier = Modifier.hint("Открепить в новое окно")) {
          Icon(imageVector = <IconOpenInNew>, contentDescription = "detach")
      }
  }
  ```
- `feature/root-screen/impl/.../ui/screen/root/RootScreen.kt` — пробросить колбэк в фабрику табов:
  ```kotlin
  tabsComponentFactory.create(
      ...
      onTabClickClose = { navigator.close(it) },
      onTabClickDetach = { navigator.transfer(it, hints = listOf(WindowScreenParams(Random.nextLong().toString()))) },
      context = context.childContext("tabs"),
  )
  ```
  (нужен импорт `WindowScreenParams` из `feature/multi-window/api` — проверить, что зависимость доступна
  в `root-screen/impl`; `MultiWindowRootScreenComponent` уже связывает эти модули, но `WindowScreenParams`
  лежит в `multi-window/api`, а `root-screen` уже зависит от него транзитивно через навигацию — при
  отсутствии зависимости добавить `feature/multi-window/api` в `root-screen/impl`).

### 7.2 Включение флага `allowDetach` для лог-таба

- `feature/log-viewer/impl/.../ui/screen/logViewer/LogViewerViewModel.kt` (или где формируется `tabState`)
  — выставить `allowDetach = true` в `TabSupport.TabState` для `LogViewerScreen`.

### 7.3 Прочее

- DI-изменений **не требуется** (нет глобального store/реестра — инстанс передаётся из рук в руки).
- `HomeScreen.kt` — существующий сценарий «открыть в новом окне» оставляем без изменений; «открепить»
  добавляется отдельной кнопкой на табе.

---

## 8. Публичный API навигации (итог)

```kotlin
interface ScreenNavigator {
    fun <S : IntentScreenParams<I>, I : ScreenIntent> open(screenParams: S, intent: I? = null, hints: List<IntentScreenParams<*>> = emptyList())
    fun close(screenParams: IntentScreenParams<*>)
    fun close()
    fun <S : IntentScreenParams<I>, I : ScreenIntent> transfer(screenParams: S, hints: List<IntentScreenParams<*>> = emptyList())  // ← новое
}
```

Семантика `transfer(screenParams, hints)`: найти уже открытый экран `screenParams` относительно
текущего узла, отвязать его удержанный инстанс (со всем поддеревом) и заново открыть в локации,
которую задают `hints` (и стандартный резолвинг), **без** пересоздания viewModel и без сброса состояния.

---

## 9. Краевые случаи и гарантии

- **Атомарность**: `transfer` целиком в `UnsafeRelay`; пре-валидация целевого пути до `close`;
  defensive-fallback при падении `open`.
- **Перенос «на себя»/дубликаты**: если целевая локация == исходной, или в целевом родителе уже есть
  экран с теми же `screenParams` — no-op (гвард).
- **Смена конфигурации Android**: пока НЕ идёт перенос, holder привязан к mount → удержание и
  сериализация штатные, регресса нет.
- **Смерть процесса**: состояние viewModel сохраняется через `holder.stateKeeper` → `mount.stateKeeper`
  → штатная сериализация decompose; после рестарта holder пересоздаётся и `consume`-ит состояние.
  Перенесённость (в каком окне таб) сама по себе восстанавливается обычным восстановлением конфигураций
  Pages/Stack/Slot (перенос — это close+open, отражённый в конфигурациях).
- **`pageStatus` неактивных табов**: если навигация уничтожает неактивные child'ы (DESTROYED), их
  holder'ы уничтожатся штатно; для табов в `RootScreen` неактивные держатся в CREATED (не уничтожаются),
  так что состояние живо (текущее поведение сохраняется).
- **BackHandler**: holder имеет собственный `BackDispatcher`; при необходимости можно мостить к
  back-handler'у mount'а (для десктопа некритично).
- **Компоуз-состояние `remember`**: сбрасывается при ре-маунте в новом окне в любом случае — это
  ожидаемо и не является «состоянием экрана».

---

## 10. План тестирования

### 10.1 Unit-тесты vs-core (`core/navigation/impl/src/commonTest`, `core/decompose/test`)

- `TransferableScreenHolder`: build → эмуляция `unbind` (как при `close(keep=true)`) → `bindTo` к
  новому `TestComponentContext` → тот же экземпляр viewModel и то же состояние; `instanceKeeper` не
  уничтожен, scope не отменён.
- `ScreenNavigatorImpl.rebase`: путь и `parentNavigator` пересчитываются рекурсивно; регистрация
  переезжает из старого родителя в новый; после `rebase` резолвинг `open/close` из перенесённого узла
  корректен.
- `GlobalNavigator.transfer`: пре-валидация пути; порядок close→open; гварды (self/duplicate);
  fallback при падении open.
- Регресс: существующие `NavigationTest`, `GlobalNavigatorCreateOpenPathTest`,
  `NavigationRepositoryTest`, `ScreenTest` — должны проходить после смены модели регистрации.
- `core/decompose/test/BaseComponentTest` уже умеет пересоздавать контекст с сохранением/сбросом
  keeper'ов — использовать для проверки поведения holder при config-change/process-death.

### 10.2 Ручной сценарий (desktop)

1. Открыть лог, применить фильтры, проскроллить, открыть mapping.
2. Нажать «открепить» на табе.
3. Проверить: новое окно показывает **идентичное** состояние (фильтры, скролл, индексы), без
   переиндексации/перезагрузки; старый таб исчез; в логах нет `viewModel.onDestroy` для этой сессии.
4. Закрыть новое окно — состояние уничтожается штатно (нет утечек).

### 10.3 Команды (см. AGENTS.md)

```
./gradlew detekt
./gradlew test allTests
./gradlew :app:jvm:jvmRun            # ручная проверка на десктопе
```

CI-порядок: detekt → unit tests → build.

---

## 11. Порядок внедрения (этапы)

1. **vs-core, инфраструктура**: `TransferableScreenHolder` + зеркало lifecycle (6.1). Юнит-тесты holder.
2. **vs-core, навигатор**: `var screenPath/parentNavigator`, `rebase`, явная модель регистрации,
   `holder`-ссылка (6.6). Прогон/починка существующих тестов навигации.
3. **vs-core, host-навигация**: universal-`childScreenFactory` (6.5), `ConfigurationHolder.savedInstance`
   (6.2), `savedInstance` в `open` у Pages/Stack/Slot (6.3–6.4).
4. **vs-core, transfer**: `CloseResult`, `keepInstance` в close-цепочке, `GlobalNavigator.transfer`,
   `ScreenNavigator.transfer` (6.7–6.8). Юнит-тесты transfer.
5. **Публикация vs-core** в `mavenLocal()` (или сборка с `ru.vs.core.useVsCoreSources=true`).
6. **vs-qa, UI**: `TabState.allowDetach`, колбэк `onTabClickDetach`, кнопка в `TabsContent`,
   проброс из `RootScreen`, `allowDetach=true` для `LogViewerScreen` (7.1–7.2).
7. **Проверка**: detekt, тесты, ручной сценарий на десктопе; затем проверка на Android
   (отсутствие регресса config-change/process-death).

> Примечание по vs-core sources: при `ru.vs.core.useVsCoreSources=true` изменения ядра подхватываются
> из соседнего `../vs-core` напрямую; иначе нужно опубликовать артефакты в `mavenLocal()`.

---

## 12. Риски

- **Большой blast radius (universal)**: holder становится стандартным способом построения любого экрана;
  меняется модель регистрации навигаторов, используемая всеми экранами. Требует полного прогона тестов
  навигации и аккуратного зеркалирования lifecycle.
- **Корректность зеркала lifecycle**: неверная трансляция состояний (особенно `onDestroy → onStop` и
  различение config-change vs removal) — главный источник тонких багов. Нужны прицельные тесты.
- **Ключи в keeper'ах**: конфликт `put` при переносе в локацию с уже открытым таким экраном — покрыт
  гвардом, но требует внимательного тестирования.
- **Взаимодействие с `pageStatus`/уничтожением неактивных child'ов** при переносе неактивного таба.
- **BackHandler-мост** — при необходимости корректной обработки «назад» в перенесённом поддереве.

---

## 13. Открытые вопросы

- Нужен ли глобальный `Navigation.transfer` (6.9), или достаточно `ScreenNavigator.transfer`?
- Иконка/место кнопки «открепить» (сейчас — отдельная кнопка рядом с закрытием; ждём финального UX).
- Нужно ли сохранять «перенесённость» между запусками на Android детальнее, чем даёт штатное
  восстановление конфигураций (сейчас — в объёме close+open).
