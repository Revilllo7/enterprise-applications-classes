package com.techcorp.employee.model;

import java.time.Instant;

public class EmployeeDocument {
	private long id;
	private String employeeEmail;
	private String fileName; // stored unique file name or relative path
	private String originalFileName;
	private DocumentType fileType;
	private Instant uploadDate;
	private String filePath; // relative path under uploads root (e.g. documents/email/uuid.pdf)

	public EmployeeDocument() {}

	public EmployeeDocument(long id, String employeeEmail, String fileName, String originalFileName, DocumentType fileType, Instant uploadDate, String filePath) {
		this.id = id;
		this.employeeEmail = employeeEmail;
		this.fileName = fileName;
		this.originalFileName = originalFileName;
		this.fileType = fileType;
		this.uploadDate = uploadDate;
		this.filePath = filePath;
	}

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }

	public String getEmployeeEmail() { return employeeEmail; }
	public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }

	public String getOriginalFileName() { return originalFileName; }
	public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

	public DocumentType getFileType() { return fileType; }
	public void setFileType(DocumentType fileType) { this.fileType = fileType; }

	public Instant getUploadDate() { return uploadDate; }
	public void setUploadDate(Instant uploadDate) { this.uploadDate = uploadDate; }

	public String getFilePath() { return filePath; }
	public void setFilePath(String filePath) { this.filePath = filePath; }
}
