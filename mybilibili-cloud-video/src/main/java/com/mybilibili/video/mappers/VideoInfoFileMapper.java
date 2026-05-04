package com.mybilibili.video.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import com.mybilibili.video.entity.query.VideoInfoFileQuery;
import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息
 */
public interface VideoInfoFileMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 FileId查询
	 */
	T selectByFileId(@Param("fileId") String fileId);

	/**
	 * @description 根据 FileId更新
	 */
	Integer updateByFileId(@Param("bean") T t, @Param("fileId") String fileId);

	/**
	 * @description 根据 FileId删除
	 */
	Integer deleteByFileId(@Param("fileId") String fileId);

    Integer deleteByCondition(@Param("query") VideoInfoFileQuery videoInfoFileQuery);
}
