# How to run?

## Run main application:
```bash
mvn -DskipTests=true spring-boot:run
```

## Enable bootstrap (import sample data on startup):
```bash
mvn -DskipTests=true -Dapp.bootstrap.enabled=true spring-boot:run
```

## Disable bootstrap (don't import data on startup):
```bash
mvn -DskipTests=true -Dapp.bootstrap.enabled=false spring-boot:run
```

## Run Unit tests:
```bash
mvn test
```

## Run tests with code coverage:
```bash
mvn verify
```

# REST Endpoints

Base URL: `http://localhost:8080`

Employee management endpoints (`/api/employees`):

- GET all employees (optional company filter):

	Request:

	```bash
	curl -s http://localhost:8080/api/employees
	# or filtered by company
	curl -s "http://localhost:8080/api/employees?company=TechCorp"
	```

	Response: 200 OK, JSON array of `EmployeeDTO` objects.

- POST create new employee:

	```bash
	curl -i -X POST http://localhost:8080/api/employees \
		-H "Content-Type: application/json" \
		-d '{"firstName":"Jan","lastName":"Kowalski","email":"jan@example.com","company":"TechCorp","position":"PROGRAMISTA","salary":8000.0,"status":"ACTIVE"}'
	```

	Response: 201 Created, `Location` header with new resource and JSON body of created `EmployeeDTO`. 409 Conflict if duplicate, 400 Bad Request for invalid payload.

- GET employee by email:

	```bash
	curl -s http://localhost:8080/api/employees/jan@example.com
	```

	Response: 200 OK with employee JSON or 404 Not Found.



- PUT update employee:

	```bash
	curl -i -X PUT http://localhost:8080/api/employees/jan@example.com \
		-H "Content-Type: application/json" \
		-d '{"firstName":"Jan","lastName":"Kowalski","email":"jan@example.com","company":"TechCorp","position":"PROGRAMISTA","salary":9000.0,"status":"ACTIVE"}'
	```

	Response: 200 OK with updated `EmployeeDTO` or 404 Not Found.

- DELETE employee:

	```bash
	curl -i -X DELETE http://localhost:8080/api/employees/jan@example.com
	```

	Response: 204 No Content on success, 404 Not Found otherwise.

- PATCH change employment status:

	```bash
	curl -i -X PATCH http://localhost:8080/api/employees/jan@example.com/status \
		-H "Content-Type: application/json" \
		-d '{"status":"ON_LEAVE"}'
	```

	Response: 200 OK with updated `EmployeeDTO`, 400 Bad Request for invalid status, 404 Not Found if employee missing.

- GET employees by status:

	```bash
	curl -s http://localhost:8080/api/employees/status/ACTIVE
	```

	Response: 200 OK with JSON array of matching employees.

Statistics endpoints (`/api/statistics`):

- GET average salary (global):

	```bash
	curl -s http://localhost:8080/api/statistics/salary/average
	```

	Response: 200 OK with JSON object: `{ "averageSalary": 10052.63 }`.

- GET average salary for a specific company:

	```bash
	curl -s "http://localhost:8080/api/statistics/salary/average?company=TechCorp"
	```
    Response: 200 OK with JSON object: `{ "averageSalary": 10166.67 }`.

- GET company statistics (detailed):

	```bash
	curl -s http://localhost:8080/api/statistics/company/TechCorp
	```

	Response: 200 OK with `CompanyStatisticsDTO` JSON (companyName, employeeCount, averageSalary, highestSalary, topEarnerName)
 `{"companyName":"TechCorp","employeeCount":6,"averageSalary":10166.666666666666,"highestSalary":20000.0,"topEarnerName":"Wojtek Kamiński"}`

- GET counts per position:

	```bash
	curl -s http://localhost:8080/api/statistics/positions
	```

	Response: 200 OK with JSON map `{"PREZES":1,"PROGRAMISTA":15,"MANAGER":3}`.

- GET status distribution:

	```bash
	curl -s http://localhost:8080/api/statistics/status
	```

	Response: 200 OK with JSON map `{"ACTIVE":18,"ON_LEAVE":1}`.


---

## Code Coverage Report
Zad-5 code coverage report: ![coverage report showing 68% coverage](markdown/coverage-report.png)

