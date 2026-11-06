package com.pet.adopt.service;

import com.pet.adopt.dto.PetDTO;
import com.pet.adopt.dto.PetQueryDTO;
import com.pet.adopt.util.PageResult;

/**
 * 宠物档案服务
 */
public interface PetService {

    /**
     * 分页查询宠物档案，支持类型、状态精确筛选与关键字模糊匹配
     */
    PageResult<PetDTO> pagePets(PetQueryDTO query);

    /**
     * 按ID查询宠物详情
     */
    PetDTO getPetById(Long id);

    /**
     * 新增宠物档案
     */
    void addPet(PetDTO petDTO);

    /**
     * 修改宠物档案
     */
    void updatePet(PetDTO petDTO);

    /**
     * 修改宠物领养状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除宠物档案
     */
    void deletePet(Long id);
}
