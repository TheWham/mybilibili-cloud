package com.mybilibili.user.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.user.entity.po.UserStats;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserFocusQuery;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.query.UserStatsQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.base.entity.vo.UserInfoVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.component.TokenRedisComponent;
import com.mybilibili.user.component.UserRedisComponent;
import com.mybilibili.user.component.UserStatsCacheAsyncComponent;
import com.mybilibili.user.entity.po.UserFocus;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.enums.StatusEnum;
import com.mybilibili.user.mappers.UserFocusMapper;
import com.mybilibili.user.mappers.UserInfoMapper;
import com.mybilibili.user.mappers.UserStatsMapper;
import com.mybilibili.user.services.UserFocusService;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service("UserInfoService")
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    //TODO 接入Video模块
    // @Resource
    // private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    // @Resource
    // private VideoInfoService videoInfoService;
    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
    @Resource
    private TokenRedisComponent tokenRedisComponent;
    @Resource
    private UserRedisComponent userRedisComponent;
    @Resource
    private UserStatsCacheAsyncComponent userStatsCacheAsyncComponent;
    @Resource
    private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;
    @Resource
    private UserFocusService userFocusService;

    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        Integer count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    @Override
    public UserInfo getUserInfoByNickName(String nickName) {
        return this.userInfoMapper.selectByNickName(nickName);
    }

    @Override
    public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
        return this.userInfoMapper.updateByNickName(bean, nickName);
    }

    @Override
    public Integer deleteUserInfoByNickName(String nickName) {
        return this.userInfoMapper.deleteByNickName(nickName);
    }

    @Override
    public TokenUserInfoDTO login(WebLoginDTO webLoginDTO) {
        String email = webLoginDTO.getEmail();
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (userInfo == null || !userInfo.getPassword().equals(webLoginDTO.getPassword()))
            throw new BusinessException("账号或密码错误");

        if (StatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("用户已被禁用");
        }

        userInfo.setLastLoginIp(webLoginDTO.getLastLoginIp());
        userInfo.setLastLoginTime(new Date());
        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

        TokenUserInfoDTO tokenUserInfoDTO = BeanUtil.toBean(userInfo, TokenUserInfoDTO.class);
        tokenRedisComponent.saveTokenUserInfo(tokenUserInfoDTO);

        String userId = userInfo.getUserId();
        String tokenId = tokenRedisComponent.getTokenIdByUserId(userId);
        if (tokenId != null) {
            tokenRedisComponent.cleanExistToken(userId);
        }
        tokenRedisComponent.saveTokenIdByUserId(userInfo.getUserId(), tokenUserInfoDTO.getTokenId());
        userRedisComponent.refreshRealtimeUserStatsExpire(userInfo.getUserId());
        userStatsCacheAsyncComponent.refreshRealtimeUserStatsCache(userInfo.getUserId());
        return tokenUserInfoDTO;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 注册逻辑在 UserLoginApi.provider 中实现，避免循环依赖
        throw new UnsupportedOperationException("register should be called via UserLoginApi provider");
    }

    @Override
    public void setUserInHome(UserInfoVO userInfoVO) {
        String userId = userInfoVO.getUserId();
        HashMap<String, Integer> realtimeStatsMap = userRedisComponent.getRealtimeUserStatsInfo(userId);
        if (realtimeStatsMap != null && !realtimeStatsMap.isEmpty()) {
            userRedisComponent.refreshRealtimeUserStatsExpire(userId);
            fillUserInfoVOWithRealtimeStats(userInfoVO, realtimeStatsMap);
            return;
        }

        UserStats userStats = userStatsMapper.selectLatestByUserId(userId);
        if (userStats == null) {
            userInfoVO.setPlayCount(0);
            userInfoVO.setLikeCount(0);
            userInfoVO.setFansCount(0);
            userInfoVO.setFocusCount(0);
            return;
        }
        userInfoVO.setPlayCount(Optional.ofNullable(userStats.getPlayCount()).orElse(0));
        userInfoVO.setLikeCount(Optional.ofNullable(userStats.getLikeCount()).orElse(0));
        userInfoVO.setFansCount(Optional.ofNullable(userStats.getFansCount()).orElse(0));
        userInfoVO.setFocusCount(Optional.ofNullable(userStats.getFocusCount()).orElse(0));
    }

    //TODO 接入Video模块后恢复此方法
    // @Override
    // public PaginationResultVO<VideoInfoUHomeVO> loadUHomeVideoList(String userId, Integer type, Integer pageNo, String videoName, Integer orderType) {
    //     ...
    // }

    @Override
    public UserInfoVO getUHomeUserInfo(String userId, TokenUserInfoDTO currentUser) {
        UserInfo userInfoDb = this.getUserInfoByUserId(userId);
        if (userInfoDb == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserInfoVO userInfoVO = BeanUtil.toBean(userInfoDb, UserInfoVO.class);
        if (currentUser != null && !userId.equals(currentUser.getUserId())) {
            Integer haveFocus = userFocusService.selectHaveFocus(currentUser.getUserId(), userId);
            userInfoVO.setHaveFocus(haveFocus);
        }
        this.setUserInHome(userInfoVO);
        return userInfoVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfoUHome(TokenUserInfoDTO tokenUserInfoDTO, UserInfo userInfo) {
        String userId = tokenUserInfoDTO.getUserId();
        boolean isEditNickName = !tokenUserInfoDTO.getNickName().equals(userInfo.getNickName());
        Integer totalCoinCount = userInfoMapper.selectTotalCoinCount(userId);
        if (isEditNickName && totalCoinCount < Constants.UPDATE_NAME_COIN) {
            throw new BusinessException("硬币不足, 无法修改昵称");
        }
        userInfoMapper.updateByUserId(userInfo, userId);

        if (isEditNickName) {
            Integer count = userInfoMapper.updateUserCoin(userId, -Constants.UPDATE_NAME_COIN);
            if (count == 0) {
                throw new BusinessException("硬币不足,无法修改昵称");
            }
        }

        boolean isEditAvatar = tokenUserInfoDTO.getAvatar() == null || !tokenUserInfoDTO.getAvatar().equals(userInfo.getAvatar());
        boolean isEditPersonIntroduction = tokenUserInfoDTO.getPersonIntroduction() == null || !tokenUserInfoDTO.getPersonIntroduction().equals(userInfo.getPersonIntroduction());
        if (isEditNickName || isEditAvatar || isEditPersonIntroduction) {
            tokenUserInfoDTO.setNickName(userInfo.getNickName());
            tokenUserInfoDTO.setPersonIntroduction(userInfo.getPersonIntroduction());
            tokenUserInfoDTO.setAvatar(userInfo.getAvatar());
            tokenRedisComponent.updateTokenUserInfo(tokenUserInfoDTO);
        }
    }

    @Override
    public void saveTheme(String userId, Integer theme) {
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        this.updateUserInfoByUserId(userInfo, userId);
    }

    @Override
    public Integer selectTotalCoinCount(String userId) {
        return this.userInfoMapper.selectTotalCoinCount(userId);
    }

    @Override
    public UserCountVO getUserCountInfo(String userId) {
        HashMap<String, Integer> userStatsMap = userRedisComponent.getRealtimeUserStatsInfo(userId);
        if (userStatsMap != null && !userStatsMap.isEmpty()) {
            userRedisComponent.refreshRealtimeUserStatsExpire(userId);
            return buildUserCountVOFromRedis(userStatsMap);
        }

        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        if (userInfo == null) {
            return null;
        }
        userStatsCacheAsyncComponent.refreshRealtimeUserStatsCache(userId);
        return buildUserCountVOFromDB(userId, userInfo);
    }

    private UserCountVO buildUserCountVOFromRedis(HashMap<String, Integer> userStatsMap) {
        UserCountVO userCountVO = new UserCountVO();
        userCountVO.setFocusCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FOCUS.getField(), 0));
        userCountVO.setFansCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
        userCountVO.setCurrentCoinCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_COIN.getField(), 0));
        userCountVO.setLikeCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
        userCountVO.setPlayCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
        return userCountVO;
    }

    private UserCountVO buildUserCountVOFromDB(String userId, UserInfo userInfo) {
        UserCountVO userCountVO = new UserCountVO();
        userCountVO.setCurrentCoinCount(Optional.ofNullable(userInfo.getCurrentCoinCount()).orElse(0));

        UserFocusQuery focusQuery = new UserFocusQuery();
        focusQuery.setUserId(userId);
        userCountVO.setFocusCount(Optional.ofNullable(userFocusMapper.selectCount(focusQuery)).orElse(0));

        UserFocusQuery fansQuery = new UserFocusQuery();
        fansQuery.setUserFocusId(userId);
        userCountVO.setFansCount(Optional.ofNullable(userFocusMapper.selectCount(fansQuery)).orElse(0));

        //TODO 接入Video模块: 需要 videoInfoMapper.sumVideoCountByUserId()
        // VideoCountDTO videoCountDTO = videoInfoMapper.sumVideoCountByUserId(userId);
        // if (videoCountDTO == null) { ... }
        userCountVO.setLikeCount(0);
        userCountVO.setPlayCount(0);
        return userCountVO;
    }

    private void fillUserInfoVOWithRealtimeStats(UserInfoVO userInfoVO, HashMap<String, Integer> userStatsMap) {
        userInfoVO.setFocusCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FOCUS.getField(), 0));
        userInfoVO.setFansCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
        userInfoVO.setLikeCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
        userInfoVO.setPlayCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
    }

    //TODO 接入Video模块后恢复此方法
    // @Override
    // public UCenterVideoDateVO getActualTimeStatisticsInfo(String userId) {
    //     ...
    // }

    //TODO 接入Video模块后恢复此方法
    // @Override
    // public List<UCenterVideoWeekCountVO> getWeekStatisticsInfo(UserStatsRedisEnum anEnum, String userId) {
    //     ...
    // }

    @Override
    public Integer changeStatus(String userId, Integer type) {
        UserInfo userInfo = this.userInfoMapper.selectByUserId(userId);
        Optional.ofNullable(userInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));
        StatusEnum statusEnum = Optional.ofNullable(StatusEnum.getEnum(type)).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

        if (userInfo.getStatus().equals(type)) {
            throw new BusinessException("用户已经是" + statusEnum.getStatus() + "状态");
        }

        UserInfo updateInfo = new UserInfo();
        updateInfo.setStatus(type);
        Integer updateCount = this.userInfoMapper.updateByUserId(updateInfo, userId);
        if (updateCount == null || updateCount == 0) {
            throw new BusinessException("操作失败");
        }

        if (StatusEnum.DISABLE.equals(statusEnum)) {
            tokenRedisComponent.cleanUserLoginToken(userId);
        }
        return updateCount;
    }
}
