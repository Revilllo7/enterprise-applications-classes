CREATE TABLE IF NOT EXISTS employees
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    position VARCHAR(100),
    company VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    department_id BIGINT foreign key references departments(id),
    photo_file_name VARCHAR(255)
);