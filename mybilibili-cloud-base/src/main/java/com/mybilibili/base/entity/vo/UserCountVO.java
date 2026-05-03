package com.mybilibili.base.entity.vo;

import lombok.Data;

/**
 * 用户主页和登录态展示的统计数量。
 *
 * <p>这个 VO 会被 auth 通过 Feign 暴露给前端，所以放在 base，
 * 避免 auth 为了一个返回对象反向依赖 user 服务。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Data
public class UserCountVO {

    /**
     * 关注数。
     */
    private Integer focusCount;

    /**
     * 粉丝数。
     */
    private Integer fansCount;

    /**
     * 当前硬币数。
     */
    private Integer currentCoinCount;

    /**
     * 获赞数。
     */
    private Integer likeCount;

    /**
     * 播放数。
     */
    private Integer playCount;
}
