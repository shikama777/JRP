package com.app.utils;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils {
	
	@Autowired
	MessageSource messageSource;
	
	public String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.JAPAN);
	}

}
