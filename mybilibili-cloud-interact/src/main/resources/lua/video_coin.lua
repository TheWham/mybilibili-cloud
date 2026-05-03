-- KEYS[1] 投币状态 key
-- KEYS[2] 投币用户的实时统计 Hash
-- KEYS[3] 视频作者的实时统计 Hash
-- ARGV[1] 用户硬币字段
-- ARGV[2] 视频投币字段
-- ARGV[3] 本次投币数量
-- ARGV[4] 统计 Hash 的过期时间(毫秒)

if redis.call('EXISTS', KEYS[1]) == 1 then
    return 1
end

local senderCoin = tonumber(redis.call('HGET', KEYS[2], ARGV[1]) or '0')
local actionCount = tonumber(ARGV[3])
if senderCoin < actionCount then
    return 2
end

redis.call('HINCRBY', KEYS[2], ARGV[1], -actionCount)
redis.call('PEXPIRE', KEYS[2], tonumber(ARGV[4]))
redis.call('HINCRBY', KEYS[3], ARGV[1], actionCount)
redis.call('HINCRBY', KEYS[3], ARGV[2], actionCount)
redis.call('PEXPIRE', KEYS[3], tonumber(ARGV[4]))
redis.call('SET', KEYS[1], actionCount)
return 0
