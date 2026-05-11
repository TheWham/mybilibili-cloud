package com.mybilibili.user.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.VideoCountDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.query.SimplePage;
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
import com.mybilibili.user.consumer.VideoInfoClient;
import com.mybilibili.user.component.UserRedisComponent;
import com.mybilibili.user.component.UserStatsCacheAsyncComponent;
import com.mybilibili.user.entity.po.UserFocus;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.entity.po.UserStats;
import com.mybilibili.user.entity.query.UserFocusQuery;
import com.mybilibili.user.entity.vo.TotalCountInfoVO;
import com.mybilibili.user.entity.vo.UCenterVideoDateVO;
import com.mybilibili.user.entity.vo.UCenterVideoWeekCountVO;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("UserInfoService")
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
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
    @Resource
    private VideoInfoClient videoInfoClient;

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

        fillVideoCount(userCountVO, userId);
        return userCountVO;
    }

    private void fillVideoCount(UserCountVO userCountVO, String userId) {
        VideoCountDTO videoCountDTO = null;
        try {
            // 用户主页冷启动时只缺少展示统计，video 服务短暂异常不应该影响用户基础信息返回。
            videoCountDTO = videoInfoClient.countVideoInfoByUserId(userId);
        } catch (Exception e) {
            log.warn("查询用户视频统计失败，userId:{}", userId, e);
        }
        if (videoCountDTO == null) {
            userCountVO.setLikeCount(0);
            userCountVO.setPlayCount(0);
            return;
        }
        userCountVO.setLikeCount(Optional.ofNullable(videoCountDTO.getTotalLikeCount()).orElse(0));
        userCountVO.setPlayCount(Optional.ofNullable(videoCountDTO.getTotalPlayCount()).orElse(0));
    }

    private void fillUserInfoVOWithRealtimeStats(UserInfoVO userInfoVO, HashMap<String, Integer> userStatsMap) {
        userInfoVO.setFocusCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FOCUS.getField(), 0));
        userInfoVO.setFansCount(userStatsMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
        userInfoVO.setLikeCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
        userInfoVO.setPlayCount(userStatsMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
    }

    @Override
    public UCenterVideoDateVO getActualTimeStatisticsInfo(String userId) {
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Optional.ofNullable(userInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

        UCenterVideoDateVO dateVO = new UCenterVideoDateVO();
        dateVO.setPreDayData(new Integer[]{0, 0, 0, 0, 0, 0, 0});

        HashMap<String, Integer> realtimeStatsMap = userRedisComponent.getRealtimeUserStatsInfo(userId);
        if (realtimeStatsMap != null && !realtimeStatsMap.isEmpty()) {
            userRedisComponent.refreshRealtimeUserStatsExpire(userId);
            dateVO.setTotalCountInfo(buildTotalCountInfoFromRedis(realtimeStatsMap));
            return dateVO;
        }

        UserStats userStats = userStatsMapper.selectLatestByUserId(userId);
        dateVO.setTotalCountInfo(buildTotalCountInfoFromDb(userStats));
        return dateVO;
    }

    @Override
    public List<UCenterVideoWeekCountVO> getWeekStatisticsInfo(UserStatsRedisEnum statsType, String userId) {
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Optional.ofNullable(userInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

        UserStatsQuery query = new UserStatsQuery();
        query.setUserId(userId);
        query.setPageNo(1);
        query.setPageSize(7);
        query.setOrderBy("v.stats_day desc");
        List<UserStats> statsList = userStatsMapper.selectList(query);

        // 补齐最近 7 天的数据点，前端画趋势图时不需要再处理缺失日期。
        Map<LocalDate, Integer> countMap = statsList.stream()
                .filter(userStats -> userStats.getStatsDay() != null)
                .collect(Collectors.toMap(userStats -> userStats.getStatsDay().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        userStats -> getStatsCountByType(userStats, statsType),
                        (oldValue, newValue) -> newValue));

        List<UCenterVideoWeekCountVO> result = new ArrayList<>(7);
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate statisticsDate = today.minusDays(i);
            UCenterVideoWeekCountVO weekCountVO = new UCenterVideoWeekCountVO();
            weekCountVO.setStatisticsDate(Date.from(statisticsDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            weekCountVO.setStatisticsCount(countMap.getOrDefault(statisticsDate, 0));
            result.add(weekCountVO);
        }
        return result;
    }

    private TotalCountInfoVO buildTotalCountInfoFromRedis(HashMap<String, Integer> statsMap) {
        TotalCountInfoVO totalCountInfoVO = new TotalCountInfoVO();
        totalCountInfoVO.setFansCount(statsMap.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
        totalCountInfoVO.setPlayCount(statsMap.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
        totalCountInfoVO.setCommentCount(statsMap.getOrDefault(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), 0));
        totalCountInfoVO.setDanmuCount(statsMap.getOrDefault(UserStatsRedisEnum.VIDEO_DANMU.getField(), 0));
        totalCountInfoVO.setLikeCount(statsMap.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
        totalCountInfoVO.setCoinCount(statsMap.getOrDefault(UserStatsRedisEnum.VIDEO_COIN.getField(), 0));
        totalCountInfoVO.setCollectCount(statsMap.getOrDefault(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), 0));
        return totalCountInfoVO;
    }

    private TotalCountInfoVO buildTotalCountInfoFromDb(UserStats userStats) {
        if (userStats == null) {
            return new TotalCountInfoVO(0, 0, 0, 0, 0, 0, 0);
        }
        return BeanUtil.toBean(userStats, TotalCountInfoVO.class);
    }

    private Integer getStatsCountByType(UserStats userStats, UserStatsRedisEnum statsType) {
        if (statsType == null || userStats == null) {
            return 0;
        }
        return switch (statsType) {
            case VIDEO_LIKE -> Optional.ofNullable(userStats.getLikeCount()).orElse(0);
            case VIDEO_PLAY -> Optional.ofNullable(userStats.getPlayCount()).orElse(0);
            case VIDEO_DANMU -> Optional.ofNullable(userStats.getDanmuCount()).orElse(0);
            case VIDEO_COIN -> Optional.ofNullable(userStats.getCoinCount()).orElse(0);
            case USER_FOCUS -> Optional.ofNullable(userStats.getFocusCount()).orElse(0);
            case USER_FANS -> Optional.ofNullable(userStats.getFansCount()).orElse(0);
            case USER_COIN -> Optional.ofNullable(userStats.getCurrentCoinCount()).orElse(0);
            case USER_COMMENT_COUNT -> Optional.ofNullable(userStats.getCommentCount()).orElse(0);
            case USER_COLLECT_COUNT -> Optional.ofNullable(userStats.getCollectCount()).orElse(0);
        };
    }

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
