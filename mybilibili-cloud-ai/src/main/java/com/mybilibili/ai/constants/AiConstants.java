package com.mybilibili.ai.constants;

/**
 * AI 模块内部协议常量。
 *
 * <p>这些值和前端事件、模型接口、ES 查询协议绑定，放在代码里比放到配置中心更稳定。</p>
 */
public final class AiConstants {

    private AiConstants() {
    }

    public static final String MATCH_TYPE_SUBTITLE = "subtitle";
    public static final String MATCH_TYPE_TITLE = "title";

    public static final String SUGGESTION_TYPE_CONTINUE = "continue";
    public static final String SUGGESTION_TYPE_VIDEO = "video";
    public static final String SUGGESTION_TYPE_MORE = "more";
    public static final String SUGGESTION_ID_PREFIX = "sug_";

    public static final String SSE_EVENT_START = "start";
    public static final String SSE_EVENT_DELTA = "delta";
    public static final String SSE_EVENT_VIDEOS = "videos";
    public static final String SSE_EVENT_SUGGESTIONS = "suggestions";
    public static final String SSE_EVENT_DONE = "done";
    public static final String SSE_EVENT_ERROR = "error";
    public static final String SSE_FIELD_CONVERSATION_ID = "conversationId";
    public static final String SSE_FIELD_MESSAGE = "message";

    public static final String OLLAMA_EMBED_API_PATH = "/api/embed";
    public static final String OLLAMA_LEGACY_EMBED_API_PATH = "/api/embeddings";
    public static final String OPENAI_CHAT_COMPLETIONS_API_PATH = "/v1/chat/completions";

    public static final String ES_SEARCH_PATH = "_search";
    public static final String ES_DELETE_BY_QUERY_PATH = "_delete_by_query";
    public static final String ES_CONFLICTS_PARAM = "conflicts";
    public static final String ES_CONFLICTS_PROCEED = "proceed";
    public static final String ES_CONTENT_VECTOR_FIELD = "contentVector";
    public static final double ES_COSINE_SCORE_OFFSET = 1.0D;
    public static final double SCORE_SCALE = 10000D;
}
