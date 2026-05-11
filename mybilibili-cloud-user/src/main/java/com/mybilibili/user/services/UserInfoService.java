package com.mybilibili.user.services;

import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.base.entity.vo.UserInfoVO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.user.entity.vo.UCenterVideoDateVO;
import com.mybilibili.user.entity.vo.UCenterVideoWeekCountVO;
import com.mybilibili.user.entity.po.UserInfo;

import java.util.List;

public interface UserInfoService {

    List<UserInfo> findListByParam(UserInfoQuery param);
    Integer findCountByParam(UserInfoQuery param);
    PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param);
    Integer add(UserInfo bean);
    Integer addBatch(List<UserInfo> listBean);
    Integer addOrUpdateBatch(List<UserInfo> listBean);
    UserInfo getUserInfoByUserId(String userId);
    Integer updateUserInfoByUserId(UserInfo bean, String userId);
    Integer deleteUserInfoByUserId(String userId);
    UserInfo getUserInfoByEmail(String email);
    Integer updateUserInfoByEmail(UserInfo bean, String email);
    Integer deleteUserInfoByEmail(String email);
    UserInfo getUserInfoByNickName(String nickName);
    Integer updateUserInfoByNickName(UserInfo bean, String nickName);
    Integer deleteUserInfoByNickName(String nickName);

    void register(RegisterDTO registerDTO);
    TokenUserInfoDTO login(WebLoginDTO webLoginDTO);
    void setUserInHome(UserInfoVO userInfoVO);

    UserInfoVO getUHomeUserInfo(String userId, TokenUserInfoDTO currentUser);
    void updateUserInfoUHome(TokenUserInfoDTO tokenUserInfoDTO, UserInfo userInfo);
    void saveTheme(String userId, Integer theme);
    Integer selectTotalCoinCount(String userId);
    UserCountVO getUserCountInfo(String userId);

    UCenterVideoDateVO getActualTimeStatisticsInfo(String userId);
    List<UCenterVideoWeekCountVO> getWeekStatisticsInfo(UserStatsRedisEnum anEnum, String userId);

    Integer changeStatus(String userId, Integer type);
}
