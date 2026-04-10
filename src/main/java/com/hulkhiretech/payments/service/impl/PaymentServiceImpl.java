package com.hulkhiretech.payments.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.PaymentValidator;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.helper.CaptureOrderHelper;
import com.hulkhiretech.payments.service.helper.CreateOrderHelper;
import com.hulkhiretech.payments.service.interfaces.PaymentService;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final TokenService tokenService;

	private final HttpServiceEngine httpServiceEngine;

	private final JsonUtil jsonUtil;

	@Value("${paypal.create-order-url}")
	private String createOrderUrl;

	private final CreateOrderHelper createOrderHelper;
	
	private final CaptureOrderHelper captureOrderHelper;

	private final PaymentValidator paymentValidator;
	
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {
		log.info("Creating order in PaymentServiceImpl || createOrderReq: {}", createOrderReq);
		
		paymentValidator.validateCreateOrderRequest(createOrderReq);
		log.info("CreateOrderReqest validated successfully");

		String accessToken = tokenService.getAccessToken();
		//log.info("Access token received : {}", accessToken);
		log.info("Access token retrieved successfully.");

		HttpRequest httpRequest = createOrderHelper.prepareCreateOrderHttpRequest(createOrderReq, accessToken);
		log.info(" Prepared HttpRequest for httpServiceEngine : {}",httpRequest);

		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("Httpresponse from httpServiceEngine : {}", httpResponse);
		
		return handleCreateOrderResponse(httpResponse);

		//OrderResponse orderResponse = handleCreateOrderPaypalResponse(httpResponse);
		//log.info("OrderResponse created from Paypal response : {}", orderResponse);
		//return orderResponse;
	}
	@Override
	public OrderResponse captureOrder(String orderId) {
		log.info("Capturing order in PaymentServiceImpl|| orderId:{}",
				orderId);
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access token retrieved: {}", accessToken);
		
		HttpRequest httpRequest = captureOrderHelper.prepareCaptureOrderHttpRequest(
				orderId, accessToken);
		log.info("Prepared HttpRequest for capturing order httpRequest: {}", httpRequest);
		
		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP response from HttpServiceEngine: {}", httpResponse);
		
		 return handleCaptureOrderResponse(httpResponse);
		//OrderResponse orderResponse = handleCreateOrderPaypalResponse(httpResponse);
		//log.info("Final OrderResponse to be returned: {}", orderResponse);
		
		//return orderResponse;
	}

	private OrderResponse handleCreateOrderResponse(ResponseEntity<String> httpResponse) {
		log.info("Handling Paypal response in paypalServiceImpl || httpResponse: {}", httpResponse);
		
		if(httpResponse.getStatusCode().is2xxSuccessful()) { //Success
			
			PaypalOrder paypalOrder = jsonUtil.fromJson(
					httpResponse.getBody(), PaypalOrder.class);
			log.info("Converted response body to PaypalOrder: {}", paypalOrder);
			
			OrderResponse orderResponse = createOrderHelper.toOrderResponse(paypalOrder);
			log.info("Converted orderResponse : {}", orderResponse);
			
			if(isValidCreateOrder(orderResponse)) {
				log.info("OrderResponse is valid and complete. Returning orderResponse.");
				return orderResponse;
		
		 }
	}
		// Everything else is an error
		throw new RuntimeException(
				"Failed to create order in paypal: "
		       +" Status code :"+ httpResponse.getStatusCode()
		       +", Response body: "+ httpResponse.getBody());

	}
	

	private OrderResponse handleCaptureOrderResponse(ResponseEntity<String> httpResponse) {

	    log.info("Handling capture order response || httpResponse: {}", httpResponse);

	    if (httpResponse.getStatusCode().is2xxSuccessful()) {
	    	
	    	 PaypalOrder paypalOrder = jsonUtil.fromJson(
	                 httpResponse.getBody(), PaypalOrder.class);

	        // ✅ Let helper map correctly
	    	 OrderResponse orderResponse = captureOrderHelper.toOrderResponse(paypalOrder);
	        log.info("Converted OrderResponse: {}", orderResponse);
	        
	        if (isValidCaptureOrder(orderResponse)) {
                return orderResponse;
            }
	    }

	    throw new RuntimeException(
	            "Failed to capture order in paypal: "
	                    + " Status code :" + httpResponse.getStatusCode()
	                    + ", Response body: " + httpResponse.getBody());
	}
	
	
	  private boolean isValidCreateOrder(OrderResponse res) {
	        return res != null
	                && res.getOrderId() != null
	                && res.getPaypalStatus() != null
	                && "PAYER_ACTION_REQUIRED".equalsIgnoreCase(res.getPaypalStatus())
	                && res.getRedirectUrl() != null;
	    }

	    private boolean isValidCaptureOrder(OrderResponse res) {
	        return res != null
	                && res.getOrderId() != null
	                && "COMPLETED".equalsIgnoreCase(res.getPaypalStatus());
	    }
	
	

}
