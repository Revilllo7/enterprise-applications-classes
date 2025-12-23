package com.techcorp.employee.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateDTO {
    private String status;

    public StatusUpdateDTO() {}

    public StatusUpdateDTO(String status) { this.status = status; }

    @NotBlank(message = "Status jest wymagany")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
