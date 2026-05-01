package com.example.sashabf.DTO;

import com.example.sashabf.model.Priority;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDashboardDTO {
    private Long id;
    private String title;
    private Priority priority;
    
    
}
