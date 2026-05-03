-- ARGV[4] 动作状态缓存过期时间(毫秒)

local actionCount = tonumber(ARGV[2])
local currentValue = redis.call('GET', KEYS[1])
if currentValue ~= false and tonumber(currentValue) ~= nil and tonumber(currentValue) > 0 then
    redis.call('SET', KEYS[1], 0)
    redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[4]))
    redis.call('HINCRBY', KEYS[2], ARGV[1], -actionCount)
    redis.call('PEXPIRE', KEYS[2], tonumber(ARGV[3]))
    return -1
end

redis.call('SET', KEYS[1], actionCount)
redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[4]))
redis.call('HINCRBY', KEYS[2], ARGV[1], actionCount)
redis.call('PEXPIRE', KEYS[2], tonumber(ARGV[3]))
return 1
