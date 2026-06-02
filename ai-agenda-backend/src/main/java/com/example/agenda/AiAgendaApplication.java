package com.example.agenda;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.agenda.**.mapper") // 加上这一行，扫描所有的Mapper接口
public class AiAgendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgendaApplication.class, args);
    }

}