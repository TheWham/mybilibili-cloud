package com.mybilibili.video.services;

import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.query.VideoInfoFilePostQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息Service
 */
public interface VideoInfoFilePostService {

	/**
	 * @description 根据条件查询
	 */
	List<VideoInfoFilePost> findListByParam(VideoInfoFilePostQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoFilePostQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<VideoInfoFilePost> findListByPage(VideoInfoFilePostQuery param);

	/**
	 * @description 新增
	 */
	Integer add(VideoInfoFilePost bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<VideoInfoFilePost>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoInfoFilePost> listBean);


	/**
	 * @description 根据 FileId查询
	 */
	VideoInfoFilePost getVideoInfoFilePostByFileId(String fileId);

	/**
	 * @description 根据 FileId更新
	 */
	Integer updateVideoInfoFilePostByFileId(VideoInfoFilePost bean, String fileId);

	/**
	 * @description 根据 FileId删除
	 */
	Integer deleteVideoInfoFilePostByFileId(String fileId);


	/**
	 * @description 根据 UploadIdAndUserId查询
	 */
	VideoInfoFilePost getVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId);

	/**
	 * @description 根据 UploadIdAndUserId更新
	 */
	Integer updateVideoInfoFilePostByUploadIdAndUserId(VideoInfoFilePost bean, String uploadId, String userId);

	/**
	 * @description 根据 UploadIdAndUserId删除
	 */
	Integer deleteVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId);

    void transferVideo(VideoInfoFilePost transferVideo);

    void deleVideo(String videoId, String userId, Boolean isAdmin);
}