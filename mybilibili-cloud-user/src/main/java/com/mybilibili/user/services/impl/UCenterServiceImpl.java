package com.mybilibili.user.services.impl;

import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.*;
import com.mybilibili.user.consumer.InteractClient;
import com.mybilibili.user.consumer.VideoInfoClient;
import com.mybilibili.user.services.UCenterService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UCenterServiceImpl implements UCenterService {

    @Resource
    private VideoInfoClient videoInfoClient;

    @Resource
    private InteractClient interactClient;

    @Override
    public PaginationResultVO<VideoInfoPostDTO> loadVideoList(Integer pageNo, String videoNameFuzzy, Integer status, String userId) {
        return videoInfoClient.loadUCenterVideoList(pageNo, videoNameFuzzy, status, userId);
    }

    @Override
    public VideoAuditCountVO getVideoCountInfo(String userId) {
        return videoInfoClient.getVideoCountInfo(userId);
    }

    @Override
    public VideoInfoPostEditVO getVideoByVideoId(String videoId, String userId) {
        return videoInfoClient.getVideoByVideoId(videoId, userId);
    }

    @Override
    public void postVideo(VideoInfoPostDTO videoInfoPostDTO, String userId) {
        videoInfoPostDTO.setUserId(userId);
        videoInfoClient.postVideo(videoInfoPostDTO);
    }

    @Override
    public void deleteVideo(String videoId, String userId) {
        videoInfoClient.deleteVideo(videoId, userId);
    }

    @Override
    public void saveVideoInteraction(String videoId, String interaction, String userId) {
        videoInfoClient.saveVideoInteraction(videoId, userId, interaction);
    }

    @Override
    public List<VideoInfoDTO> loadAllVideo(String userId) {
        return videoInfoClient.loadAllVideo(userId);
    }

    @Override
    public PaginationResultVO<VideoCommentInUCenterVO> loadComment(Integer pageNo, Integer pageSize, String videoId, String userId) {
        return interactClient.loadCommentInUCenter(pageNo, pageSize, videoId, userId);
    }

    @Override
    public PaginationResultVO<VideoDanmuVO> loadDanmu(Integer pageNo, Integer pageSize, String videoId, String userId) {
        return interactClient.loadDanmu(pageNo, pageSize, videoId, userId);
    }

    @Override
    public void delComment(Integer commentId, String userId) {
        interactClient.delComment(commentId, userId);
    }

    @Override
    public void delDanmu(Integer danmuId, String userId) {
        interactClient.delDanmu(danmuId, userId);
    }
}
