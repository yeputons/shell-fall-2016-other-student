В общем и целом, архитектура у меня это более-менее Module-View-Controller,
но я позволил себе сделать пару упращений (далее подробнее  ).

Происходит примерно соедующее:

Главный класс -- `Messenger` -- запускает наш главный контроллер
`GeneralController`. Этот `GeneralController` как бы "контроллер над
контроллерами". Он, во-первых, содержит `userInfo` -- информация
о юзере и подключениях, во-вторых, содержит `SceneManager` -- это
такая штука, которая ответственна за переключение на след. скрин и
сохранение истории, чтобы мы могли нажать кнопку `назад`.

Далее, есть набо скринов (они все лежат в `package view`). Вьюшки
просто строят скрины, которые отобразит `SceneManager`. К каждой
вьюшке привязан контроллер. И вот тут есть одно не очень красивое место --
вместо регистрации методов контроллера во вьюшках и просто вызова их,
я напрямую передаю вьюшке контроллер, а в контроллере храню вьюшку.
Вот тут это не очень MVC + есть перекрестные ссылки, что нехорошо. Но
все же я считаю, что тут это допустимо, т.к. регистрация методов на события
приведет к разрастанию кода, а наши экраны так устроенны, что в них нет
никаких аддонов и все управление каждым экраном лежит в одном лишь
соответствующем контроллере. Так что, в данном случае, я думаю, что это
не так уж плохо.

Кроме всего этого, в `package model` лежат всякие `Connection`'ы и
таски для них. Ну там я, вроде, в комментариях написал, что да как.

В `package common` лежит класс `userInfo` (возможно, это можно было
положить в `model`, но почему-то мне захотелось подчеркнуть, что с `userInfo`
работать могут все -- и `view`, и `model`, и `controller`). Там же лежит
`Proto.java` -- сгенерированный `java`-файл из моего `.proto` файла, который
лежит в `/src/main/protobuf/Message.proto` -- протокол передачи сообщений.

Вроде все.