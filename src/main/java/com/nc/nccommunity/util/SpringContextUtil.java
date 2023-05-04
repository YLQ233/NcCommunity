package com.nc.nccommunity.util;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
@Deprecated
@Configuration
public class SpringContextUtil implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		SpringContextUtil.applicationContext = applicationContext;
	}
	
	
	public static Object getBean(String name) throws BeansException{
		
		return applicationContext.getBean(name);
	}
}