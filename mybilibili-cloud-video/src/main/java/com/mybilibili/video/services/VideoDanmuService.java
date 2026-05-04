package com.mybilibili.video.services;

import com.mybilibili.video.entity.po.VideoDanmu;
import com.mybilibili.video.entity.query.VideoDanmuQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/09
 * 视频弹幕Service
 */
public interface VideoDanmuService {

	/**
	 * 根据条件查询
	 */
	List<VideoDanmu> findListByParam(VideoDanmuQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(VideoDanmuQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param);

	/**
	 * 新增
	 */
	Integer add(VideoDanmu bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoDanmu>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoDanmu> listBean);


	/**
	 * 根据 DanmuId查询
	 */
	VideoDanmu getVideoDanmuByDanmuId(Integer danmuId);

	/**
	 * 根据 DanmuId更新
	 */
	Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId);

	/**
	 * 根据 DanmuId删除
	 */
	Integer deleteVideoDanmuByDanmuId(Integer danmuId, Boolean isAdmin, String userId);

	/**
	 * 发送弹幕
	 * @param videoDanmu 弹幕信息
	 */
	void postDanmu(VideoDanmu videoDanmu);

	/**
	 * 加载弹幕
	 * @param fileId 视频文件id
	 * @param videoId 视频id
	 * @return 弹幕集合
	 */
	List<VideoDanmu> loadDanmu(String fileId, String videoId);
}