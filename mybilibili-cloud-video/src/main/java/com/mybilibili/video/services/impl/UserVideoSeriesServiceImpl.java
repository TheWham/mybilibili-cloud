package com.mybilibili.video.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.entity.dto.UserVideoSeriesDTO;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.video.entity.dto.SeriesWithVideoQueryDTO;
import com.mybilibili.video.entity.po.UserVideoSeries;
import com.mybilibili.video.entity.po.UserVideoSeriesVideo;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.query.UserVideoSeriesQuery;
import com.mybilibili.video.entity.query.UserVideoSeriesVideoQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.mappers.UserVideoSeriesMapper;
import com.mybilibili.video.mappers.UserVideoSeriesVideoMapper;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.services.UserVideoSeriesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户视频合集服务。
 *
 * <p>合集的增删改仍使用 video 内部 PO；用户主页展示查询使用 QueryDTO，
 * 避免 Mapper 直接返回跨服务 VO。</p>
 */
@Service("UserVideoSeriesService")
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {

    @Resource
    private UserVideoSeriesMapper<UserVideoSeries, UserVideoSeriesQuery> userVideoSeriesMapper;
    @Resource
    private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> userVideoSeriesVideoMapper;
    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Override
    public List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param) {
        return userVideoSeriesMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(UserVideoSeriesQuery param) {
        return userVideoSeriesMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param) {
        Integer count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserVideoSeries> list = findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(UserVideoSeries bean) {
        return userVideoSeriesMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<UserVideoSeries> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userVideoSeriesMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<UserVideoSeries> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userVideoSeriesMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId) {
        return userVideoSeriesMapper.selectBySeriesId(seriesId);
    }

    @Override
    public Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId) {
        return userVideoSeriesMapper.updateBySeriesId(bean, seriesId);
    }

    @Override
    public Integer deleteUserVideoSeriesBySeriesIdAndUserId(Integer seriesId, String userId) {
        return userVideoSeriesMapper.deleteBySeriesIdAndUserId(seriesId, userId);
    }

    @Override
    public List<VideoInfo> selectAllVideoBySeriesIdAndUserId(Integer seriesId, String userId) {
        return videoInfoMapper.selectVideoListBySeriesIdAndUserId(seriesId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveVideoSeries(Integer seriesId, String seriesName, String seriesDescription, String videoIds, String userId) {
        if (seriesId == null && StringTools.isEmpty(videoIds)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String[] videoIdList = null;
        if (!StringTools.isEmpty(videoIds)) {
            videoIdList = videoIds.split(",");
            checkVideoIdsValid(userId, videoIdList);
        }

        UserVideoSeries userVideoSeries = new UserVideoSeries();
        userVideoSeries.setSeriesDescription(seriesDescription);
        userVideoSeries.setSeriesName(seriesName);
        userVideoSeries.setUserId(userId);
        userVideoSeries.setSeriesId(seriesId);
        userVideoSeries.setUpdateTime(new Date());
        Integer maxSort = userVideoSeriesMapper.selectMaxSort(userId);
        userVideoSeries.setSort(maxSort + 1);

        if (seriesId == null) {
            userVideoSeriesMapper.insert(userVideoSeries);
            saveUserVideoSeriesVideo(userVideoSeries.getSeriesId(), videoIdList, userId);
            return;
        }

        UserVideoSeries oldVideoSeries = userVideoSeriesMapper.selectBySeriesId(seriesId);
        if (oldVideoSeries == null || !oldVideoSeries.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        userVideoSeriesMapper.insertOrUpdate(userVideoSeries);
    }

    @Override
    public void saveSeriesVideo(Integer seriesId, Integer sort, String videoIds, String userId) {
        if (StringTools.isEmpty(videoIds)) {
            return;
        }

        String[] videoIdList = videoIds.split(",");
        if (sort == null) {
            Integer newSort = 0;
            List<UserVideoSeriesVideo> updateList = new ArrayList<>(videoIdList.length);
            for (String videoId : videoIdList) {
                UserVideoSeriesVideo seriesVideo = buildSeriesVideo(seriesId, videoId, userId, ++newSort);
                updateList.add(seriesVideo);
            }
            userVideoSeriesVideoMapper.insertOrUpdateBatch(updateList);
            return;
        }

        List<UserVideoSeriesVideo> addList = new ArrayList<>(videoIdList.length);
        for (String videoId : videoIdList) {
            addList.add(buildSeriesVideo(seriesId, videoId, userId, ++sort));
        }
        userVideoSeriesVideoMapper.insertBatch(addList);
    }

    @Override
    public List<UserVideoSeriesDTO> loadVideoSeries(String userId) {
        List<UserVideoSeries> userVideoSeries = userVideoSeriesMapper.loadVideoSeries(userId);
        List<UserVideoSeriesDTO> userVideoSeriesDTOS = BeanUtil.copyToList(userVideoSeries, UserVideoSeriesDTO.class);
        return userVideoSeriesDTOS;
    }

    @Override
    public void changeVideoSeriesSort(String videoSeriesIds, String userId) {
        if (StringTools.isEmpty(videoSeriesIds)) {
            return;
        }

        String[] ids = videoSeriesIds.split(",");
        Integer[] seriesIds = Arrays.stream(ids).map(Integer::parseInt).toArray(Integer[]::new);
        Integer sort = 0;
        List<UserVideoSeries> updateList = new ArrayList<>(seriesIds.length);
        for (Integer seriesId : seriesIds) {
            UserVideoSeries userVideoSeries = new UserVideoSeries();
            userVideoSeries.setSeriesId(seriesId);
            userVideoSeries.setSort(++sort);
            updateList.add(userVideoSeries);
        }
        userVideoSeriesMapper.updateSeriesSortBatch(updateList, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer delVideoSeries(Integer seriesId, String userId) {
        UserVideoSeriesVideoQuery seriesVideoQuery = new UserVideoSeriesVideoQuery();
        seriesVideoQuery.setSeriesId(seriesId);
        seriesVideoQuery.setUserId(userId);
        List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoMapper.selectList(seriesVideoQuery);

        if (seriesVideoList != null && !seriesVideoList.isEmpty()) {
            List<String> videoIdList = seriesVideoList.stream()
                    .map(UserVideoSeriesVideo::getVideoId)
                    .collect(Collectors.toList());
            userVideoSeriesVideoMapper.deleteByIds(videoIdList, userId, seriesId);
        }

        return userVideoSeriesMapper.deleteBySeriesIdAndUserId(seriesId, userId);
    }

    @Override
    public List<SeriesWithVideoQueryDTO> selectVideoSeriesWithVideo(String userId) {
        return userVideoSeriesMapper.selectVideoSeriesWithVideo(userId);
    }

    private void checkVideoIdsValid(String userId, String[] videoIdList) {
        if (videoIdList == null || videoIdList.length == 0) {
            return;
        }
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setArrayIds(videoIdList);
        Integer count = videoInfoMapper.selectCount(videoInfoQuery);
        if (count == null || count != videoIdList.length) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    private void saveUserVideoSeriesVideo(Integer seriesId, String[] videoIdList, String userId) {
        if (seriesId == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (videoIdList == null || videoIdList.length == 0) {
            throw new BusinessException("未发布视频");
        }

        List<String> videoIdsInDb = userVideoSeriesVideoMapper.selectVideoIdsBySeriesIdAndUserId(seriesId, userId);
        List<String> newAddVideoIdList = Arrays.asList(videoIdList);
        List<String> newVideoIds = newAddVideoIdList.stream()
                .filter(videoId -> !videoIdsInDb.contains(videoId))
                .distinct()
                .collect(Collectors.toList());
        if (newVideoIds.isEmpty()) {
            throw new BusinessException("所选视频已添加");
        }

        Integer maxSort = userVideoSeriesVideoMapper.selectMaxSort(userId);
        List<UserVideoSeriesVideo> seriesVideoList = new ArrayList<>(newVideoIds.size());
        for (String videoId : newVideoIds) {
            seriesVideoList.add(buildSeriesVideo(seriesId, videoId, userId, ++maxSort));
        }
        userVideoSeriesVideoMapper.insertBatch(seriesVideoList);
    }

    private UserVideoSeriesVideo buildSeriesVideo(Integer seriesId, String videoId, String userId, Integer sort) {
        UserVideoSeriesVideo seriesVideo = new UserVideoSeriesVideo();
        seriesVideo.setSeriesId(seriesId);
        seriesVideo.setVideoId(videoId);
        seriesVideo.setUserId(userId);
        seriesVideo.setSort(sort);
        return seriesVideo;
    }
}
