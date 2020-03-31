package org.olf.rs.mockncip.models;



public class Loan {
	
	private String id;
	private String dueDate;
	private String itemBarcode;
	private String patronBarcode;
	private String loanStatus;
	
	
	
	public String getLoanStatus() {
		return loanStatus;
	}
	public void setLoanStatus(String loanStatus) {
		this.loanStatus = loanStatus;
	}

	
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getItemBarcode() {
		return itemBarcode;
	}
	public void setItemBarcode(String itemBarcode) {
		this.itemBarcode = itemBarcode;
	}
	public String getPatronBarcode() {
		return patronBarcode;
	}
	public void setPatronBarcode(String patronBarcode) {
		this.patronBarcode = patronBarcode;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	

}
