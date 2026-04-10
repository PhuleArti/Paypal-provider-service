package com.hulkhiretech.payments.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {
	
	private final HttpServiceEngine httpServiceEngine;
	
	//TODO : implement Radis based and take care of expiry.
	private static String accessToken;
	
	@Value("${paypal.client.id}")
	private String clientId;
	
	@Value("${paypal.client.secret}")
	private String clientSecret;
	
	@Value("${paypal.oauth.url}")
	private String oauthUrl;
	
	private final JsonUtil jsonUtil;
	
	/*
	 * @return access token string
	 */

	public String getAccessToken() {
		log.info(" Retriving access token from TokenService ");
		
		if(accessToken != null) {
			log.info(" Returning cached access token ");
			return accessToken;
		}
		
		log.info(" No cached access token found, calling paypal OAuth service ");
		
		// TODO Call paypal OAuth service to get access token
		//update the accessToken variable with actual token from paypal.
		HttpHeaders headers =new HttpHeaders();
		
		
		headers.setBasicAuth(clientId, clientSecret);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		
		MultiValueMap<String,String> formData = new LinkedMultiValueMap<>();
		formData.add(Constant.GRANT_TYPE,Constant.CLIENT_CREDENTIALS);
		
		// fluent API ---------------------------------------------------
		
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(oauthUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(formData);
		
		log.info(" Prepared HttpRequest for httpServiceEngine : {}",httpRequest);
		
		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP response from httpServiceEngine : {}", httpResponse);
		
		String tokenBody= httpResponse.getBody();
		log.info("Access token retrived from paypal OAuth service : {}", tokenBody);
		
		PaypalOAuthToken token =jsonUtil.fromJson(tokenBody, PaypalOAuthToken.class);
		 accessToken = token.getAccessToken(); // Cache the access token for future use
		 log.info("Caching access token for future use");
		 
		return accessToken;
		
	}
}
