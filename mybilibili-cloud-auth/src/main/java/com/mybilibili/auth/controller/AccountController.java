package com.mybilibili.auth.controller;

import com.mybilibili.auth.consumer.UserLoginClient;
import com.mybilibili.auth.component.AuthRedisComponent;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.component.TokenRedisComponent;
import com.mybilibili.common.controller.ABaseController;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private UserLoginClient userLoginClient;

    @Resource
    private AuthRedisComponent authRedisComponent;

    @Resource
    private TokenRedisComponent tokenRedisComponent;

/**
 * 获取验证码接口
 * @return 返回包含验证码图片和验证码key的响应对象
 */
    @RequestMapping("/checkCode")
    public ResponseVO getCheckCode(){
        // 1. 生成验证码
    // 创建一个指定宽高的算术验证码对象
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);

    // 获取验证码文本结果
        String code = captcha.text();
    // 将验证码图片转换为Base64编码格式
        String checkCode = captcha.toBase64();

    // 将验证码保存到Redis中并获取对应的key
        String checkCodeKey = authRedisComponent.saveCode(code);


    // 创建Map对象用于存储验证码key和验证码图片
        Map<String, String> map = new HashMap<>();
        map.put("checkCodeKey", checkCodeKey); // 存储验证码在Redis中的key
        map.put("checkCode", checkCode);       // 存储验证码图片的Base64编码

    // 返回成功响应，包含验证码相关信息
        return getSuccessResponseVO(map);
    }

/**
 * 用户注册接口
 * @param registerDTO 注册信息DTO，包含用户名、密码、验证码等信息
 * @return ResponseVO 统一响应对象，包含操作结果信息
 * @throws BusinessException 业务异常，可能抛出注册过程中的业务异常
 */
    @RequestMapping("/register")
    public ResponseVO register(@Validated RegisterDTO registerDTO){

    // 获取用户输入的验证码
        String checkCode = registerDTO.getCheckCode();
    // 从Redis中获取存储的验证码
        String redisCode = authRedisComponent.getCode(registerDTO.getCheckCodeKey());
        boolean check = checkCode.equalsIgnoreCase(redisCode);
        try {
            if (!check) {
                throw new BusinessException("图形验证码不正确");
            }
            userLoginClient.register(registerDTO);
            return getSuccessResponseVO(null);
        }finally {
            authRedisComponent.cleanCheckCode(registerDTO.getCheckCodeKey());
        }
    }

    @RequestMapping("/login")
    public ResponseVO login(
            HttpServletResponse response,
            HttpServletRequest request,
            @Validated WebLoginDTO webLoginDTO) {
        // 获取用户输入的验证码
        String checkCode = webLoginDTO.getCheckCode();
        // 从 redis中获取存储的验证码
        String redisCode = authRedisComponent.getCode(webLoginDTO.getCheckCodeKey());
        String lastLoginIp = getIpAddr();
        webLoginDTO.setLastLoginIp(lastLoginIp);

        boolean check = checkCode.equalsIgnoreCase(redisCode);
        try {
            if (!check) {
                throw new BusinessException("图形验证码不正确");
            }
            TokenUserInfoDTO tokenInfo = userLoginClient.login(webLoginDTO);
            //为了方便直接将token信息到session中, 正常来说只需要提供tokenID给前端即可, 然后每次请求带着tokenID从redis中取出数据
            saveToken2Session(response, tokenInfo.getTokenId());
            return getSuccessResponseVO(tokenInfo);

        }finally {
            authRedisComponent.cleanCheckCode(webLoginDTO.getCheckCodeKey());
        }
    }

    @RequestMapping("/autoLogin")
    public ResponseVO autoLogin(
            HttpServletResponse response) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();

        if (tokenUserInfo == null)
            return getSuccessResponseVO(null);

        // 判断是否在其他浏览器中登录, 如果登录则挤掉旧账号
        String latestTokenId = tokenRedisComponent.getTokenIdByUserId(tokenUserInfo.getUserId());
        if (!tokenUserInfo.getTokenId().equals(latestTokenId))
            return getSuccessResponseVO(null);

        if (tokenUserInfo.getExpireAt() - System.currentTimeMillis() < Constants.REDIS_EXPIRE_TIME_ONE_DAY){
            tokenUserInfo.setExpireAt(System.currentTimeMillis()
                    + (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT);
            tokenRedisComponent.updateTokenUserInfo(tokenUserInfo);
        }

        // 用户统计缓存归 user 服务维护，auth 只负责登录态续期和转发刷新请求。
        userLoginClient.refreshRealtimeUserStatsCache(tokenUserInfo.getUserId());
        saveToken2Session(response, tokenUserInfo.getTokenId());
        return getSuccessResponseVO(tokenUserInfo);
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response){
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getUserCountInfo")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO getUserCountInfo() {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo();
        UserCountVO userCountVO = userLoginClient.getUserCountInfo(tokenUserInfoDTO.getUserId());
        return getSuccessResponseVO(userCountVO);
    }

}
