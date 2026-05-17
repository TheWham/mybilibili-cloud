package com.mybilibili.ai.controller;

import com.mybilibili.ai.entity.dto.AiChatSessionDetailRequestDTO;
import com.mybilibili.ai.entity.dto.AiChatSessionDeleteRequestDTO;
import com.mybilibili.ai.entity.dto.AiChatRequestDTO;
import com.mybilibili.ai.service.AiChatService;
import com.mybilibili.ai.service.AiChatSessionService;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class AiChatController extends ABaseController {

    @Resource
    private AiChatService aiChatService;
    @Resource
    private AiChatSessionService aiChatSessionService;

    @PostMapping("/chat")
    public ResponseVO chat(@Valid @RequestBody AiChatRequestDTO request) {
        fillLoginUser(request);
        return getSuccessResponseVO(aiChatService.chat(request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody AiChatRequestDTO request) {
        fillLoginUser(request);
        return aiChatService.streamChat(request);
    }

    /**
     * 查询当前登录用户的 AI 会话列表。
     *
     * <p>AI 对话页允许匿名访问，所以这里不使用强登录拦截。
     * 未登录时直接返回空列表，前端据此展示登录提示。</p>
     */
    @PostMapping("/chat/session/list")
    public ResponseVO listSession() {
        TokenUserInfoDTO tokenUserInfo = getOptionalTokenUserInfo();
        String userId = tokenUserInfo == null ? null : tokenUserInfo.getUserId();
        return getSuccessResponseVO(aiChatSessionService.listUserSessions(userId));
    }

    /**
     * 查询当前登录用户的 AI 会话详情。
     *
     * <p>服务层会校验会话归属；没有登录、会话过期或会话不属于当前用户时返回 null。</p>
     */
    @PostMapping("/chat/session/detail")
    public ResponseVO getSessionDetail(@RequestBody AiChatSessionDetailRequestDTO request) {
        TokenUserInfoDTO tokenUserInfo = getOptionalTokenUserInfo();
        if (tokenUserInfo == null || request == null || StringUtils.isBlank(request.getConversationId())) {
            return getSuccessResponseVO(null);
        }
        return getSuccessResponseVO(aiChatSessionService.getSessionDetail(
                tokenUserInfo.getUserId(),
                request.getConversationId()
        ));
    }

    /**
     * 删除当前登录用户的 AI 会话。
     *
     * <p>删除接口不返回“是否真的删到数据”，避免通过 conversationId 探测其他用户的会话。</p>
     */
    @PostMapping("/chat/session/delete")
    public ResponseVO deleteSession(@RequestBody AiChatSessionDeleteRequestDTO request) {
        TokenUserInfoDTO tokenUserInfo = getOptionalTokenUserInfo();
        if (tokenUserInfo != null && request != null && StringUtils.isNotBlank(request.getConversationId())) {
            aiChatSessionService.deleteSession(tokenUserInfo.getUserId(), request.getConversationId());
        }
        return getSuccessResponseVO(Boolean.TRUE);
    }

    /**
     * 把可选登录用户写入请求对象。
     *
     * <p>前端不需要传 userId，避免客户端伪造归属；是否保存历史由后端 token 判断。</p>
     */
    private void fillLoginUser(AiChatRequestDTO request) {
        if (request == null) {
            return;
        }
        TokenUserInfoDTO tokenUserInfo = getOptionalTokenUserInfo();
        if (tokenUserInfo != null) {
            request.setLoginUserId(tokenUserInfo.getUserId());
        }
    }
}
