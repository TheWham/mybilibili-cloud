package com.mybilibili.user.services.impl;

import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.*;
import com.mybilibili.user.consumer.InteractClient;
import com.mybilibili.user.consumer.VideoInfoClient;
import com.mybilibili.user.services.UCenterService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.nacos.client.utils.EnvUtil.LOGGER;

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
        PaginationResultVO<VideoCommentInUCenterVO> videoCommentVOList = interactClient.loadCommentInUCenter(pageNo, pageSize, videoId, userId);
        fillCommentVideoInfo(videoCommentVOList.getList(), userId);
        return videoCommentVOList;
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

    private void fillCommentVideoInfo(List<VideoCommentInUCenterVO> commentList, String userId) {
        if (commentList == null || commentList.isEmpty()) {
            return;
        }

        Set<String> videoIds = new HashSet<>();

        for (VideoCommentInUCenterVO comment : commentList) {
            addVideoId(videoIds, comment.getVideoId());
        }

        if (videoIds.isEmpty()) {
            return;
        }

        List<VideoInfoDTO> videoInfoList;

        try {
            videoInfoList = videoInfoClient.getVideoListByIds(new ArrayList<>(videoIds), userId);
        } catch (Exception e) {
            // 用户信息只是评论列表的展示增强，user 服务短暂不可用时不应该影响评论内容返回。
            LOGGER.warn("批量查询评论视频信息失败，videoIdCount:{}", videoIds.size(), e);
            return;
        }

        if (videoInfoList == null || videoInfoList.isEmpty()) {
            return;
        }

        Map<String, VideoInfoDTO> videoInfoMap = videoInfoList.stream()
                .filter(v -> v.getVideoId() != null)
                .collect(Collectors.toMap(VideoInfoDTO::getVideoId, VideoInfoDTO -> VideoInfoDTO, (oldValue, newValue) -> oldValue));

        for (VideoCommentInUCenterVO comment : commentList) {
            fillSingleCommentVideoInfo(comment, videoInfoMap);
        }

    }

    private void fillSingleCommentVideoInfo(VideoCommentInUCenterVO comment, Map<String, VideoInfoDTO> videoInfoDTOMap) {
        if (comment == null) {
            return;
        }

        VideoInfoDTO videoInfoDTO = videoInfoDTOMap.get(comment.getVideoId());

        if (videoInfoDTO != null) {
            comment.setVideoName(videoInfoDTO.getVideoName());
            comment.setVideoCover(videoInfoDTO.getVideoCover());
            comment.setVideoId(videoInfoDTO.getVideoId());
        }
    }

    private void addVideoId(Set<String> userIds, String userId) {
        if (userId != null && !userId.isBlank()) {
            userIds.add(userId);
        }
    }

}
