package com.mybilibili.video.services;

import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.entity.dto.VideoCountDTO;
import com.mybilibili.video.entity.dto.VideoCountUpdateDTO;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息Service
 */
public interface VideoInfoService {

	/**
	 * @description 根据条件查询
	 */
	List<VideoInfo> findListByParam(VideoInfoQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param);

	/**
	 * @description 新增
	 */
	Integer add(VideoInfo bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<VideoInfo>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoInfo> listBean);


	/**
	 * @description 根据 VideoId查询
	 */
	VideoInfo getVideoInfoByVideoId(String videoId);

	/**
	 * @description 根据 VideoId更新
	 */
	Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId);

	/**
	 * @description 根据 VideoId删除
	 */
	Integer deleteVideoInfoByVideoId(String videoId);

    Integer updateByCondition(VideoInfo videoInfo, VideoInfoQuery videoInfoQuery);
	Integer addOrUpdate(VideoInfo videoInfo);

	void auditVideo(String videoId, Integer status, String reason);

	Integer reportVideoPlayOnline(String fileId, String deviceId);

    VideoCountDTO sumVideoCountByUserId(String userId);

    List<VideoInfo> selectByIds(List<String> userCollectionIds);

	List<VideoInfo> selectVideoListBySeriesIdAndUserId(Integer seriesId, String userId);

	/**
	 * 根据字段批量更新数量
	 * @param field 需要更新字段
	 * @param list  要更新的集合列表
	 * @return 返回是否更新成功
	 */
	Integer updateCountBatch(@Param("field") String field, @Param("list") List<VideoCountUpdateDTO> list);

	void recommendVideo(String videoId);
}
