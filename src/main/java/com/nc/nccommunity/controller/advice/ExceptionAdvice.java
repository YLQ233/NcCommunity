package com.nc.nccommunity.controller.advice;

import com.nc.nccommunity.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
	
	@ExceptionHandler({Exception.class})
	public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// log
		log.error("\n服务器发生异常  msg:\n	" + e.getMessage());
		for(StackTraceElement element : e.getStackTrace()){
			log.error(element.toString());
		}
		
		// handle exception
		String xRequestedWith = request.getHeader("x-requested-with");
		if ("XMLHttpRequest".equals(xRequestedWith)) {
			// 返回普通字符串，前端手动转json
			response.setContentType("application/plain;charset=utf-8");
			PrintWriter writer = response.getWriter();
			writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
		}else{
			response.sendRedirect(request.getContextPath() + "/error");
		}
		
	}

}
