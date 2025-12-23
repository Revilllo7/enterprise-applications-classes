package com.techcorp.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.techcorp.employee.validation.TechCorpEmail;

public class EmployeeDTO {
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String company;
	private String position;
	private double salary;
	private String status;

	public EmployeeDTO() {

    }

	public EmployeeDTO(Long id, String firstName, String lastName, String email, String company, String position, double salary, String status) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.company = company;
		this.position = position;
		this.salary = salary;
		this.status = status;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	@NotBlank(message = "Imię jest wymagane")
	@Size(min = 2, message = "Imię musi mieć co najmniej 2 znaki")
	public String getFirstName() { 
	    return firstName; 
	}

	public void setFirstName(String firstName) { 
	    this.firstName = firstName; 
	}

	@NotBlank(message = "Nazwisko jest wymagane")
	@Size(min = 2, message = "Nazwisko musi mieć co najmniej 2 znaki")
	public String getLastName() { 
        return lastName; 
    }

	public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }

	@NotBlank(message = "Email jest wymagany")
	@Email(message = "Nieprawidłowy format email")
	@TechCorpEmail(message = "Email musi kończyć się na @techcorp.com")
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

	@NotBlank(message = "Stanowisko jest wymagane")
	public String getPosition() { 
        return position; 
    }

	public void setPosition(String position) { 
        this.position = position; 
    }

	@Positive(message = "Wynagrodzenie musi być większe od 0")
	public double getSalary() { 
	    return salary; 
	}

	public void setSalary(double salary) { 
	    this.salary = salary; 
	}

	@NotBlank(message = "Status jest wymagany")
	public String getStatus() { 
        return status; 
    }

    public void setStatus(String status) { 
        this.status = status; 
    }
}
