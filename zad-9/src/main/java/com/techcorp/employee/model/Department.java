package com.techcorp.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "departments")
public class Department {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Nazwa departamentu jest wymagana")
	private String name;

	private String location;

	@DecimalMin(value = "0.0", message = "Budżet musi być nieujemny")
	private double budget;

	private String managerEmail;

	@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Employee> employees = new ArrayList<>();

	public Department() {}

	public Department(Long id, String name, String location, double budget, String managerEmail) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.budget = budget;
		this.managerEmail = managerEmail;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getLocation() { return location; }
	public void setLocation(String location) { this.location = location; }

	public double getBudget() { return budget; }
	public void setBudget(double budget) { this.budget = budget; }

	public String getManagerEmail() { return managerEmail; }
	public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }

	public List<Employee> getEmployees() { return employees; }
	public void setEmployees(List<Employee> employees) { this.employees = employees; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Department)) return false;
		Department that = (Department) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() { return Objects.hash(id); }
}
