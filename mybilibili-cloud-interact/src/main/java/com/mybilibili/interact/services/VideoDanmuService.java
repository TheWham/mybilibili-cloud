package com.mybilibili.interact.services;

import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;

import java.util.List;

/**
 * 视频弹幕 Service。
 */
public interface VideoDanmuService {

    List<VideoDanmu> findListByParam(VideoDanmuQuery param);

    Integer findCountByParam(VideoDanmuQuery param);

    PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param);

    Integer add(VideoDanmu bean);

    Integer addBatch(List<VideoDanmu> listBean);

    Integer addOrUpdateBatch(List<VideoDanmu> listBean);

    VideoDanmu getVideoDanmuByDanmuId(Integer danmuId);

    Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId);

    Integer deleteVideoDanmuByDanmuId(Integer danmuId, Boolean isAdmin, String userId);

    void postDanmu(VideoDanmu videoDanmu);

    List<VideoDanmu> loadDanmu(String fileId, String videoId);
}
