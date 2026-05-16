package com.mybilibili.message.services;


import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.message.entity.po.UserMessage;
import com.mybilibili.message.entity.query.UserMessageQuery;
import com.mybilibili.message.entity.vo.MessageNoticeVO;
import com.mybilibili.message.entity.vo.MessageTypeDataVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表Service
 */
public interface UserMessageService {

	/**
	 * 根据条件查询
	 */
	List<UserMessage> findListByParam(UserMessageQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserMessageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param);

	/**
	 * 新增
	 */
	Integer add(UserMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserMessage>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserMessage> listBean);


	/**
	 * 根据 MessageId查询
	 */
	UserMessage getUserMessageByMessageId(Integer messageId);

	/**
	 * 根据 MessageId更新
	 */
	Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId);

	/**
	 * 根据 MessageId删除
	 */
	Integer deleteUserMessageByMessageId(Integer messageId);

	/**
	 * @param list 信息列表
	 * @param messageType 当前列表筛选的消息类型，首页混合流时可以为空
	 * @return 附加额外显示信息列表
	 */
    List<MessageNoticeVO> fullCompleteInfo(List<UserMessage> list, Integer messageType);

	/**
	 * @param messageQuery count查询条件
	 * @return 未读信息数量
	 */
	Integer getNoReadMessageCount(UserMessageQuery messageQuery);

	/**
	 * 按消息类型统计当前用户未读数量。
	 *
	 * <p>未读分组是消息中心的高频入口，直接在数据库侧 group by，避免把所有未读消息查到 JVM 里再聚合。</p>
	 *
	 * @param messageQuery 未读统计条件，必须包含 userId 和 readType
	 * @return 每种消息类型对应的未读数量
	 */
	List<MessageTypeDataVO> getNoReadMessageCountGroup(UserMessageQuery messageQuery);

	Integer updateReadStatsBatch(UserMessageQuery userMessageQuery);
}
