package com.nc.nccommunity.controller.interceptor;

import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.DataService;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
	
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private DataService dataService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 统计UV
		String ip = request.getRemoteHost();
		dataService.recordUV(ip);
		
		// 统计DAU
		User user = hostHolder.getUser();
		if (user != null) {
			dataService.recordDAU(user.getId());
		}
		
		return true;
	}
}
