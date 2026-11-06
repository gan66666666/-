package com.pet.adopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.adopt.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物 Mapper
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
