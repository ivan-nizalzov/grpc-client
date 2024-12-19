# gRPC Сервис на Java для подключения к Tarantool

### Сервис имплементирует следующее API:
- put(key, value) - сохраняет в БД значение для новых ключей и перезаписывает значение для существующих;
- get(key) - возвращает значение для указанного ключа;
- delete(key) - удаляет значение для указанного ключа;
- range(key_since, key_to) → отдает gRPC stream пар ключ-значение из запрошенного диапазона;
- count() - возвращает кол-во записей в БД.

Для хранения данных используется БД Tarantool 3.2.x.

В качестве клиента для работы с БД Tarantool используется библиотеку **tarantool/cartridge-java** версии 0.13.0

Данные сохраняются в спейс 'KV' cо следующей схемой данных:
```
{
    {name = 'key', type = 'string'},
    {name = 'value', type = 'varbinary', is_nullable=true}
}
```
----
### Используемый стек:
- Java 17;
- Spring Boot;
- gRPC;
- Tarantool 3.2.0;
- Tarantool Cartridge driver 0.13.0;
- Docker compose.

### Пример сообщения для отправки в Postman:
```
{
    "key": "test_key",
    "value": "dGVzdF92YWx1ZQ=="  // "test_value" в base64
}
```

----

### Для запуска приложения:

1. `mvn clean package`
2. Тут должен быть `docker-compose-up`, но, к сожалению, ни официальная документация, ни Google, ни детальное изучение кода **testcontainers-java-tarantool-master** и **cartridge-java-master** не помогли добиться того, чтобы Tarantool 3.2.0 запустился в контейнере.
Даже эта команда не работает (взял отсюда: https://www.tarantool.io/en/doc/2.11/how-to/getting_started_db/): 
```dockerfile
docker run \
  --name mytarantool \
  -d -p 3301:3301 \
  -v /data/dir/on/host:/var/lib/tarantool \
  tarantool/tarantool:latest
```
И тесты в `io.tarantool.driver.integration.ProxyTarantoolClientExampleIT` из библиотеки **cartridge-java-master** не работают, поэтому мне не удалось написать тесты с помощью TestContainers.
На официальном сайте очень мало актуальной информации по конфигурированию Tarantool в контейнерах, и непонятно, почему нет готового Docker-образа, который можно запуллить и запустить в режиме standalone.

В итоге мне удалось развернуть Tarantool 3.2.0 на локальной машине Linux без контейнера, вручную прописал миграцию из файла `init.lua`, проверил свой код в Postman - всё работает.

Проблема заключается в том, что инстанс в контейрере не запускается никак, даже если подключиться к нему в терминале и вручную прописать `tt start test_db`.

3. Чтобы запустить и проверить работоспособность приложения, необходимо:
- удалить файл docker-compose;
- удалить Dockerfile;
- запустить на локальной машине Tarantool 3.2.0 (предварительно установив утилиту `tt` и сконфигурировав инстанс, подробнее тут: https://www.tarantool.io/en/doc/latest/getting_started/getting_started_db/);
- выполнить в терминале скрипт миграции из файла `init.lua` в папке `docker`;
- выполнить команду mvn clean package;
- запустить проект в IDEA;
- скопировать файл `tarantool_cdc.proto` из модуля `proto` в Postman;
- отправить сообщения для вызова соответствующих функций.

#### Примеры сообщений:

- для функции `put()`:
```json
{
  "key": "test_key",
  "value": "dGVzdF92YWx1ZQ=="  // "test_value" в base64
}
```

- для функции `get()`:
```json
{
  "key": "test_key"
}
```

- для функции `delete()`:
```json
{
  "key": "test_key"
}
```

- для функции `range()`:
```json
{
  "key_since": "key1",
  "key_to": "key5"
}
```

- для функции `count()`:
```json
{}
```


