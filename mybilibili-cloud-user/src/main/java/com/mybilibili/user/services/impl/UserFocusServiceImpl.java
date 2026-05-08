package com.mybilibili.user.services.impl;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.user.component.UserRedisComponent;
import com.mybilibili.user.entity.po.UserFocus;
import com.mybilibili.user.entity.query.UserFocusQuery;
import com.mybilibili.user.mappers.UserFocusMapper;
import com.mybilibili.user.services.UserFocusService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表Service
 */

@Service("UserFocusService")
public class UserFocusServiceImpl implements UserFocusService {
	@Resource
	private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
	@Resource
	private UserRedisComponent userRedisComponent;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<UserFocus> findListByParam(UserFocusQuery param) {
		return this.userFocusMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserFocusQuery param) {
		return this.userFocusMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserFocus> list = this.findListByParam(param);
		PaginationResultVO<UserFocus> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserFocus bean) {
		return this.userFocusMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserFocus>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userFocusMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserFocus> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userFocusMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 UserIdAndUserFocusId查询
	 */
	@Override
	public UserFocus getUserFocusByUserIdAndUserFocusId(String userId, String userFocusId) {
		return this.userFocusMapper.selectByUserIdAndUserFocusId(userId, userFocusId);
	}

	/**
	 * 根据 UserIdAndUserFocusId更新
	 */
	@Override
	public Integer updateUserFocusByUserIdAndUserFocusId(UserFocus bean, String userId, String userFocusId) {
		return this.userFocusMapper.updateByUserIdAndUserFocusId(bean, userId, userFocusId);
	}

	/**
	 * 根据 UserIdAndUserFocusId删除
	 */
	@Override
	public Integer deleteUserFocusByUserIdAndUserFocusId(String userId, String userFocusId) {
		return this.userFocusMapper.deleteByUserIdAndUserFocusId(userId, userFocusId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void focus(String focusUserId, String userId) {
		UserFocus userFocus = new UserFocus();
		userFocus.setFocusTime(new Date());
		userFocus.setUserId(userId);
		userFocus.setUserFocusId(focusUserId);
		Integer rowCount = userFocusMapper.insertIgnore(userFocus);
		if (rowCount == 0)
		{
			//插入失败表示已经关注
			throw new BusinessException("不能重复关注");
		}
		userRedisComponent.incrementUserStats(userId, UserStatsRedisEnum.USER_FOCUS.getField(), Constants.ONE);
		userRedisComponent.incrementUserStats(focusUserId, UserStatsRedisEnum.USER_FANS.getField(), Constants.ONE);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelFocus(String focusUserId, String userId)
	{
		Integer count = userFocusMapper.deleteByUserIdAndUserFocusId(userId, focusUserId);
		if (count == 0)
			throw new BusinessException("取关失败");
		userRedisComponent.incrementUserStats(userId, UserStatsRedisEnum.USER_FOCUS.getField(), -Constants.ONE);
		userRedisComponent.incrementUserStats(focusUserId, UserStatsRedisEnum.USER_FANS.getField(), -Constants.ONE);
	}

	@Override
	public Integer selectHaveFocus(String userId, String focusUserId) {
		return userFocusMapper.selectHaveFocus(userId, focusUserId);
	}

}
