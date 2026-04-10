
package com.hulkhiretech.payments.service.helper;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptureOrderHelper {

	private static final String ORDER_ID_REF = "{orderId}";

	@Value("${paypal.capture.order.url}")
	private String captureOrderUrlTemplate;

	public HttpRequest prepareCaptureOrderHttpRequest(
			String orderId, String accessToken) {
		log.info("Preparing capture order HttpRequest "
				+ "|| orderId: {}, accessToken: {}",
				orderId, accessToken);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// set header PayPal-Request-Id => UUID
		String uuid = UUID.randomUUID().toString();
		log.info("Generated UUID for PayPal-Request-Id: {}", uuid);

		headers.add(Constant.PAYPAL_REQUEST_ID, uuid);

		//String requestAsJson = "";
		
		String captureOrderUrl = captureOrderUrlTemplate.replace(ORDER_ID_REF, orderId);
		log.info("Prepared capture order URL: {}", captureOrderUrl);
		
		// create HttpRequest
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(captureOrderUrl);
		httpRequest.setHttpHeaders(headers);
	//	httpRequest.setBody(requestAsJson);
		httpRequest.setBody(null);
		
		log.info("Prepared HttpRequest for capture order: {}", httpRequest);
		return httpRequest;
	}
	
	public OrderResponse toOrderResponse(PaypalOrder paypalOrder) {
		log.info("Converting PaypalOrder to OrderResponse: {}", paypalOrder);
		
	    OrderResponse response = new OrderResponse();
	    if (paypalOrder != null) {
	        response.setOrderId(paypalOrder.getId());
	        response.setPaypalStatus(paypalOrder.getStatus());
	    }
	    	    
	    log.info("Converted PaypalOrder to OrderResponse: {}", response);
	    return response;
	}
	

}
