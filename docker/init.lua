box.cfg{}

box.schema.user.create('tarantool', { password = 'tarantool' }, {if_not_exists = true})
box.schema.user.grant('tarantool', 'read,write,execute', 'universe')

box.schema.space.create('KV', {if_not_exists = true})
box.space.KV:format({
    {name = 'key', type = 'string'},
    {name = 'value', type = 'varbinary', is_nullable = true}
})
box.space.KV:create_index('primary', {parts = {'key'}, if_not_exists = true})

function count_tuples()
    return box.space.KV:count()
end

function range_tuples(key_since, key_to)
    if not key_since or not key_to then
        error("Both `key_since` and `key_to` parameters are required")
    end

    local result = box.space.KV.index[0]:select({key_since}, {iterator = 'GE'})

    local filtered_result = {}
    for _, tuple in ipairs(result) do
        local key = tuple[1]
        if key_to and key > key_to then
            break
        end
        table.insert(filtered_result, tuple)
    end

    return filtered_result
end

box.schema.func.create('count_tuples', {if_not_exists = true})
box.schema.func.create('range_tuples', {if_not_exists = true})