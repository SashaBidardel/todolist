package com.example.sashabf.DTO;

import com.example.sashabf.model.Priority;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor

/**
 * Justificación de uso:
 * Se utiliza este DTO para optimizar la carga del Dashboard. En lugar de enviar la entidad 
 * 'Task' completa (que podría contener descripciones largas, relaciones pesadas o fechas 
 * innecesarias), solo se transfieren los atributos críticos (id, título y prioridad).

 */
public class TaskDashboardDTO {
    private Long id;
    private String title;
    private Priority priority;
    
    
}
