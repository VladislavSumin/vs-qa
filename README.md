# VS-QA

Просмотровщик логов.

## Установка

### Через Homebrew (macOS)

```bash
brew tap VladislavSumin/vs-qa
brew install vs-qa
```

После установки:

```bash
vs-qa [path_to_log_file] [path_to_mapping_file]
```

Обновление:

```bash
brew upgrade vs-qa
```

### Вручную

* Скачать vs-qa.jar из последнего [релиза](https://github.com/VladislavSumin/vs-qa/releases). (На данный момент поддерживается только macos)
* Запустить командой `java -jar vs-qa.jar [path_to_log_file] [path_to_mapping_file]`

## Локальная сборка

Для локальной сборки проекта необходимо скачать репозиторий [vs-core](https://github.com/VladislavSumin/vs-core/) на
один уровень файловой системы с этим репозиторием. После этого дополнительных действий не требуется.

* Собрать jar файл - `./gradlew :app:jvm:buildFatJarMain`.
* Собрать минимизированный jar файл - `./gradlew :app:jvm:buildFatJarMainMin`.
* Запустить `./gradlew :app:jvm:jvmRun [--args "<path_to_log_file> [path_to_mapping_file]"]`.