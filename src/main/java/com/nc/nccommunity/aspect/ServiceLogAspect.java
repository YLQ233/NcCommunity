package com.nc.nccommunity.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
@Slf4j
public class ServiceLogAspect {
	@Pointcut("execution(* com.nc.nccommunity.service.*.*(..))")
	public void pointcutLog(){}
	
	
	@Before("pointcutLog()")
	public void before(JoinPoint joinPoint) {
		// 用户IP[x.x.x.x],在[time],访问了[com.nowcoder.community.service.xxx()].
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String ip;
		if(attributes != null){
			HttpServletRequest request = attributes.getRequest();
			ip = request.getRemoteHost();
		}else{
			ip = "通过EventConsumer";
		}
		
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
		log.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
	}
	
}
