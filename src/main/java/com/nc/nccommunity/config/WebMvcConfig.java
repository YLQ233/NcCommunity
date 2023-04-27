package com.nc.nccommunity.config;

import com.nc.nccommunity.controller.interceptor.LoginRequiredInterceptor;
import com.nc.nccommunity.controller.interceptor.LoginTicketInterceptor;
import com.nc.nccommunity.controller.interceptor.MessageCntInterceptor;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Autowired
	private LoginTicketInterceptor loginTicketInterceptor;
//	@Autowired
//	private LoginRequiredInterceptor loginRequiredInterceptor;
	@Autowired
	private MessageCntInterceptor messageCntInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginTicketInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
		
//		registry.addInterceptor(loginRequiredInterceptor)
//				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
		
		registry.addInterceptor(messageCntInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
		
	}
}
