package com.mybilibili.interact.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 视频弹幕 Mapper。
 */
public interface VideoDanmuMapper<T, R> extends BaseMapper {

    T selectByDanmuId(@Param("danmuId") Integer danmuId);

    Integer updateByDanmuId(@Param("bean") T t, @Param("danmuId") Integer danmuId);

    Integer deleteByDanmuId(@Param("danmuId") Integer danmuId);
}
