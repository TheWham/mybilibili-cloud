package com.mybilibili.interact.services.impl;

import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.component.UserDailyLimitComponent;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;
import com.mybilibili.interact.mappers.VideoDanmuMapper;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

        // TODO 后续调用 video 服务查询 UP 主信息后，补回“UP 主可删除自己视频下弹幕”的权限。
        boolean canDirectDelete = Boolean.TRUE.equals(isAdmin) || danmu.getUserId().equals(userId);
        if (!canDirectDelete) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return videoDanmuMapper.deleteByDanmuId(danmuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postDanmu(VideoDanmu videoDanmu) {
        VideoInfoDTO videoInfo = Optional.ofNullable(videoInfoClient.getVideoInfoByVideoId(videoDanmu.getVideoId()))
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));
        // 视频作者必须以后端查询结果为准，前端传归属字段会带来伪造风险。
        videoDanmu.setVideoUserId(videoInfo.getUserId());
        // TODO 后续改为调用 video 服务内部接口校验弹幕是否开启。
        // TODO 后续通过 video/search 服务或消息队列更新视频弹幕数和 ES 计数字段。
        add(videoDanmu);
        // TODO 后续接入日限额成功链路后，保留“写入成功再记录”的顺序。
        // userDailyLimitComponent.recordDailyAction(videoDanmu.getUserId(), UserDailyLimitTypeEnum.DANMU);
    }

    @Override
    public List<VideoDanmu> loadDanmu(String fileId, String videoId) {
        // TODO 后续改为调用 video 服务内部接口校验弹幕开关；当前只按本地弹幕表加载。
        VideoDanmuQuery danmuQuery = new VideoDanmuQuery();
        danmuQuery.setFileId(fileId);
        danmuQuery.setVideoId(videoId);
        return findListByParam(danmuQuery);
    }
}
