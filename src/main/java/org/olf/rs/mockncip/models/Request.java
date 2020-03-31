package org.olf.rs.mockncip.models;

import java.util.Date;

public class Request {
	
	private String id;
	private String patronBarcode;
	private String requestDate;
	private String itemBarcode;
	private String pickupLocation;
	private String requestStatus;
	
	
	
	public String getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}
	public String getPickupLocation() {
		return pickupLocation;
	}
	public void setPickupLocation(String pickupLocation) {
		this.pickupLocation = pickupLocation;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPatronBarcode() {
		return patronBarcode;
	}
	public void setPatronBarcode(String patronBarcode) {
		this.patronBarcode = patronBarcode;
	}

	public String getItemBarcode() {
		return itemBarcode;
	}
	public void setItemBarcode(String itemBarcode) {
		this.itemBarcode = itemBarcode;
	}
	public String getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	
	
	
	

}
