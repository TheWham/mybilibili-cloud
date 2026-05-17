package com.mybilibili.ai.service.impl;

import com.mybilibili.ai.config.AiProperties;
import com.mybilibili.ai.constants.AiConstants;
import com.mybilibili.ai.entity.dto.AiChatMessageDTO;
import com.mybilibili.ai.entity.dto.AiChatSessionDTO;
import com.mybilibili.ai.entity.dto.AiChatSessionListDTO;
import com.mybilibili.ai.entity.dto.AiConversationContextDTO;
import com.mybilibili.ai.entity.vo.AiChatResultVO;
import com.mybilibili.ai.entity.vo.AiChatSessionDetailVO;
import com.mybilibili.ai.entity.vo.AiChatSessionSummaryVO;
import com.mybilibili.ai.service.AiChatSessionService;
import com.mybilibili.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis 版 AI 会话服务。
 *
 * <p>会话详情保存完整消息时间线，用户索引用 ZSet 维护最近更新时间排序。
 * 这样刷新页面时可以恢复整段对话，也能继续复用最近一轮上下文。</p>
 */
@Service("aiChatSessionService")
public class AiChatSessionServiceImpl implements AiChatSessionService {

    private static final Logger log = LoggerFactory.getLogger(AiChatSessionServiceImpl.class);
    private static final int SESSION_TITLE_MAX_LENGTH = 30;
    private static final int SESSION_LIST_MAX_COUNT = 20;

    @Resource
    private RedisUtils<Object> redisUtils;
    @Resource
    private AiProperties aiProperties;

