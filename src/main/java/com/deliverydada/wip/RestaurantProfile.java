package com.deliverydada.wip;

import java.util.List;

public class RestaurantProfile {
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getRestaurantName() {
		return restaurantName;
	}
	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUpdatedDateTime() {
		return updatedDateTime;
	}
	public void setUpdatedDateTime(String updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}
	public List<Detail> getDetails() {
		return details;
	}
	public void setDetails(List<Detail> details) {
		this.details = details;
	}
	public String userName;
	public String restaurantName;
	public String requestId;
	public String status;
	public String updatedDateTime;
	public List<Detail> details;
}

class Detail {
	public String restaurantName;
	public List<String> restaurantType;
	public List<OwnerDetail> ownerDetails;
	public String getRestaurantName() {
		return restaurantName;
	}
	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}
	public List<String> getRestaurantType() {
		return restaurantType;
	}
	public void setRestaurantType(List<String> restaurantType) {
		this.restaurantType = restaurantType;
	}
	public List<OwnerDetail> getOwnerDetails() {
		return ownerDetails;
	}
	public void setOwnerDetails(List<OwnerDetail> ownerDetails) {
		this.ownerDetails = ownerDetails;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String status;
}

class OwnerDetail {
	public Name getName() {
		return name;
	}
	public void setName(Name name) {
		this.name = name;
	}
	public List<OwnerContactDetail> getOwnerContactDetails() {
		return ownerContactDetails;
	}
	public void setOwnerContactDetails(List<OwnerContactDetail> ownerContactDetails) {
		this.ownerContactDetails = ownerContactDetails;
	}
	public Name name;
	public List<OwnerContactDetail> ownerContactDetails;
}

class OwnerContactDetail {
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone1() {
		return phone1;
	}
	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}
	public String getPhone2() {
		return phone2;
	}
	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}
	public String email;
	public String phone1;
	public String phone2;
}

class Name {
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
	public String firstName;
	public String lastName;
}