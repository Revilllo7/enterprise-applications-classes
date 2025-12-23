package com.techcorp.employee.dto;

public class EmployeeDTO {
	private String firstName;
	private String lastName;
	private String email;
	private String company;
	private String position;
	private double salary;
	private String status;

	public EmployeeDTO() {

    }

	public EmployeeDTO(String firstName, String lastName, String email, String company, String position, double salary, String status) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.company = company;
		this.position = position;
		this.salary = salary;
		this.status = status;
	}

	public String getFirstName() { 
        return firstName; 
    }

	public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }

	public String getLastName() { 
        return lastName; 
    }

	public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }

	public String getEmail() { 
        return email; 
    }

	public void setEmail(String email) { 
        this.email = email; 
    }

	public String getCompany() { 
        return company; 
    }

	public void setCompany(String company) { 
        this.company = company; 
    }

	public String getPosition() { 
        return position; 
    }

	public void setPosition(String position) { 
        this.position = position; 
    }

	public double getSalary() { 
        return salary; 
    }

	public void setSalary(double salary) { 
        this.salary = salary; 
    }

	public String getStatus() { 
        return status; 
    }

    public void setStatus(String status) { 
        this.status = status; 
    }
}
