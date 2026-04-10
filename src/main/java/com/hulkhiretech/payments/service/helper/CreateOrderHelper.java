package com.hulkhiretech.payments.service.helper;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.req.Amount;
import com.hulkhiretech.payments.paypal.req.ExperienceContext;
import com.hulkhiretech.payments.paypal.req.OrderRequest;
import com.hulkhiretech.payments.paypal.req.PaymentSource;
import com.hulkhiretech.payments.paypal.req.Paypal;
import com.hulkhiretech.payments.paypal.req.PurchaseUnit;
import com.hulkhiretech.payments.paypal.res.PaypalLink;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderHelper {

	private final JsonUtil jsonUtil;

	@Value("${paypal.create-order-url}")
	private String createOrderUrl;

	public HttpRequest prepareCreateOrderHttpRequest(
			CreateOrderReq createOrderReq, String accessToken) {
		HttpHeaders headers =new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// set header Paypal-Request-Id =>UUID
		String uniqueId =UUID.randomUUID().toString();
		log.info("Generated unique id for Paypal-Request-Id: {}", uniqueId);

		headers.add(Constant.PAYPAL_REQUEST_ID, uniqueId);

		// Create Amount object
		Amount amount = new Amount();
		amount.setCurrencyCode(createOrderReq.getCurrencyCode());

		//read the amount from createOrderReq and convert to 2 decimal places format string
		String amtStr = String.format(Constant.TWO_DECIMAL_FORMAT, createOrderReq.getAmount()); 
		amount.setValue(amtStr);

		// Create PurchaseUnit
		PurchaseUnit purchaseUnit = new PurchaseUnit();
		purchaseUnit.setAmount(amount);

		// Create ExperienceContext
		ExperienceContext context = new ExperienceContext();
		context.setPaymentMethodPreference(Constant.IMMEDIATE_PAYMENT_REQUIRED);
		context.setLandingPage(Constant.LANDINGPAGE_LOGIN);
		context.setShippingPreference(Constant.SHIPPING_PREF_NO_SHIPPING);
		context.setUserAction(Constant.USER_ACTION_PAY_NOW);
		context.setReturnUrl(createOrderReq.getReturnUrl());
		context.setCancelUrl(createOrderReq.getCancelUrl());

		// Create Paypal
		Paypal paypal = new Paypal();
		paypal.setExperienceContext(context);

		// Create PaymentSource
		PaymentSource paymentSource = new PaymentSource();
		paymentSource.setPaypal(paypal);

		// Create Main Request
		OrderRequest order = new OrderRequest();
		order.setIntent(Constant.INTENT_CAPTURE);
		order.setPurchaseUnits(List.of(purchaseUnit));
		order.setPaymentSource(paymentSource);

		log.info("Constructed OrderRequest: {}", order);

		// Convert to JSON String
		String requestAsJson=jsonUtil.toJson(order);

		//Create HttpRequest and set method, url, headers, body
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);

		httpRequest.setUrl(createOrderUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(requestAsJson);
		return httpRequest;
	}
	
	public OrderResponse toOrderResponse(PaypalOrder paypalOrder) {
		log.info("Converting PaypalOrder to OrderResponse :{}",paypalOrder);

		OrderResponse response = new OrderResponse();
		response.setOrderId(paypalOrder.getId());
		response.setPaypalStatus(paypalOrder.getStatus());

		String redirectLink = null;
		if (paypalOrder.getLinks() != null) {
		    redirectLink = paypalOrder.getLinks().stream()
		            .filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
		            .findFirst()
		            .map(PaypalLink::getHref)
		            .orElse(null);
		}
		response.setRedirectUrl(redirectLink);

		log.info("Mapped PaypalOrder to OrderResponse: {}", response);

		return response;
	}
	

}


