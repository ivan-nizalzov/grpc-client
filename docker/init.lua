box.cfg{}

box.schema.user.create('tarantool', { password = 'tarantool' }, {if_not_exists = true})
box.schema.user.grant('tarantool', 'read,write,execute', 'universe')

box.schema.space.create('KV', {if_not_exists = true})
box.space.KV:format({
    {name = 'key', type = 'string'},
    {name = 'value', type = 'varbinary', is_nullable = true}
})
box.space.KV:create_index('primary', {parts = {'key'}, if_not_exists = true})

dofile('/usr/local/share/tarantool/functions.lua')

box.schema.func.create('count_tuples', {if_not_exists = true})