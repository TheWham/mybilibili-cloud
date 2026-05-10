package com.mybilibili.auth.services;


import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.UserCountVO;

import java.util.Map;


/**
 * @author amani
 * @since 2026/01/07
 */

public interface AuthService {

	Integer changeStatus(String userId, Integer type);

	/**
	 * 用户注册方法
	 * @param registerDTO 注册数据传输对象，包含用户注册所需的所有信息
	 * @throws com.mybilibili.base.exception.BusinessException 当注册过程中出现业务异常时抛出，如用户名已存在、验证码错误等
	 */
	void register(RegisterDTO registerDTO);

	/**
	 * 用户登录方法
	 * @param webLoginDTO 登录数据传输对象，包含用户登录所需的信息
	 */
	TokenUserInfoDTO login(WebLoginDTO webLoginDTO);

	/**
	 * 自动登录时刷新登录态和用户实时统计缓存。
	 *
	 * @param tokenUserInfo 当前请求携带的登录用户信息
	 * @return 可继续登录时返回更新后的用户信息，否则返回 null
	 */
	TokenUserInfoDTO autoLogin(TokenUserInfoDTO tokenUserInfo);

	/**
	 * 查询用户侧统计数据。
	 *
	 * @param userId 当前登录用户 ID
	 * @return 用户统计信息
	 */
	UserCountVO getUserCountInfo(String userId);

	/**
	 * 生成验证码
	 */
	Map<String, String> getCheckCode();

}
