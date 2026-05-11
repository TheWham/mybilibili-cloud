package com.mybilibili.video.mappers;
import com.mybilibili.common.mappers.BaseMapper;
import com.mybilibili.base.entity.dto.VideoCountDTO;
import com.mybilibili.video.entity.dto.VideoCountUpdateDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息
 */

public interface VideoInfoMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 VideoId查询
	 */
	T selectByVideoId(@Param("videoId") String videoId);

	/**
	 * @description 根据 VideoId更新
	 */
	Integer updateByVideoId(@Param("bean") T t, @Param("videoId") String videoId);

	/**
	 * @description 根据 VideoId删除
	 */
	Integer deleteByVideoId(@Param("videoId") String videoId);

	/**
	 * @param videoInfo
	 * @param videoInfoQuery
	 * @return
	 */

    Integer updateByCondition(@Param("bean") T videoInfo,@Param("query") R videoInfoQuery);

    Integer updateCount(@Param("videoId") String videoId,@Param("field") String field, @Param("count")Integer count);

    Integer updateCountBatch(@Param("field") String field, @Param("list") List<VideoCountUpdateDTO> list);

	String selectUserIdByVideoId(@Param("videoId") String videoId);

    VideoCountDTO sumVideoCountByUserId(@Param("userId") String userId);

    List<T> selectByIds(@Param("ids") List<String> userCollectionIds);

    List<T> selectVideoListBySeriesIdAndUserId(@Param("seriesId") Integer seriesId,@Param("userId") String userId);
}
