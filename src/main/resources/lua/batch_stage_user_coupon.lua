local function combine(status, stagedSize)
    return status * 4294967296 + stagedSize
end

local rawStock = redis.call('get', KEYS[1])
local stagedSetKey = KEYS[2]
local receiveCountKey = KEYS[3]
local userRow = ARGV[1]
local perUserLimit = tonumber(ARGV[2])

if not rawStock then
    return combine(1, redis.call('scard', stagedSetKey))
end

local stock = tonumber(rawStock)
if stock <= 0 then
    return combine(2, redis.call('scard', stagedSetKey))
end

local receiveCount = tonumber(redis.call('get', receiveCountKey) or '0')
if receiveCount >= perUserLimit then
    return combine(3, redis.call('scard', stagedSetKey))
end

redis.call('decrby', KEYS[1], 1)
redis.call('incrby', receiveCountKey, 1)
redis.call('sadd', stagedSetKey, userRow)

return combine(0, redis.call('scard', stagedSetKey))
