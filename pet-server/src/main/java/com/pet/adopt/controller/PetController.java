package com.pet.adopt.controller;

import com.pet.adopt.dto.PetDTO;
import com.pet.adopt.dto.PetQueryDTO;
import com.pet.adopt.service.PetService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 宠物档案接口
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/pet/**
 */
@RestController
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * 分页查询宠物档案
     */
    @GetMapping("/page")
    public Result<PageResult<PetDTO>> page(PetQueryDTO query) {
        return Result.success(petService.pagePets(query));
    }

    /**
     * 查询宠物详情
     */
    @GetMapping("/{id}")
    public Result<PetDTO> detail(@PathVariable("id") Long id) {
        return Result.success(petService.getPetById(id));
    }

    /**
     * 新增宠物
     */
    @PostMapping
    public Result<Void> add(@RequestBody PetDTO petDTO) {
        petService.addPet(petDTO);
        return Result.success("新增成功", null);
    }

    /**
     * 修改宠物
     */
    @PutMapping
    public Result<Void> update(@RequestBody PetDTO petDTO) {
        petService.updatePet(petDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 修改宠物状态
     */
    @PutMapping("/status/{id}/{status}")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @PathVariable("status") Integer status) {
        petService.updateStatus(id, status);
        return Result.success("修改成功", null);
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        petService.deletePet(id);
        return Result.success("删除成功", null);
    }
}
