[![Build Status](https://travis-ci.com/yoomoney/moira-trigger-plugin.svg?branch=master)](https://travis-ci.com/yoomoney/moira-trigger-plugin)
[![codecov](https://codecov.io/gh/yoomoney/moira-trigger-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/yoomoney/moira-trigger-plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# moira-trigger-plugin

Плагин для загрузки триггеров Moira, описанных с помощью Kotlin DSL.

## Подключение

```groovy
buildscript {
    repositories {  
       mavenCentral()
    }
    dependencies {
        classpath 'ru.yoomoney.gradle.plugins:moira-trigger-plugin:4.+'
    }
}

apply plugin: 'ru.yoomoney.gradle.plugins.moira-trigger-plugin'
```

## Конфигурация

```groovy
moira {
    // URL до API Moira.
    // Параметр обязательный.
    url = 'http://example.net/api'

    // Название директории, в которой хранятся скрипты с определением триггеров. 
    // Директория определяется относительно projectDir.
    // Задан по умолчанию.
    // Параметр обязательный.
    dir = 'moira' 
    
    // Логин пользователя, от имени которого будут загружены триггеры.
    // Используется для авторизации.
    // Может быть использован без свойства password.
    // Параметр опциональный.
    login = 'admin'
    
    // Пароль пользователя, от имени которого будут загружены триггеры.
    // Не будет использован без указания свойства login.
    // Параметр опциональный.
    password = 'admin'
}
```

* Добавьте в качестве зависимости `moira-kotlin-dsl` отдельно для триггеров из проекта и из подключенного артефакта:
```groovy
dependencies {
    moiraFromDirCompile 'ru.yoomoney.tech:moira-kotlin-dsl:2.0.0'
    moiraFromArtifactCompile 'ru.yoomoney.tech:moira-kotlin-dsl:2.0.0'
}  
```

* Добавьте в директорию `$projectDir/moira` описание триггера с использованием DSL (файл с расширением `.kts`).
   Например, триггер, который рассылает уведомления со статусом `ERROR`, если количество неуспешно обработанных входящих 
   запросов конкретного компонента превысило 20 за последние 10 минут:
   ```kotlin
   trigger(id = "incoming_requests_error", name = "Component incoming requests error count") {
       description = "Кол-во неуспешно обработанных входящих запросов за последние 10 минут превысило допустимый порог"
   
       // Отправляем уведомления на рассылку вашей команды
       tags += "component-team"
   
       // Указываем метрику неуспешно обработанных входящих запросов, суммируем за последние 10 минут и по всем хостам
       val t1 by target("sumSeries(summarize(*.*.component.requests.incoming.*.*.process_time.error.count, '10m', 'sum', false))")
       
       expression {
           simple {
               // Отправляем ERROR уведомление, если заданная метрика превысила допустимое пороговое значение (20)
               rising {
                   error = 20.0
               }
           }
       }  
       
       // Задаем поведение в случае, если метрики нет совсем 
       ttl {
           // Отправить ERROR уведомление, если метрика не пушилась в течение 5 минут
           state = TriggerState.ERROR
           ttl = Duration.ofMinutes(5)
       }
   
       // Задаем допустимое расписание для уведомлений. По-умолчанию уведомления отправляются 24/7.
       schedule {
           // Не шлем в субботу и воскресенье: хотим нормально отдохнуть на выходных
           -DayOfWeek.SATURDAY
           -DayOfWeek.SUNDAY
   
           // По будням только в рабочее время
           watchTime = "10:00".."19:00"
       }
   }
   ```

## Задачи

* Вызовите таску `collectMoiraTriggers` для первичной валидации триггеров (в stdout будут выведены JSON-строки для 
   каждого обрабатываемого триггера).

* Вызовите таску `uploadMoiraTriggers` для загрузки триггеров Moira.
