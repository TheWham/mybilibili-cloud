package com.mybilibili.common.services.impl;

import com.mybilibili.common.entity.po.SysSetting;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.SysSettingQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.common.mappers.SysSettingMapper;
import com.mybilibili.common.services.SysSettingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @since 2026/04/22
 * Service
 */

@Service("SysSettingService")
public class SysSettingServiceImpl implements SysSettingService {
	@Resource
	private SysSettingMapper<SysSetting, SysSettingQuery> sysSettingMapper;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<SysSetting> findListByParam(SysSettingQuery param) {
		return this.sysSettingMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(SysSettingQuery param) {
		return this.sysSettingMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<SysSetting> findListByPage(SysSettingQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SysSetting> list = this.findListByParam(param);
		PaginationResultVO<SysSetting> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SysSetting bean) {
		return this.sysSettingMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SysSetting>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysSettingMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SysSetting> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysSettingMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 Id查询
	 */
	@Override
	public SysSetting getSysSettingById(Long id) {
		return this.sysSettingMapper.selectById(id);
	}

	/**
	 * 根据 Id更新
	 */
	@Override
	public Integer updateSysSettingById(SysSetting bean, Long id) {
		return this.sysSettingMapper.updateById(bean, id);
	}

	/**
	 * 根据 Id删除
	 */
	@Override
	public Integer deleteSysSettingById(Long id) {
		return this.sysSettingMapper.deleteById(id);
	}

}