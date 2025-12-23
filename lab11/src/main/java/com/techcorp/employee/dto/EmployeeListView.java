package com.techcorp.employee.dto;

import com.techcorp.employee.model.Position;

// Lightweight projection for list view: only required columns
public interface EmployeeListView {
    String getFirstName();
    String getLastName();
    Position getPosition();
    String getDepartmentName();
}
 
