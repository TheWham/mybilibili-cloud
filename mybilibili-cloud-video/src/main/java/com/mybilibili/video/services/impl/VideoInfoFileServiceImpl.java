package com.mybilibili.video.services.impl;

import com.mybilibili.video.entity.po.VideoInfoFile;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.video.entity.query.VideoInfoFileQuery;
import com.mybilibili.video.mappers.VideoInfoFileMapper;
import com.mybilibili.video.services.VideoInfoFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息Service
 */

@Service("VideoInfoFileService")
public class VideoInfoFileServiceImpl implements VideoInfoFileService {
	@Resource
	private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoInfoFile> findListByParam(VideoInfoFileQuery param) {
		return this.videoInfoFileMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoInfoFileQuery param) {
		return this.videoInfoFileMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoInfoFile> findListByPage(VideoInfoFileQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoFile> list = this.findListByParam(param);
		PaginationResultVO<VideoInfoFile> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoInfoFile bean) {
		return this.videoInfoFileMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfoFile>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoFileMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfoFile> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoFileMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 FileId查询
	 */
	@Override
	public VideoInfoFile getVideoInfoFileByFileId(String fileId) {
		return this.videoInfoFileMapper.selectByFileId(fileId);
	}

	/**
	 * @description 根据 FileId更新
	 */
	@Override
	public Integer updateVideoInfoFileByFileId(VideoInfoFile bean, String fileId) {
		return this.videoInfoFileMapper.updateByFileId(bean, fileId);
	}

	/**
	 * @description 根据 FileId删除
	 */
	@Override
	public Integer deleteVideoInfoFileByFileId(String fileId) {
		return this.videoInfoFileMapper.deleteByFileId(fileId);
	}

	@Override
	public Integer deleteByCondition(VideoInfoFileQuery videoInfoFileQuery) {
		return this.videoInfoFileMapper.deleteByCondition(videoInfoFileQuery);
	}

}