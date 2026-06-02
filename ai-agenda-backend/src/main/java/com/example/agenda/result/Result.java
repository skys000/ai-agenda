package com.example.agenda.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200成功，500失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据

    // 成功的方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    // 失败的方法
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        return result;
    }
}