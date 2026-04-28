package com.mybilibili.user.controller;


import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author amani
 * @date 2026/01/07
 * @description Service
 */

@RestController
@RequestMapping("userInfo")
public class UserInfoController extends ABaseController {
	@Resource
	private UserInfoService userInfoService;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (UserInfoQuery query) {
		return getSuccessResponseVO(userInfoService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		Integer count = this.userInfoService.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.userInfoService.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(UserInfo bean) {
		userInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 UserId查询
	 */
	@RequestMapping("getUserInfoByUserId")
	public ResponseVO getUserInfoByUserId(String userId) {
		return getSuccessResponseVO(this.userInfoService.getUserInfoByUserId(userId));
	}

	/**
	 * @description 根据 UserId更新
	 */
	@RequestMapping("updateUserInfoByUserId")
	public ResponseVO updateUserInfoByUserId(UserInfo bean, String userId) {
		this.userInfoService.updateUserInfoByUserId(bean, userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 UserId删除
	 */
	@RequestMapping("deleteUserInfoByUserId")
	public ResponseVO deleteUserInfoByUserId(String userId) {
		this.userInfoService.deleteUserInfoByUserId(userId);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 Email查询
	 */
	@RequestMapping("getUserInfoByEmail")
	public ResponseVO getUserInfoByEmail(String email) {
		return getSuccessResponseVO(this.userInfoService.getUserInfoByEmail(email));
	}

	/**
	 * @description 根据 Email更新
	 */
	@RequestMapping("updateUserInfoByEmail")
	public ResponseVO updateUserInfoByEmail(UserInfo bean, String email) {
		this.userInfoService.updateUserInfoByEmail(bean, email);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 Email删除
	 */
	@RequestMapping("deleteUserInfoByEmail")
	public ResponseVO deleteUserInfoByEmail(String email) {
		this.userInfoService.deleteUserInfoByEmail(email);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 NickName查询
	 */
	@RequestMapping("getUserInfoByNickName")
	public ResponseVO getUserInfoByNickName(String nickName) {
		return getSuccessResponseVO(this.userInfoService.getUserInfoByNickName(nickName));
	}

	/**
	 * @description 根据 NickName更新
	 */
	@RequestMapping("updateUserInfoByNickName")
	public ResponseVO updateUserInfoByNickName(UserInfo bean, String nickName) {
		this.userInfoService.updateUserInfoByNickName(bean, nickName);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 NickName删除
	 */
	@RequestMapping("deleteUserInfoByNickName")
	public ResponseVO deleteUserInfoByNickName(String nickName) {
		this.userInfoService.deleteUserInfoByNickName(nickName);
		return getSuccessResponseVO(null);
	}

}
