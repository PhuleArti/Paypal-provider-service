package com.hulkhiretech.payments.pojo;

import lombok.Data;

@Data
public class CreateOrderReq {
	
	private Double amount;
	private String currencyCode;
	private String returnUrl;
	private String cancelUrl;

}
