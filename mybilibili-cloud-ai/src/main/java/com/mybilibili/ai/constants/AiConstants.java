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
    public static final String MATCH_SOURCE_VECTOR = "vector";
    public static final String MATCH_SOURCE_SUBTITLE = "subtitle";
    public static final String MATCH_SOURCE_TITLE = "title";
    public static final String MATCH_SOURCE_HYBRID = "hybrid";

    public static final String SUGGESTION_TYPE_CONTINUE = "continue";
    public static final String SUGGESTION_TYPE_VIDEO = "video";
    public static final String SUGGESTION_TYPE_MORE = "more";
    public static final String SUGGESTION_ID_PREFIX = "sug_";

    public static final String INTENT_TYPE_VIDEO_SEARCH = "video_search";
    public static final String INTENT_TYPE_VIDEO_TITLE_SEARCH = "video_title_search";
    public static final String INTENT_TYPE_VIDEO_SUBTITLE_SEARCH = "video_subtitle_search";
    public static final String INTENT_TYPE_VIDEO_HYBRID_SEARCH = "video_hybrid_search";
    public static final String INTENT_TYPE_KNOWLEDGE_QUESTION = "knowledge_question";
    public static final String INTENT_TYPE_FOLLOW_UP = "follow_up";
    public static final String INTENT_TYPE_UNKNOWN = "unknown";

    public static final String SESSION_ACTION_INIT = "init";
    public static final String SESSION_ACTION_CHAT = "chat";
    public static final String REDIS_KEY_AI_CHAT_SESSION = "ai:chat:session:";
    public static final String REDIS_KEY_AI_CHAT_USER_SESSIONS = "ai:chat:user:sessions:";
    public static final String CHAT_MESSAGE_ROLE_USER = "user";
    public static final String CHAT_MESSAGE_ROLE_ASSISTANT = "assistant";

    public static final String SSE_EVENT_START = "start";
    public static final String SSE_EVENT_WELCOME = "welcome";
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
