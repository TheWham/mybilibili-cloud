package com.mybilibili.message.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表
 */
public interface UserMessageMapper<T, R> extends BaseMapper {

	/**
	 * 根据 MessageId查询
	 */
	T selectByMessageId(@Param("messageId") Integer messageId);

	/**
	 * 根据 MessageId更新
	 */
	Integer updateByMessageId(@Param("bean") T t, @Param("messageId") Integer messageId);

	/**
	 * 根据 MessageId删除
	 */
	Integer deleteByMessageId(@Param("messageId") Integer messageId);

	Integer updateReadStatsBatch(@Param("query") R userMessageQuery);

	T selectLatestByNoticeKey(@Param("userId") String userId,
							  @Param("messageType") Integer messageType,
							  @Param("sendUserId") String sendUserId,
							  @Param("videoId") String videoId);
}
