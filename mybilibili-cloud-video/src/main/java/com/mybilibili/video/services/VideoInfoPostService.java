package com.mybilibili.video.services;

import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.entity.po.VideoInfoPost;
import com.mybilibili.video.entity.query.VideoInfoPostQuery;
import com.mybilibili.video.entity.vo.VideoAuditCountVO;

import java.util.List;


/**
 * @author amani
 * @date 2026/02/13
 * @description 视频信息Service
 */
public interface VideoInfoPostService {

	/**
	 * @description 根据条件查询
	 */
	List<VideoInfoPost> findListByParam(VideoInfoPostQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoPostQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param);

	/**
	 * @description 新增
	 */
	Integer add(VideoInfoPost bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<VideoInfoPost>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoInfoPost> listBean);


	/**
	 * @description 根据 VideoId查询
	 */
	VideoInfoPost getVideoInfoPostByVideoId(String videoId);

	/**
	 * @description 根据 VideoId更新
	 */
	Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId);

	/**
	 * @description 根据 VideoId删除
	 */
	Integer deleteVideoInfoPostByVideoId(String videoId);

	void savePostVideoInfo(VideoInfoPostDTO videoInfoPostDTO);

    Integer updateByCondition(VideoInfoPost updateInfoPost, VideoInfoPostQuery postQuery);

    VideoAuditCountVO getVideoCountInfo(String userId);

	void saveVideoInteraction(VideoInfoPost videoInfoPost);
}