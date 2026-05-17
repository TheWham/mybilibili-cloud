package com.mybilibili.ai.service;

import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;

import java.util.List;

public interface AiSubtitleVectorService {

    /**
     * 只根据字幕向量做检索。
     *
     * @param queryVector 查询向量
     * @param topK 返回条数
     * @param minScore 向量最低相似度
     * @return 聚合后的视频命中列表
     */
    List<AiMatchedVideoVO> search(List<Double> queryVector, Integer topK, Double minScore);

    /**
     * 按旧协议做混合检索。
     *
     * <p>该方法保留给已有调用方，关键词会同时作为字幕检索词和元数据检索词使用。
     * 新的 AI 问答链路应优先调用带 rawKeyword 和 metadataKeywords 的重载方法。</p>
     *
     * @param keyword 字幕和元数据共用关键词
     * @param queryVector 查询向量
     * @param topK 返回条数
     * @param minScore 向量最低相似度
     * @return 聚合后的视频命中列表
     */
    List<AiMatchedVideoVO> search(String keyword, List<Double> queryVector, Integer topK, Double minScore);

    /**
     * 按字幕语义词、用户原始问题和元数据短关键词做多路召回。
     *
     * <p>subtitleKeyword 服务于字幕关键词和向量检索；rawKeyword、metadataKeywords
     * 服务于标题/标签检索，避免标题短词被字幕语义改写词稀释。</p>
     *
     * @param subtitleKeyword 字幕检索词，通常来自 AI 改写结果
     * @param rawKeyword 用户原始问题
     * @param metadataKeywords 标题/标签短关键词
     * @param queryVector 字幕向量
     * @param topK 返回条数
     * @param minScore 向量最低相似度
     * @return 聚合后的视频命中列表
     */
    List<AiMatchedVideoVO> search(String subtitleKeyword,
                                  String rawKeyword,
                                  List<String> metadataKeywords,
                                  List<Double> queryVector,
                                  Integer topK,
                                  Double minScore);

    /**
     * 删除一个视频下的全部字幕向量。
     *
     * @param videoId 视频 ID
     */
    void deleteByVideoId(String videoId);

    /**
     * 按分 P 文件 ID 批量删除字幕向量。
     *
     * <p>审核通过时只清理被删除或被替换的分 P，未变化分 P 的向量继续保留，避免重复向量化。</p>
     *
     * @param fileIds 分 P 文件 ID 列表
     */
    void deleteByFileIds(List<String> fileIds);

    /**
     * 更新一个视频下字幕向量文档里的视频展示信息。
     *
     * <p>标题、封面、标签属于视频元数据，变更时不需要重新切字幕和向量化，直接更新 ES 文档即可。</p>
     *
     * @param videoId 视频 ID
     * @param videoName 视频标题
     * @param videoCover 视频封面
     * @param tags 视频标签
     */
    void updateVideoMetaByVideoId(String videoId, String videoName, String videoCover, String tags);
}
