package com.mybilibili.interact.services.impl;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.event.VideoDanmuPostEvent;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.UserDailyLimitTypeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.base.utils.JsonUtils;
import com.mybilibili.common.component.UserDailyLimitComponent;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.interact.component.InteractRedisComponent;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;
import com.mybilibili.interact.mappers.VideoDanmuMapper;
import com.mybilibili.interact.mq.producer.VideoDanmuEventProducer;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 视频弹幕 Service。
 */
@Service("VideoDanmuService")
public class VideoDanmuServiceImpl implements VideoDanmuService {

    @Resource
    private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

    @Resource
    private UserDailyLimitComponent userDailyLimitComponent;

    @Resource
    private VideoInfoClient videoInfoClient;

    @Resource
    private RedisUtils<String> redisUtils;

    @Resource
    private InteractRedisComponent interactRedisComponent;

    @Resource
    private VideoDanmuEventProducer videoDanmuEventProducer;

    @Override
    public List<VideoDanmu> findListByParam(VideoDanmuQuery param) {
        return videoDanmuMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(VideoDanmuQuery param) {
        return videoDanmuMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param) {
        Integer count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoDanmu> list = findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(VideoDanmu bean) {
        return videoDanmuMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<VideoDanmu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoDanmuMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<VideoDanmu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoDanmuMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public VideoDanmu getVideoDanmuByDanmuId(Integer danmuId) {
        return videoDanmuMapper.selectByDanmuId(danmuId);
    }

    @Override
    public Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId) {
        return videoDanmuMapper.updateByDanmuId(bean, danmuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteVideoDanmuByDanmuId(Integer danmuId, Boolean isAdmin, String userId) {
        VideoDanmu danmu = Optional.ofNullable(videoDanmuMapper.selectByDanmuId(danmuId))
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

        boolean canDirectDelete = Boolean.TRUE.equals(isAdmin)
                || danmu.getUserId().equals(userId)
                || isVideoOwner(danmu.getVideoId(), userId);
        if (!canDirectDelete) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer count = videoDanmuMapper.deleteByDanmuId(danmuId);
        if (count != null && count > 0) {
            // 弹幕删除属于低频管理动作，直接失效该视频分片缓存，比精确匹配 ZSet 成员更稳。
            interactRedisComponent.deleteVideoDanmuCache(danmu.getVideoId(), danmu.getFileId());
        }
        return count;
    }

    @Override
    public void postDanmu(VideoDanmu videoDanmu) {
        validatePostDanmu(videoDanmu);
        VideoInfoDTO videoInfo = getVideoInfoWithCache(videoDanmu.getVideoId());
        if (!checkDanmuOpenWithCache(videoDanmu.getVideoId())) {
            throw new BusinessException("弹幕功能已关闭");
        }
        // 额度必须在入口原子占用，不能等消费者落库后再扣，否则热点视频下会被瞬间打穿限制。
        userDailyLimitComponent.occupyDailyAction(videoDanmu.getUserId(), UserDailyLimitTypeEnum.DANMU);

        VideoDanmuPostEvent event = buildDanmuPostEvent(videoDanmu, videoInfo);
        VideoDanmu cacheDanmu = buildCacheDanmu(event);
        // 先写热缓存再投 MQ：接口成功后播放页能马上看到弹幕，MySQL 由消费者最终落库。
        String cacheValue = interactRedisComponent.saveVideoDanmuCache(cacheDanmu);
        if (StringTools.isEmpty(cacheValue)) {
            userDailyLimitComponent.releaseDailyAction(videoDanmu.getUserId(), UserDailyLimitTypeEnum.DANMU);
            throw new BusinessException(ResponseCodeEnum.CODE_503);
        }

        try {
            videoDanmuEventProducer.sendDanmuPostEvent(event);
        } catch (RuntimeException e) {
            // MQ 投递失败时，入口缓存里的“已发送”状态也要撤掉，避免用户看到一条不会落库的弹幕。
            interactRedisComponent.removeVideoDanmuCache(event.getVideoId(), event.getFileId(), cacheValue);
            userDailyLimitComponent.releaseDailyAction(videoDanmu.getUserId(), UserDailyLimitTypeEnum.DANMU);
            throw new BusinessException(ResponseCodeEnum.CODE_503);
        }
    }

    @Override
    public List<VideoDanmu> loadDanmu(String fileId, String videoId) {
        if (!checkDanmuOpenWithCache(videoId)) {
            return Collections.emptyList();
        }
        // 播放页是高频读场景，优先走 Redis，避免热点视频反复扫 MySQL。
        List<VideoDanmu> cacheList = interactRedisComponent.loadVideoDanmuCache(videoId, fileId);
        if (!cacheList.isEmpty()) {
            return cacheList;
        }

        // 缓存未命中通常是冷视频或缓存刚过期，回源后立即重建分片缓存。
        VideoDanmuQuery danmuQuery = new VideoDanmuQuery();
        danmuQuery.setFileId(fileId);
        danmuQuery.setVideoId(videoId);
        danmuQuery.setOrderBy("v.time asc");
        List<VideoDanmu> danmuList = findListByParam(danmuQuery);
        interactRedisComponent.rebuildVideoDanmuCache(videoId, fileId, danmuList);
        return danmuList;
    }

    private boolean checkDanmuOpen(String videoId) {
        Boolean danmuOpen = videoInfoClient.checkVideoDanmuStatusByVideoId(videoId);
        return Boolean.TRUE.equals(danmuOpen);
    }

    private void validatePostDanmu(VideoDanmu videoDanmu) {
        if (videoDanmu == null
                || StringTools.isEmpty(videoDanmu.getUserId())
                || StringTools.isEmpty(videoDanmu.getVideoId())
                || StringTools.isEmpty(videoDanmu.getFileId())
                || StringTools.isEmpty(videoDanmu.getText())
                || videoDanmu.getTime() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (videoDanmu.getTime() < Constants.ZERO) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 前端已经做了长度校验，这里再做一次兜底，避免绕过 Controller 直接调服务层。
        String text = videoDanmu.getText().trim();
        if (StringTools.isEmpty(text) || text.length() > 300) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        videoDanmu.setText(text);
    }

    private VideoInfoDTO getVideoInfoWithCache(String videoId) {
        String redisKey = Constants.REDIS_KEY_VIDEO_INFO_CACHE + videoId;
        String videoInfoJson = redisUtils.get(redisKey);
        if (!StringTools.isEmpty(videoInfoJson)) {
            return JsonUtils.convertJson2Obj(videoInfoJson, VideoInfoDTO.class);
        }

        VideoInfoDTO videoInfo = Optional.ofNullable(videoInfoClient.getVideoInfoByVideoId(videoId))
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));
        redisUtils.setex(redisKey,
                JsonUtils.convertObj2Json(videoInfo),
                Constants.REDIS_VIDEO_DANMU_CACHE_TTL_MINUTES * Constants.REDIS_EXPIRE_TIME_ONE_MINUTE);
        return videoInfo;
    }

    private boolean checkDanmuOpenWithCache(String videoId) {
        String redisKey = Constants.REDIS_KEY_VIDEO_DANMU_STATUS_CACHE + videoId;
        String cacheValue = redisUtils.get(redisKey);

        if (!StringTools.isEmpty(cacheValue)) {
            return Constants.ONE.toString().equals(cacheValue);
        }

        boolean danmuOpen = checkDanmuOpen(videoId);
        redisUtils.setex(redisKey,
                danmuOpen ? Constants.ONE.toString() : Constants.ZERO.toString(),
                Constants.REDIS_VIDEO_DANMU_CACHE_TTL_MINUTES * Constants.REDIS_EXPIRE_TIME_ONE_MINUTE);
        return danmuOpen;
    }

    private VideoDanmuPostEvent buildDanmuPostEvent(VideoDanmu videoDanmu, VideoInfoDTO videoInfo) {
        VideoDanmuPostEvent event = new VideoDanmuPostEvent();
        // eventId 同时承担 MQ 幂等和数据库唯一键职责，不能由前端传入。
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setVideoId(videoDanmu.getVideoId());
        // 视频作者必须以后端查询结果为准，前端传归属字段会带来伪造风险。
        event.setVideoUserId(videoInfo.getUserId());
        event.setFileId(videoDanmu.getFileId());
        event.setUserId(videoDanmu.getUserId());
        event.setPostTime(new Date());
        event.setText(videoDanmu.getText());
        event.setMode(videoDanmu.getMode());
        event.setColor(videoDanmu.getColor());
        event.setTime(videoDanmu.getTime());
        return event;
    }

    private VideoDanmu buildCacheDanmu(VideoDanmuPostEvent event) {
        VideoDanmu videoDanmu = new VideoDanmu();
        videoDanmu.setEventId(event.getEventId());
        videoDanmu.setVideoId(event.getVideoId());
        videoDanmu.setVideoUserId(event.getVideoUserId());
        videoDanmu.setFileId(event.getFileId());
        videoDanmu.setUserId(event.getUserId());
        videoDanmu.setPostTime(event.getPostTime());
        videoDanmu.setText(event.getText());
        videoDanmu.setMode(event.getMode());
        videoDanmu.setColor(event.getColor());
        videoDanmu.setTime(event.getTime());
        return videoDanmu;
    }

    private boolean isVideoOwner(String videoId, String userId) {
        if (videoId == null || userId == null) {
            return false;
        }
        VideoInfoDTO videoInfo = Optional.ofNullable(videoInfoClient.getVideoInfoByVideoId(videoId))
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));
        return userId.equals(videoInfo.getUserId());
    }
}
