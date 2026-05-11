package com.mybilibili.user.services.impl;

import com.mybilibili.base.entity.event.UserCoinSyncEvent;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.user.entity.dto.UserCoinCountUpdateDTO;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.mappers.UserInfoMapper;
import com.mybilibili.user.services.UserCoinSyncService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户硬币同步实现。
 *
 * <p>普通投币是“投币人扣 current，作者加 current 和 total”；审核奖励只有作者增加。
 * 这里先聚合再批量更新，后续如果消费者改成批量监听也不用重写核心逻辑。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Service
public class UserCoinSyncServiceImpl implements UserCoinSyncService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserCoin(UserCoinSyncEvent event) {
        if (event.getActionCount() == null || event.getActionCount() <= 0 || event.getVideoUserId() == null) {
            return;
        }

        Map<String, Integer> currentCoinMap = new LinkedHashMap<>();
        Map<String, Integer> totalCoinMap = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(event.getAuditReward())) {
            mergeCount(currentCoinMap, event.getVideoUserId(), event.getActionCount());
            mergeCount(totalCoinMap, event.getVideoUserId(), event.getActionCount());
        } else {
            if (event.getUserId() == null) {
                return;
            }
            mergeCount(currentCoinMap, event.getUserId(), -event.getActionCount());
            mergeCount(currentCoinMap, event.getVideoUserId(), event.getActionCount());
            mergeCount(totalCoinMap, event.getVideoUserId(), event.getActionCount());
        }

        List<UserCoinCountUpdateDTO> updateList = buildUpdateList(currentCoinMap, totalCoinMap);
        if (!updateList.isEmpty()) {
            userInfoMapper.updateCountBatch(updateList);
        }
    }

    private void mergeCount(Map<String, Integer> countMap, String userId, Integer count) {
        if (userId == null || count == null || count == 0) {
            return;
        }
        countMap.merge(userId, count, Integer::sum);
    }

    private List<UserCoinCountUpdateDTO> buildUpdateList(Map<String, Integer> currentCoinMap,
                                                         Map<String, Integer> totalCoinMap) {
        List<UserCoinCountUpdateDTO> updateList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : currentCoinMap.entrySet()) {
            String userId = entry.getKey();
            Integer currentCoinCount = entry.getValue();
            Integer totalCoinCount = totalCoinMap.getOrDefault(userId, 0);
            if (currentCoinCount == 0 && totalCoinCount == 0) {
                continue;
            }
            updateList.add(new UserCoinCountUpdateDTO(userId, totalCoinCount, currentCoinCount));
        }
        for (Map.Entry<String, Integer> entry : totalCoinMap.entrySet()) {
            String userId = entry.getKey();
            if (currentCoinMap.containsKey(userId) || entry.getValue() == 0) {
                continue;
            }
            updateList.add(new UserCoinCountUpdateDTO(userId, entry.getValue(), 0));
        }
        return updateList;
    }
}
