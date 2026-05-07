package com.mybilibili.interact.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import com.mybilibili.interact.entity.dto.CommentCountUpdateDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper。
 */
public interface VideoCommentMapper<T, R> extends BaseMapper {

    T selectByCommentId(@Param("commentId") Integer commentId);

    Integer updateByCommentId(@Param("bean") T t, @Param("commentId") Integer commentId);

    Integer deleteByCommentId(@Param("commentId") Integer commentId);

    List<T> selectListWithChildren(@Param("query") R query);

    Integer updateCount(@Param("commentId") Integer commentId, @Param("likeDiff") Integer likeDiff, @Param("hateDiff") Integer hateDiff);

    Integer updateCountBatch(@Param("list") List<CommentCountUpdateDTO> list);
}
