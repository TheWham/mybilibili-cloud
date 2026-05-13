local current = redis.call('GET', KEYS[1])
local limit = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])

if current and tonumber(current) >= limit then
    return 0
end

local value = redis.call('INCR', KEYS[1])
if value == 1 and expire and expire > 0 then
    redis.call('PEXPIRE', KEYS[1], expire)
end

if value > limit then
    redis.call('DECR', KEYS[1])
    return 0
end

return value
