package com.mybilibili.ai.service;

import com.mybilibili.ai.entity.dto.AiChatRequestDTO;
import com.mybilibili.ai.entity.vo.AiChatResultVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {

    AiChatResultVO chat(AiChatRequestDTO request);

    SseEmitter streamChat(AiChatRequestDTO request);
}
