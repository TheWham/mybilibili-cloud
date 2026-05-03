package com.mybilibili.common.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.common.constants.TokenRedisKeys;
import com.mybilibili.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * 登录态缓存组件。
 *
 * <p>这里仅保留 token 读写，不放验证码、分类、播放、互动等业务缓存。
 * 这样 common 还能支撑登录切面和控制器基类，但不会继续承载具体业务逻辑。</p>
 */
@Component
public class TokenRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public void saveTokenUserInfo(TokenUserInfoDTO tokenUserInfoDTO) {
        String tokenId = UUID.randomUUID().toString();
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT;
        tokenUserInfoDTO.setExpireAt(System.currentTimeMillis() + expireTime);
        tokenUserInfoDTO.setTokenId(tokenId);
        redisUtils.setex(TokenRedisKeys.WEB_TOKEN_KEY + tokenId, tokenUserInfoDTO, expireTime);
    }

    public void updateTokenUserInfo(TokenUserInfoDTO tokenUserInfoDTO) {
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT;
        redisUtils.setex(TokenRedisKeys.WEB_TOKEN_KEY + tokenUserInfoDTO.getTokenId(), tokenUserInfoDTO, expireTime);
    }

    public String saveToken4Admin(String account) {
        String tokenId = UUID.randomUUID().toString();
        redisUtils.setex(TokenRedisKeys.ADMIN_TOKEN_KEY + tokenId, account, Constants.REDIS_EXPIRE_TIME_ONE_DAY);
        return tokenId;
    }

    public void cleanWebToken(String tokenId) {
        redisUtils.delete(TokenRedisKeys.WEB_TOKEN_KEY + tokenId);
    }

    public void cleanAdminToken(String tokenId) {
        redisUtils.delete(TokenRedisKeys.ADMIN_TOKEN_KEY + tokenId);
    }

    public TokenUserInfoDTO getTokenInfo(String tokenId) {
        return (TokenUserInfoDTO) redisUtils.get(TokenRedisKeys.WEB_TOKEN_KEY + tokenId);
    }

    public Object getTokenInfo4Admin(String tokenId) {
        return redisUtils.get(TokenRedisKeys.ADMIN_TOKEN_KEY + tokenId);
    }

    public String getTokenIdByUserId(String userId) {
        Object value = redisUtils.get(TokenRedisKeys.USER_TOKEN_KEY + userId);
        return Objects.isNull(value) ? null : value.toString();
    }

    public void saveTokenIdByUserId(String userId, String token) {
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT;
        redisUtils.setex(TokenRedisKeys.USER_TOKEN_KEY + userId, token, expireTime);
    }

    public void cleanExistToken(String userId) {
        redisUtils.delete(TokenRedisKeys.USER_TOKEN_KEY + userId);
    }

    public void cleanUserLoginToken(String userId) {
        String tokenId = getTokenIdByUserId(userId);
        if (tokenId != null) {
            cleanWebToken(tokenId);
        }
        cleanExistToken(userId);
    }
}
