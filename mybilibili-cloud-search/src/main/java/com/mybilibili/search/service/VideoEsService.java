package com.mybilibili.search.service;

import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoSearchCountUpdateDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoSearchResultVO;

public interface VideoEsService {

    void saveDoc(VideoInfoDTO videoInfo);

    void updateDoc(VideoInfoDTO videoInfo);

    void deleteDoc(String videoId);

    void updateCount(VideoSearchCountUpdateDTO countUpdateDTO);

    PaginationResultVO<VideoSearchResultVO> search(Boolean highlight, String keyword, Integer orderType, Integer pageNo, Integer pageSize);

    void rebuildVideoIndex();
}