    @Override
    public AiChatSessionDTO getSession(String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            return null;
        }
        Object cacheValue = redisUtils.get(buildSessionKey(conversationId));
        if (cacheValue instanceof AiChatSessionDTO sessionDTO) {
            return sessionDTO;
        }
        if (cacheValue != null) {
            log.warn("AI 会话缓存类型异常, conversationId={}, cacheType={}",
                    conversationId, cacheValue.getClass().getName());
        }
        return null;
    }

    @Override
    public AiConversationContextDTO getContext(String conversationId) {
        AiChatSessionDTO sessionDTO = getSession(conversationId);
        return sessionDTO == null ? null : sessionDTO.getContext();
    }

    @Override
    public void saveContext(String conversationId, AiConversationContextDTO context) {
        if (StringUtils.isBlank(conversationId) || context == null) {
            return;
        }
        AiChatSessionDTO sessionDTO = getSession(conversationId);
        if (sessionDTO == null) {
            sessionDTO = new AiChatSessionDTO();
            sessionDTO.setConversationId(conversationId);
        }
        sessionDTO.setConversationId(conversationId);
        sessionDTO.setContext(context);
        sessionDTO.setUpdateTime(System.currentTimeMillis());
        boolean success = redisUtils.setex(
                buildSessionKey(conversationId),
                sessionDTO,
                aiProperties.getChat().getSessionExpireMs()
        );
        if (!success) {
            log.warn("AI 会话写入 Redis 失败, conversationId={}", conversationId);
        }
    }

    @Override
    public AiChatSessionSummaryVO saveRound(String userId,
                                            String conversationId,
                                            String question,
                                            AiChatResultVO resultVO) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(conversationId) || resultVO == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        AiChatSessionDTO sessionDTO = getSession(conversationId);
        if (sessionDTO == null) {
            sessionDTO = new AiChatSessionDTO();
            sessionDTO.setConversationId(conversationId);
            sessionDTO.setUserId(userId);
            sessionDTO.setTitle(buildSessionTitle(question));
            sessionDTO.setMessages(new ArrayList<>());
        }
        if (StringUtils.isBlank(sessionDTO.getUserId())) {
            sessionDTO.setUserId(userId);
        }
        if (!StringUtils.equals(sessionDTO.getUserId(), userId)) {
            log.warn("AI 会话归属不匹配，拒绝写入, conversationId={}, requestUserId={}, ownerUserId={}",
                    conversationId, userId, sessionDTO.getUserId());
            return null;
        }
        if (StringUtils.isBlank(sessionDTO.getTitle())) {
            sessionDTO.setTitle(buildSessionTitle(question));
        }
        List<AiChatMessageDTO> messages = sessionDTO.getMessages();
        if (messages == null) {
            messages = new ArrayList<>();
            sessionDTO.setMessages(messages);
        }
        messages.add(buildUserMessage(question, now));
        messages.add(buildAssistantMessage(resultVO, now + 1));
        sessionDTO.setContext(resultVO.getContext());
        sessionDTO.setLastQuestion(question);
        sessionDTO.setLastAnswer(resultVO.getAnswer());
        sessionDTO.setUpdateTime(now);

        long expireMs = aiProperties.getChat().getSessionExpireMs();
        boolean success = redisUtils.setex(buildSessionKey(conversationId), sessionDTO, expireMs);
        if (!success) {
            log.warn("AI 完整会话写入 Redis 失败, conversationId={}, userId={}", conversationId, userId);
            return null;
        }
        redisUtils.zadd(buildUserSessionKey(userId), conversationId, now, expireMs);
        return buildSummary(sessionDTO);
    }

    @Override
    public AiChatSessionListDTO listUserSessions(String userId) {
        AiChatSessionListDTO result = new AiChatSessionListDTO();
        result.setUserId(userId);
        if (StringUtils.isBlank(userId)) {
            result.setSessions(Collections.emptyList());
            return result;
        }
        List<Object> conversationIds = redisUtils.getZSetList(buildUserSessionKey(userId), SESSION_LIST_MAX_COUNT - 1);
        if (conversationIds == null || conversationIds.isEmpty()) {
            result.setSessions(Collections.emptyList());
            return result;
        }
        List<AiChatSessionSummaryVO> sessions = new ArrayList<>(conversationIds.size());
        for (Object item : conversationIds) {
            String conversationId = item == null ? null : String.valueOf(item);
            AiChatSessionDTO sessionDTO = getSession(conversationId);
            if (sessionDTO == null) {
                redisUtils.zremove(buildUserSessionKey(userId), conversationId);
                continue;
            }
            if (!StringUtils.equals(userId, sessionDTO.getUserId())) {
                continue;
            }
            sessions.add(buildSummary(sessionDTO));
        }
        result.setSessions(sessions);
        return result;
    }

    @Override
    public AiChatSessionDetailVO getSessionDetail(String userId, String conversationId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(conversationId)) {
            return null;
        }
        AiChatSessionDTO sessionDTO = getSession(conversationId);
        if (sessionDTO == null || !StringUtils.equals(userId, sessionDTO.getUserId())) {
            return null;
        }
        AiChatSessionDetailVO detailVO = new AiChatSessionDetailVO();
        detailVO.setConversationId(sessionDTO.getConversationId());
        detailVO.setTitle(sessionDTO.getTitle());
        detailVO.setMessages(sessionDTO.getMessages() == null ? Collections.emptyList() : sessionDTO.getMessages());
        detailVO.setContext(sessionDTO.getContext());
        detailVO.setUpdateTime(sessionDTO.getUpdateTime());
        return detailVO;
    }

    @Override
    public void deleteSession(String userId, String conversationId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(conversationId)) {
            return;
        }
        String userSessionKey = buildUserSessionKey(userId);
        AiChatSessionDTO sessionDTO = getSession(conversationId);
        if (sessionDTO == null) {
            redisUtils.zremove(userSessionKey, conversationId);
            return;
        }
        if (!StringUtils.equals(userId, sessionDTO.getUserId())) {
            log.warn("AI 会话删除被拒绝, conversationId={}, requestUserId={}, ownerUserId={}",
                    conversationId, userId, sessionDTO.getUserId());
            return;
        }
        redisUtils.delete(buildSessionKey(conversationId));
        redisUtils.zremove(userSessionKey, conversationId);
    }

    private AiChatMessageDTO buildUserMessage(String question, long createdAt) {
        AiChatMessageDTO messageDTO = new AiChatMessageDTO();
        messageDTO.setId(AiConstants.CHAT_MESSAGE_ROLE_USER + "_" + createdAt);
        messageDTO.setRole(AiConstants.CHAT_MESSAGE_ROLE_USER);
        messageDTO.setText(StringUtils.defaultString(question));
        messageDTO.setVideos(Collections.emptyList());
        messageDTO.setSuggestions(Collections.emptyList());
        messageDTO.setSuggestionActions(Collections.emptyList());
        messageDTO.setCreatedAt(createdAt);
        return messageDTO;
    }

    private AiChatMessageDTO buildAssistantMessage(AiChatResultVO resultVO, long createdAt) {
        AiChatMessageDTO messageDTO = new AiChatMessageDTO();
        messageDTO.setId(AiConstants.CHAT_MESSAGE_ROLE_ASSISTANT + "_" + createdAt);
        messageDTO.setRole(AiConstants.CHAT_MESSAGE_ROLE_ASSISTANT);
        messageDTO.setText(resultVO.getAnswer());
        messageDTO.setVideos(resultVO.getVideos() == null ? Collections.emptyList() : resultVO.getVideos());
        messageDTO.setSuggestions(resultVO.getSuggestions() == null ? Collections.emptyList() : resultVO.getSuggestions());
        messageDTO.setSuggestionActions(resultVO.getSuggestionActions() == null
                ? Collections.emptyList()
                : resultVO.getSuggestionActions());
        messageDTO.setQueryAnalysis(resultVO.getQueryAnalysis());
        messageDTO.setCreatedAt(createdAt);
        return messageDTO;
    }

    private AiChatSessionSummaryVO buildSummary(AiChatSessionDTO sessionDTO) {
        AiChatSessionSummaryVO summaryVO = new AiChatSessionSummaryVO();
        summaryVO.setConversationId(sessionDTO.getConversationId());
        summaryVO.setTitle(sessionDTO.getTitle());
        summaryVO.setLastQuestion(sessionDTO.getLastQuestion());
        summaryVO.setLastAnswer(sessionDTO.getLastAnswer());
        summaryVO.setUpdateTime(sessionDTO.getUpdateTime());
        return summaryVO;
    }

    private String buildSessionTitle(String question) {
        String title = StringUtils.trimToEmpty(question);
        if (StringUtils.isBlank(title)) {
            return "新的 AI 会话";
        }
        if (title.length() <= SESSION_TITLE_MAX_LENGTH) {
            return title;
        }
        return title.substring(0, SESSION_TITLE_MAX_LENGTH) + "...";
    }

    private String buildSessionKey(String conversationId) {
        return AiConstants.REDIS_KEY_AI_CHAT_SESSION + conversationId;
    }

    private String buildUserSessionKey(String userId) {
        return AiConstants.REDIS_KEY_AI_CHAT_USER_SESSIONS + userId;
    }
}
