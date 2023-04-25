package com.nc.nccommunity.aspect;

import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;


@Deprecated
@Component
@Aspect
@Slf4j
public class ClearCacheAspect {
	@Autowired
	private UserService userService;
	@Pointcut("execution(* com.nc.nccommunity.dao.UserMapper.update*(..))")
	public void pointcutClear(){}
	
	@After("pointcutClear()")
	public void after(JoinPoint joinPoint) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//		Class cls = Class.forName("com.nc.nccommunity.service.UserService");
//		Method m = cls.getDeclaredMethod("clearCache",int.class);
//		m.setAccessible(true);
		
		Object[] args = joinPoint.getArgs();
		int p = (int) args[0];
//		Object o = SpringContextUtil.getBean("userService");
//		m.invoke(o, p);
		userService.clearCache(p);
	}
	
	
}
