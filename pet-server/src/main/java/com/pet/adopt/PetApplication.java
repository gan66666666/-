package com.pet.adopt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物领养管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.pet.adopt.mapper")
public class PetApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetApplication.class, args);
        System.out.println("宠物领养管理系统启动成功");
    }
}
