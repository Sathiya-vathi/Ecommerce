package com.website.dto;


public class UpdateOrderStatusDTO {
    private Long orderId;
    private String status;

    public UpdateOrderStatusDTO() {}

    // Getters and Setters
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
