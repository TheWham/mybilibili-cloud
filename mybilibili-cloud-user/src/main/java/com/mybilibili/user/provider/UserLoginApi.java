package com.mybilibili.user.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.user.component.UserRedisComponent;
import com.mybilibili.user.component.UserStatsCacheAsyncComponent;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.enums.SexEnum;
import com.mybilibili.user.enums.StatusEnum;
import com.mybilibili.user.mappers.UserInfoMapper;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class UserLoginApi {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserRedisComponent userRedisComponent;
    @Resource
    private UserStatsCacheAsyncComponent userStatsCacheAsyncComponent;
    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/changeStatus")
    public Integer changeStatus(@RequestParam("userId") String userId, @RequestParam("type") Integer type) {
        return userInfoService.changeStatus(userId, type);
    }

    @RequestMapping("/register")
    public void register(@RequestBody RegisterDTO registerDTO) {
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
        SysSettingDTO sysSetting = userRedisComponent.getSysSetting();
        aUserInfo.setCurrentCoinCount(sysSetting.getRegisterCoinCount());
        aUserInfo.setTotalCoinCount(sysSetting.getRegisterCoinCount());
        userInfoMapper.insert(aUserInfo);
    }

    @RequestMapping("/login")
    public TokenUserInfoDTO login(@RequestBody WebLoginDTO webLoginDTO) {
        return userInfoService.login(webLoginDTO);
    }

    @RequestMapping("/autoLogin")
    public void autoLogin(@RequestParam("userId") String userId) {
        userStatsCacheAsyncComponent.refreshRealtimeUserStatsCache(userId);
    }

    @RequestMapping("/getUserCountInfo")
    public UserCountVO getUserCountInfo(@RequestParam("userId") String userId) {
        return userInfoService.getUserCountInfo(userId);
    }
}
