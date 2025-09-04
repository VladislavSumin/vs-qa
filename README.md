# VS-QA

Просмотровщик логов.

## Запуск

* Скачать vs-qa.jar из последней сборки в github actions. (На данный момент поддерживается только macos)
* Запустить командой `java -jar vs-qa.jar <path_to_log_file> [path_to_mapping_file]`

## Локальная сборка

Для локальной сборки проекта необходимо скачать репозиторий [vs-core](https://github.com/VladislavSumin/vs-core/) на
один уровень файловой системы с этим репозиторием. После этого дополнительных действий не требуется.

* Собрать jar файл - `./gradlew :app:buildFatJarMain`.
* Запустить `./gradlew :app:jvmRun --args "<путь_к_логу>"`.