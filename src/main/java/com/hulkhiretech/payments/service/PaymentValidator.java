package com.hulkhiretech.payments.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.pojo.CreateOrderReq;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentValidator {
	/*
	 * this method checks for incoming createOrder reqest for null,invalid values
	 * if their is any validation error it throws PaypalProviderException 
	 * if no error,then just runs till end and returns void
	 * */
	public void validateCreateOrderRequest(CreateOrderReq createOrderReq) {
		log.info("Validating CreateOrderReq: {}", createOrderReq);
		
		if (createOrderReq == null) {
			log.error("CreateOrderReq is null");
			
			throw new PaypalProviderException(
		            ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
		            ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
		            HttpStatus.BAD_REQUEST
		    );
		}
		
		//add null chek for amount
		if (createOrderReq.getAmount() == null 
				||createOrderReq.getAmount() <= 0) {
			log.error("Invalid amount in createOrderReq: {}", createOrderReq.getAmount());
			
			throw new PaypalProviderException(
		            ErrorCodeEnum.INVALID_AMOUNT.getErrorCode(),
		            ErrorCodeEnum.INVALID_AMOUNT.getErrorMessage(),
		            HttpStatus.BAD_REQUEST
		    );
		}
		
		if (createOrderReq.getCurrencyCode() == null || createOrderReq.getCurrencyCode().isBlank()) {
			log.error("Currency code is required field in createOrderReq");
			
			throw new PaypalProviderException(
		            ErrorCodeEnum.CURRENCY_CODE_REQUIRED.getErrorCode(),
		            ErrorCodeEnum.CURRENCY_CODE_REQUIRED.getErrorMessage(),
		            HttpStatus.BAD_REQUEST
		    );
		}
		
		//check returnUrl
		if (createOrderReq.getReturnUrl() == null || createOrderReq.getReturnUrl().isBlank()) {
			log.error("Return url is required field in createOrderReq");
			
			throw new PaypalProviderException(
		            ErrorCodeEnum.RETURN_URL_REQUIRED.getErrorCode(),
		            ErrorCodeEnum.RETURN_URL_REQUIRED.getErrorMessage(),
		            HttpStatus.BAD_REQUEST
		    );
		}
		//check cancel url
		if (createOrderReq.getCancelUrl() == null || createOrderReq.getCancelUrl().isBlank()) {
			log.error("Cancel url is required field in createOrderReq");
			
			throw new PaypalProviderException(
		            ErrorCodeEnum.CANCEL_URL_REQUIRED.getErrorCode(),
		            ErrorCodeEnum.CANCEL_URL_REQUIRED.getErrorMessage(),
		            HttpStatus.BAD_REQUEST
		    );
		}
		
		log.info("CreateOrderReq validation passed");
		
	}

}
