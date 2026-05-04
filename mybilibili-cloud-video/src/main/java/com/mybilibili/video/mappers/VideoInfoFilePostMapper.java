package com.mybilibili.video.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息
 */
public interface VideoInfoFilePostMapper<T, R> extends BaseMapper {

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


	/**
	 * @description 根据 UploadIdAndUserId查询
	 */
	T selectByUploadIdAndUserId(@Param("uploadId") String uploadId, @Param("userId") String userId);

	/**
	 * @description 根据 UploadIdAndUserId更新
	 */
	Integer updateByUploadIdAndUserId(@Param("bean") T t, @Param("uploadId") String uploadId, @Param("userId") String userId);

	/**
	 * @description 根据 UploadIdAndUserId删除
	 */
	Integer deleteByUploadIdAndUserId(@Param("uploadId") String uploadId, @Param("userId") String userId);

    Integer getSumDuration(@Param("videoId") String videoId);

	Integer updateByCondition(@Param("bean")T videoInfoFilePost,@Param("query") R filePostQuery);

    Integer deleteByCondition(@Param("query") R videoInfoFilePostQuery);

    Integer delBatchByIds(@Param("fileIds") List<String> deleteListIds,@Param("userId") String userId);
}
