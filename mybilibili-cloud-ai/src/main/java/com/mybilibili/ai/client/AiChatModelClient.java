package com.mybilibili.ai.client;

import java.util.function.Consumer;

/**
 * 对话模型客户端。
 *
 * <p>业务层只关心问答能力，不直接感知具体供应商协议。后续从 OpenAI 兼容接口切到
 * Spring AI、百炼 SDK 或其他网关时，只需要替换这个接口的实现。</p>
 */
public interface AiChatModelClient {

    /**
     * 普通问答。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return 模型完整回答
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 流式问答。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param deltaConsumer 增量内容回调
     * @return 模型完整回答
     */
    String streamChat(String systemPrompt, String userPrompt, Consumer<String> deltaConsumer);
}
