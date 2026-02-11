package com.hulkhiretech.payments.service.impl;


import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	
	private final TokenService tokenService;

	@Override
	public String createOrder() {
		log.info("Creating order in PaymentServiceImpl");
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access token received : {}", accessToken);
		
		/* TODO
		  Steps to integrate with PayPal- all business logic to be implemented here
		  1. to get access token( implement OAuth layer) 
		  2. call paypal creteOrder
		  3. success/failure/timeout - proper response handling 
		  4. what to return to your calling service(Payment-processing-service)
		 */
		
		
		
		return "Order created from service-"+ accessToken;
	}
	
	@PostConstruct
	public void init() {
		log.info("PaymentServiceImpl initialized");
	}
}
