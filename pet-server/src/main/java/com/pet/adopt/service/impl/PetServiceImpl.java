package com.pet.adopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.adopt.dto.PetDTO;
import com.pet.adopt.dto.PetQueryDTO;
import com.pet.adopt.entity.Pet;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.PetMapper;
import com.pet.adopt.service.PetService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 宠物档案服务实现
 */
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    /** 允许的宠物类型 */
    private static final Set<String> ALLOWED_TYPES =
            Set.of("狗狗", "猫咪", "兔子", "其他");

    /** 允许的领养状态：1可领养/2已领养/3救助中 */
    private static final Set<Integer> ALLOWED_STATUS = Set.of(1, 2, 3);

    private final PetMapper petMapper;

    @Override
    public PageResult<PetDTO> pagePets(PetQueryDTO query) {
        PetQueryDTO safeQuery = query == null ? new PetQueryDTO() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1
                ? 1L : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
                ? 10L : safeQuery.getPageSize();

        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getType())) {
            wrapper.eq(Pet::getType, safeQuery.getType().trim());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(Pet::getStatus, safeQuery.getStatus());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(Pet::getName, keyword).or().like(Pet::getBreed, keyword));
        }
        wrapper.orderByDesc(Pet::getCreateTime).orderByDesc(Pet::getId);

        Page<Pet> page = petMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<PetDTO> records = page.getRecords().stream()
                .map(this::petToDTO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    public PetDTO getPetById(Long id) {
        if (id == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物信息不存在");
        }
        return petToDTO(pet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPet(PetDTO petDTO) {
        validatePetDTO(petDTO);

        Pet pet = new Pet();
        // 新增场景忽略前端传入的 id，主键一律交由数据库自增生成，避免主键冲突与数据错乱
        pet.setId(null);
        pet.setName(petDTO.getName().trim());
        pet.setType(petDTO.getType().trim());
        pet.setBreed(petDTO.getBreed());
        pet.setAge(petDTO.getAge());
        pet.setGender(petDTO.getGender());
        pet.setAvatar(petDTO.getAvatar());
        pet.setHealth(petDTO.getHealth());
        pet.setStatus(petDTO.getStatus() == null ? 1 : petDTO.getStatus());
        pet.setCreateTime(LocalDateTime.now());
        petMapper.insert(pet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePet(PetDTO petDTO) {
        validatePetDTO(petDTO);
        if (petDTO.getId() == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        if (petMapper.selectById(petDTO.getId()) == null) {
            throw new BusinessException("宠物信息不存在");
        }

        Pet pet = new Pet();
        pet.setId(petDTO.getId());
        pet.setName(petDTO.getName().trim());
        pet.setType(petDTO.getType().trim());
        pet.setBreed(petDTO.getBreed());
        pet.setAge(petDTO.getAge());
        pet.setGender(petDTO.getGender());
        pet.setAvatar(petDTO.getAvatar());
        pet.setHealth(petDTO.getHealth());
        if (petDTO.getStatus() != null) {
            pet.setStatus(petDTO.getStatus());
        }
        petMapper.updateById(pet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        if (status == null || !ALLOWED_STATUS.contains(status)) {
            throw new BusinessException("宠物状态只能是1可领养/2已领养/3救助中");
        }
        if (petMapper.selectById(id) == null) {
            throw new BusinessException("宠物信息不存在");
        }

        Pet pet = new Pet();
        pet.setId(id);
        pet.setStatus(status);
        petMapper.updateById(pet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePet(Long id) {
        if (id == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        if (petMapper.selectById(id) == null) {
            throw new BusinessException("宠物信息不存在");
        }
        petMapper.deleteById(id);
    }

    /**
     * 实体转 DTO
     */
    private PetDTO petToDTO(Pet pet) {
        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setType(pet.getType());
        dto.setBreed(pet.getBreed());
        dto.setAge(pet.getAge());
        dto.setGender(pet.getGender());
        dto.setAvatar(pet.getAvatar());
        dto.setHealth(pet.getHealth());
        dto.setStatus(pet.getStatus());
        return dto;
    }

    /**
     * 基础入参校验
     */
    private void validatePetDTO(PetDTO petDTO) {
        if (petDTO == null
                || !StringUtils.hasText(petDTO.getName())
                || !StringUtils.hasText(petDTO.getType())) {
            throw new BusinessException("宠物名称和类型不能为空");
        }
        // 字段长度与 pet 表的列宽保持一致，超长直接给友好提示，避免落库时炸成 500
        ValidationUtil.maxLength(petDTO.getName(), 50, "宠物名称");
        ValidationUtil.maxLength(petDTO.getType(), 20, "宠物类型");
        ValidationUtil.maxLength(petDTO.getBreed(), 50, "品种");
        ValidationUtil.maxLength(petDTO.getGender(), 10, "性别");
        ValidationUtil.maxLength(petDTO.getAvatar(), 255, "图片地址");
        ValidationUtil.maxLength(petDTO.getHealth(), 255, "健康状况");
        String type = petDTO.getType().trim();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException("宠物类型只能是狗狗/猫咪/兔子/其他");
        }
        if (petDTO.getStatus() != null && !ALLOWED_STATUS.contains(petDTO.getStatus())) {
            throw new BusinessException("宠物状态只能是1可领养/2已领养/3救助中");
        }
    }
}
