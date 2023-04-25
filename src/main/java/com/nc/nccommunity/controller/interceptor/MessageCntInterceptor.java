package com.nc.nccommunity.controller.interceptor;

import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.MessageService;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageCntInterceptor implements HandlerInterceptor {
	@Autowired
	private MessageService messageService;
	@Autowired
	private HostHolder hostHolder;
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		if(user != null) {
			int letterUnread = messageService.findUnreadLetterCount(user.getId(), null);
			int noticeUnread = messageService.getUnreadMsgCnt(user.getId(), null);
			modelAndView.addObject("allUnreadCount", letterUnread + noticeUnread);
		}
	}
}
