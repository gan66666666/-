package com.pet.adopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.adopt.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 普通用户 Mapper
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}

