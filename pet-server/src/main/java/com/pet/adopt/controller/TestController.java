package com.pet.adopt.controller;

import com.pet.adopt.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 骨架验证接口
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("后端骨架搭建成功");
    }
}
