package com.mybilibili.user.services;

import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserInfoVO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.entity.vo.UserCountVO;

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

    //TODO 接入Video模块后恢复
    // PaginationResultVO<VideoInfoUHomeVO> loadUHomeVideoList(String userId, Integer type, Integer pageNo, String videoName, Integer orderType);

    UserInfoVO getUHomeUserInfo(String userId, TokenUserInfoDTO currentUser);
    void updateUserInfoUHome(TokenUserInfoDTO tokenUserInfoDTO, UserInfo userInfo);
    void saveTheme(String userId, Integer theme);
    Integer selectTotalCoinCount(String userId);
    UserCountVO getUserCountInfo(String userId);

    //TODO 接入Video模块后恢复
    // UCenterVideoDateVO getActualTimeStatisticsInfo(String userId);
    // List<UCenterVideoWeekCountVO> getWeekStatisticsInfo(UserStatsRedisEnum anEnum, String userId);

    Integer changeStatus(String userId, Integer type);
}
