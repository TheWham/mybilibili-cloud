package com.mybilibili.message.controller;

import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.message.entity.po.UserMessage;
import com.mybilibili.message.entity.query.UserMessageQuery;
import com.mybilibili.message.entity.vo.MessageNoticeVO;
import com.mybilibili.message.entity.vo.MessageTypeDataVO;
import com.mybilibili.message.enums.MessageReadTypeEnum;
import com.mybilibili.message.services.UserMessageService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表Service
 */

@RestController
@RequestMapping("message")
public class UserMessageController extends ABaseController {
	@Resource
	private UserMessageService userMessageService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("loadMessage")
	public ResponseVO loadMessage (Integer pageNo, @NotNull Integer messageType)
	{
		UserMessageQuery userMessageQuery = new UserMessageQuery();
		userMessageQuery.setPageNo(pageNo);
		userMessageQuery.setMessageType(messageType);
		userMessageQuery.setOrderBy("create_time desc");
		// 收件箱只能查当前登录用户自己的消息。
		userMessageQuery.setUserId(getTokenUserInfo().getUserId());
		PaginationResultVO<UserMessage> page = userMessageService.findListByPage(userMessageQuery);

		// 分页先拿 message 主表，再补发件人、封面和扩展字段这些展示信息。
		// 同一类消息列表会把 messageType 一起透传下去，service 侧可以直接走批量组装逻辑。
		List<MessageNoticeVO> messageNoticeVOList = userMessageService.fullCompleteInfo(page.getList(), messageType);
		PaginationResultVO<MessageNoticeVO> result = new PaginationResultVO<>(page.getTotalCount(), page.getPageSize(), page.getPageNo(), page.getPageTotal(), messageNoticeVOList);
		return getSuccessResponseVO(result);
	}

	/**
	 * 查询未读信息数量
	 */

	@RequestMapping("getNoReadCount")
	public ResponseVO getNoReadCount()
	{
		UserMessageQuery messageQuery = new UserMessageQuery();
		messageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
		messageQuery.setUserId(getTokenUserInfo().getUserId());
		return getSuccessResponseVO(userMessageService.getNoReadMessageCount(messageQuery));
	}

	@RequestMapping("getNoReadCountGroup")
	public ResponseVO getNoReadCountGroup()
	{
		UserMessageQuery messageQuery = new UserMessageQuery();
		messageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
		messageQuery.setUserId(getTokenUserInfo().getUserId());
		// 未读分组只需要类型和数量，直接让数据库聚合，避免用户未读多时把整批消息拉进内存。
		List<MessageTypeDataVO> dataVOList = userMessageService.getNoReadMessageCountGroup(messageQuery);
		return getSuccessResponseVO(dataVOList);
	}

	@RequestMapping("readAll")
	public ResponseVO readAll(@NotNull Integer messageType)
	{
		UserMessageQuery userMessageQuery = new UserMessageQuery();
		userMessageQuery.setUserId(getTokenUserInfo().getUserId());
		userMessageQuery.setMessageType(messageType);
		userMessageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
		Integer count = userMessageService.updateReadStatsBatch(userMessageQuery);
		return getSuccessResponseVO(count);
	}

}
