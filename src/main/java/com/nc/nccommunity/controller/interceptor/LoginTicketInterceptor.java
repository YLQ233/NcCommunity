package com.nc.nccommunity.controller.interceptor;

import com.nc.nccommunity.entity.LoginTicket;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CookieUtil;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
	@Autowired
	HostHolder hostHolder;
	@Autowired
	UserService userService;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		String ticket = CookieUtil.getCookieValue(request, "ticket");
		if(ticket != null){
			//cookie中获取凭证
			LoginTicket loginTicket = userService.getLoginTicketByTicket(ticket);
			//验证凭证有效
			if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
				User user = userService.getUserById(loginTicket.getUserId());
				//保存User到本次请求的线程
				hostHolder.setUser(user);
				
				// 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
				Authentication authentication = new UsernamePasswordAuthenticationToken(
						user, user.getPassword(), userService.getAuthorities(user.getId()));
				SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
			}
		}
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		if(user!=null && modelAndView!=null)
			modelAndView.addObject("loginUser",user);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		hostHolder.clear();
	}
}
