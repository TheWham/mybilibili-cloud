package com.mybilibili.auth.services;


import com.mybilibili.auth.entity.dto.RegisterDTO;
import com.mybilibili.auth.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;


/**
 * @author amani
 * @since 2026/01/07
 */

public interface authService {

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

}
