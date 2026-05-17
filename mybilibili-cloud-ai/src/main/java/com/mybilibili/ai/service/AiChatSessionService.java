package com.mybilibili.ai.service;

import com.mybilibili.ai.entity.dto.AiChatSessionDTO;
import com.mybilibili.ai.entity.dto.AiChatSessionListDTO;
import com.mybilibili.ai.entity.dto.AiConversationContextDTO;
import com.mybilibili.ai.entity.vo.AiChatResultVO;
import com.mybilibili.ai.entity.vo.AiChatSessionDetailVO;
import com.mybilibili.ai.entity.vo.AiChatSessionSummaryVO;

/**
 * AI 会话上下文服务。
 *
 * <p>负责把最近一轮对话上下文存到 Redis，供下一轮追问直接复用。</p>
 */
public interface AiChatSessionService {

    /**
     * 读取会话。
     *
     * @param conversationId 会话编号
     * @return 会话信息，不存在时返回 null
     */
    AiChatSessionDTO getSession(String conversationId);

    /**
     * 只读取最近一轮上下文。
     *
     * @param conversationId 会话编号
     * @return 上下文，不存在时返回 null
     */
    AiConversationContextDTO getContext(String conversationId);

    /**
     * 保存最近一轮上下文。
     *
     * @param conversationId 会话编号
     * @param context 最近一轮上下文
     */
    void saveContext(String conversationId, AiConversationContextDTO context);

    /**
     * 保存一轮完整问答消息。
     *
     * <p>登录用户的 AI 会话需要支持刷新后恢复完整时间线，所以这里不只保存最近一轮 context，
     * 还会把用户问题、AI 回答、命中视频和推荐问题一起追加到会话快照中。</p>
     *
     * @param userId 当前登录用户编号，匿名用户传空时直接忽略
     * @param conversationId 会话编号
     * @param question 用户本轮问题
     * @param resultVO AI 本轮回答结果
     * @return 会话摘要，未保存时返回 null
     */
    AiChatSessionSummaryVO saveRound(String userId, String conversationId, String question, AiChatResultVO resultVO);

    /**
     * 查询用户最近 AI 会话列表。
     *
     * @param userId 当前登录用户编号
     * @return 会话列表，未登录或没有历史时返回空列表对象
     */
    AiChatSessionListDTO listUserSessions(String userId);

    /**
     * 查询用户某个 AI 会话详情。
     *
     * <p>会校验会话归属，避免用户通过 conversationId 读取别人的会话。</p>
     *
     * @param userId 当前登录用户编号
     * @param conversationId 会话编号
     * @return 会话详情，不存在或无权限时返回 null
     */
    AiChatSessionDetailVO getSessionDetail(String userId, String conversationId);

    /**
     * 删除用户某个 AI 会话。
     *
     * <p>只允许删除当前用户自己的会话。未登录、会话不存在或归属不匹配时直接忽略，
     * 避免通过删除接口探测别人的 conversationId。</p>
     *
     * @param userId 当前登录用户编号
     * @param conversationId 会话编号
     */
    void deleteSession(String userId, String conversationId);
}
