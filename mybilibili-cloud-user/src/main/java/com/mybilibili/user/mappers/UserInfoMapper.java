package com.mybilibili.user.mappers;


import com.mybilibili.common.mappers.BaseMapper;
import com.mybilibili.user.entity.dto.UserCoinCountUpdateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author amani
 * @date 2026/01/07
 * @description 
 */
@Mapper
public interface UserInfoMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 UserId查询
	 */
	T selectByUserId(@Param("userId") String userId);

	/**
	 * @description 根据 UserId更新
	 */
	Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

	/**
	 * @description 根据 UserId删除
	 */
	Integer deleteByUserId(@Param("userId") String userId);


	/**
	 * @description 根据 Email查询
	 */
	T selectByEmail(@Param("email") String email);

	/**
	 * @description 根据 Email更新
	 */
	Integer updateByEmail(@Param("bean") T t, @Param("email") String email);

	/**
	 * @description 根据 Email删除
	 */
	Integer deleteByEmail(@Param("email") String email);


	/**
	 * @description 根据 NickName查询
	 */
	T selectByNickName(@Param("nickName") String nickName);

	/**
	 * @description 根据 NickName更新
	 */
	Integer updateByNickName(@Param("bean") T t, @Param("nickName") String nickName);

	/**
	 * @description 根据 NickName删除
	 */
	Integer deleteByNickName(@Param("nickName") String nickName);


	Integer updateUserCoin(@Param("userId") String userId, @Param("coinCount") Integer actionCount);

    Integer selectTotalCoinCount(@Param("userId") String userId);

    Integer updateCountBatch(@Param("list") List<UserCoinCountUpdateDTO> updateList);

	Integer selectTotalUserCount();

	Integer selectRegisterCountUntilDate(@Param("statsDay") Date statsDay);

	Integer selectRegisterCountByDate(@Param("statsDay") Date statsDay);

	List<Map<String, Object>> selectDailyRegisterCountByDateRange(@Param("startDay") Date startDay, @Param("endDay") Date endDay);
}
