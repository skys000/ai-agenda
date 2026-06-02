package com.example.agenda.pojo.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignMenuDTO {
    private Long roleId;
    private List<Long> menuIds;
}