package com.mybilibili.message.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import com.mybilibili.message.entity.vo.MessageTypeDataVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
	Integer deleteByMessageIdAndUserId(@Param("messageId") Integer messageId, @Param("userId") String userId);

	Integer updateReadStatsBatch(@Param("query") R userMessageQuery);

	/**
	 * 按消息类型统计未读数量。
	 *
	 * @param userMessageQuery 统计条件，业务层会限定当前登录用户和未读状态
	 * @return 每类消息的未读数量
	 */
	List<MessageTypeDataVO> selectNoReadCountGroup(@Param("query") R userMessageQuery);

	T selectLatestByNoticeKey(@Param("userId") String userId,
							  @Param("messageType") Integer messageType,
							  @Param("sendUserId") String sendUserId,
							  @Param("videoId") String videoId);
}
