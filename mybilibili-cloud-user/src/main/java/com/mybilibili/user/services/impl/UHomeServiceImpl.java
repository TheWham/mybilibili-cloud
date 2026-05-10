package com.mybilibili.user.services.impl;

import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.SeriesWithVideoUHomeVO;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import com.mybilibili.base.entity.vo.UserVideoSeriesVO;
import com.mybilibili.base.entity.vo.VideoInfoUHomeVO;
import com.mybilibili.user.consumer.VideoInfoClient;
import com.mybilibili.user.services.UHomeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UHomeServiceImpl implements UHomeService {

    @Resource
    private VideoInfoClient videoInfoClient;

    @Override
    public PaginationResultVO<VideoInfoUHomeVO> loadVideoList(String userId, Integer type, Integer pageNo, String videoName, Integer orderType) {
        return videoInfoClient.loadVideoList(userId, type, pageNo, videoName, orderType);
    }

    @Override
    public List<SeriesWithVideoUHomeVO> loadVideoSeriesWithVideo(String userId) {
        return videoInfoClient.loadVideoSeriesWithVideo(userId);
    }

    @Override
    public List<UserVideoSeriesVO> loadVideoSeries(String userId) {
        return videoInfoClient.loadVideoSeries(userId);
    }

    @Override
    public PaginationResultVO<UserCollectionVO> loadUserCollection(Integer pageNo, String userId) {
        return videoInfoClient.loadUserCollection(pageNo, userId);
    }
}
