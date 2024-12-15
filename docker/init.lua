box.cfg{}

box.schema.user.create('tarantool', { password = 'tarantool' })

box.schema.user.grant('tarantool', 'read,write,execute', 'universe')

box.schema.space.create('KV', {if_not_exists = true})

box.space.KV:format({
    {name = 'key', type = 'string'},            -- Ключ: строка
    {name = 'value', type = 'varbinary', is_nullable = true} -- Значение: бинарные данные (nullable)
})

box.space.KV:create_index('primary', {parts = {'key'}, if_not_exists = true})
