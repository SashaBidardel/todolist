package com.example.sashabf.DTO;


import lombok.Data;
import java.util.List;

@Data
/**
 * Justificación de uso:
 * Al enviar las tareas ya agrupadas por prioridad (low, medium, high), evitamos que el Frontend 
 * tenga que realizar cálculos o filtros adicionales sobre una lista plana. 
 * Facilita una implementación inmediata del tablero visual (tipo Kanban o listas por columnas).
 */
public class PriorityGroupDTO {
    private List<TaskDashboardDTO> low;
    private List<TaskDashboardDTO> medium;
    private List<TaskDashboardDTO> high;
}
