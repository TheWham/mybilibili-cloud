package com.mybilibili.user.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.component.RedisComponent;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.enums.SexEnum;
import com.mybilibili.user.enums.StatusEnum;
import com.mybilibili.user.mappers.UserInfoMapper;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class UserLoginApi {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/changeStatus")
    public Integer changeStatus(String userId, Integer type) {
        return userInfoService.changeStatus(userId, type);
    }

    @RequestMapping("/register")
    public void register(RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        String nickName = registerDTO.getNickName();
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo != null)
            throw new BusinessException("邮箱已经存在");

        UserInfo userInfo1 = userInfoMapper.selectByNickName(nickName);
        if (userInfo1 != null)
            throw new BusinessException("昵称已经存在");

        UserInfo aUserInfo = BeanUtil.toBean(registerDTO, UserInfo.class);
        aUserInfo.setUserId(StringTools.generateRandomNumber(Constants.USER_ID_LENGTH));
        aUserInfo.setPassword(StringTools.md5Password(registerDTO.getRegisterPassword()));
        aUserInfo.setJoinTime(new Date());
        aUserInfo.setStatus(StatusEnum.NORMAL.getType());
        aUserInfo.setSex(SexEnum.UNKNOWN.getType());
        SysSettingDTO sysSetting = redisComponent.getSysSetting();
        aUserInfo.setCurrentCoinCount(sysSetting.getRegisterCoinCount());
        aUserInfo.setTotalCoinCount(sysSetting.getRegisterCoinCount());
        userInfoMapper.insert(aUserInfo);
    }

    @RequestMapping("/login")
    public TokenUserInfoDTO login(WebLoginDTO webLoginDTO) {
        return userInfoService.login(webLoginDTO);
    }

    //TODO 接入auth模块后: autoLogin逻辑当前在auth的AccountController中直接调用Redis
    @RequestMapping("/autoLogin")
    public void autoLogin() {
    }
}
