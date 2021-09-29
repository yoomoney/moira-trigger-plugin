## [4.0.0](https://github.com/yoomoney/moira-trigger-plugin/pull/1) (29-09-2021)

* `moira-trigger-plugin` has moved to github
* Build with travis
* Update version: ru.yoomoney.tech:moira-kotlin-client:2.0.0
* Update version: ru.yoomoney.tech:moira-kotlin-dsl:2.0.0
* **breaking changes** Packages `ru.yandex.money` have been renamed into `ru.yoomoney.tech`

## [3.3.0]() (12-02-2021)

* Переименование yamoney-kotlin-module-plugin в ru.yoomoney.gradle.plugins.kotlin-plugin

## [3.2.2]() (30-11-2020)

* Обновлена версия kotlin 1.3.71 -> 1.3.50

## [3.2.1]() (23-11-2020)

* Замена доменов email @yamoney.ru -> @yoomoney.ru

## [3.2.0]() (03-07-2020)

* Up gradle version: 6.0.1 -> 6.4.1.

## [3.1.2]() (01-07-2020)

* If trigger throws some exception, plugin shouldn`t suppress them

## [3.1.1]() (28-02-2020)

* Don't add bibucket pull request link into changelog.md on release

## [3.1.0]() (21-02-2020)

* Сборка на java 11

## [3.0.1]() (30-01-2020)

* Fix gradle.wrapper distributionUrl and update kotlin version 1.2.61 -> 1.3.50.

## [3.0.0]() (30-01-2020)

* Update gradle version `4.10.2` -> `6.0.1`
* Update dependecies version

## [2.0.4]() (02-12-2019)

* Fix `group` in `build-public.gradle`

## [2.0.3]() (27-11-2019)

* Fix `.travis.yml` script to publish Gradle plugin

## [2.0.2]() (27-11-2019)

* Minor fixes in README.md

## [2.0.1]() (27-11-2019)

* Add support for GitHub publication

## [2.0.0]() (11-09-2019)

1. Добавлен sourceSet moiraTriggersCompile, куда можно добавлять артефакты, содержащие описания триггеров.
2. SourceSet разделен на moiraFromArtifact и moiraFromDir для возможности добавления moira-dsl разных версий.

## [1.0.0]() (04-07-2019)

* первая версия плагина