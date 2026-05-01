package com.example.sashabf.DTO;


import lombok.Data;
import java.util.List;

@Data
public class PriorityGroupDTO {
    private List<TaskDashboardDTO> low;
    private List<TaskDashboardDTO> medium;
    private List<TaskDashboardDTO> high;
}
