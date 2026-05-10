package com.mybilibili.ai.service;

import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;

import java.util.List;

public interface AiSubtitleVectorService {

    List<AiMatchedVideoVO> search(List<Double> queryVector, Integer topK, Double minScore);

    List<AiMatchedVideoVO> search(String keyword, List<Double> queryVector, Integer topK, Double minScore);

    void deleteByVideoId(String videoId);
}
