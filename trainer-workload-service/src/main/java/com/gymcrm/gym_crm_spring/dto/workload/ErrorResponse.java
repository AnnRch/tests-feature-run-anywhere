package com.gymcrm.gym_crm_spring.dto.workload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
}
