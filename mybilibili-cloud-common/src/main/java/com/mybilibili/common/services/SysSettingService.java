package com.mybilibili.common.services;


import com.mybilibili.base.entity.po.SysSetting;
import com.mybilibili.base.entity.query.SysSettingQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/04/22
 * Service
 */
public interface SysSettingService {

	/**
	 * 根据条件查询
	 */
	List<SysSetting> findListByParam(SysSettingQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(SysSettingQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SysSetting> findListByPage(SysSettingQuery param);

	/**
	 * 新增
	 */
	Integer add(SysSetting bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SysSetting>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SysSetting> listBean);


	/**
	 * 根据 Id查询
	 */
	SysSetting getSysSettingById(Long id);

	/**
	 * 根据 Id更新
	 */
	Integer updateSysSettingById(SysSetting bean, Long id);

	/**
	 * 根据 Id删除
	 */
	Integer deleteSysSettingById(Long id);

}