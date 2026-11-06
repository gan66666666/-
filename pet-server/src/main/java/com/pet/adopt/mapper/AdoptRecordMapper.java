package com.pet.adopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.adopt.entity.AdoptRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领养记录 Mapper
 */
@Mapper
public interface AdoptRecordMapper extends BaseMapper<AdoptRecord> {
}
