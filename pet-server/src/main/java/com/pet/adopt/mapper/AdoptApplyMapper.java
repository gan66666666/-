package com.pet.adopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.adopt.entity.AdoptApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领养申请 Mapper
 */
@Mapper
public interface AdoptApplyMapper extends BaseMapper<AdoptApply> {
}
