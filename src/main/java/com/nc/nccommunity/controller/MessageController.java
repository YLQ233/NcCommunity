package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.Message;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.MessageService;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import com.sun.istack.internal.NotNull;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/letter")
public class MessageController {
	@Autowired
	MessageService messageService;
	@Autowired
	HostHolder hostHolder;
	@Autowired
	UserService userService;
	
	@GetMapping("/list")
	public String getLetterList(Model model, Page page) {
		User user = hostHolder.getUser();
		//page
		page.setLimit(5);
		page.setPath("/letter/list");
		page.setRows(messageService.findConversationCount(user.getId()));
		
		List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
		List<Map<String,Object>> conversations = new ArrayList<Map<String,Object>>();
		if (conversationList != null){
			for(Message message : conversationList){
				String conversationId = message.getConversationId();
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("conversation", message);
				int unreadCount = messageService.findUnreadLetterCount(user.getId(), conversationId);
				map.put("unreadCount",unreadCount);
				int letterCount = messageService.findLetterCount(conversationId);
				map.put("letterCount", letterCount);
				int tagetId = message.getToId()==user.getId() ? message.getFromId() : message.getToId();
				User target = userService.getUserById(tagetId);
				map.put("target", target);
				
				conversations.add(map);
			}
		}
		model.addAttribute("conversations", conversations);
		
		int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);
		
		return "/site/letter";
	}
	
	@GetMapping("/detail/{conversationId}")
	public String getLetterDetail(@PathVariable("conversationId")String conversationId, Model model, Page page){
		page.setLimit(5);
		page.setPath("/letter/detail/" + conversationId);
		page.setRows(messageService.findLetterCount(conversationId));
		
		List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
		List<Map<String,Object>> letters = new ArrayList<Map<String,Object>>();
		if(letterList != null){
			for (Message message : letterList){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("letter", message);
				User fromUser = userService.getUserById(message.getFromId());
				map.put("fromUser", fromUser);
				
				letters.add(map);
			}
		}
		model.addAttribute("letters", letters);
		
		//target
		String[] userIds = conversationId.split("_");
		int userId1 = Integer.parseInt(userIds[1]);
		int userId0 = Integer.parseInt(userIds[0]);
		int targetId = hostHolder.getUser().getId()==userId0 ? userId1 : userId0;
		model.addAttribute("target", userService.getUserById(targetId));
		
		// 设置已读
		List<Integer> ids = getUnreadIds(letterList);
		if (!ids.isEmpty()) {
			messageService.readMessage(ids);
		}
		
		return "/site/letter-detail";
	}
	
	private List<Integer> getUnreadIds(List<Message> letterList) {
		List<Integer> ids = new ArrayList<>();
		if (letterList != null) {
			for (Message message : letterList) {
				if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
					ids.add(message.getId());
				}
			}
		}
		return ids;
	}
	
	@PostMapping("/send")
	@ResponseBody
	public String addMessage(String toName, String content) {
		User toUser = userService.getUserByName(toName);
		if(toUser==null){
			return CommunityUtil.getJSONString(1,"用户不存在");
		}
		Message message = new Message();
		int fromId = hostHolder.getUser().getId();
		message.setFromId(fromId);
		int toId = toUser.getId();
		message.setToId(toId);
		message.setContent(content);
		message.setCreateTime(new Date());
		message.setStatus(0);
		String cid = fromId>toId ? toId+"_"+fromId : fromId+"_"+toId;
		message.setConversationId(cid);
		messageService.addMessage(message);
		
		return CommunityUtil.getJSONString(0);
	}
	
	@PostMapping("/delete")
	@ResponseBody
	public String deleteMessage(int id){
		int rows = messageService.deleteMessage(id);
		if(rows == 0){
			return CommunityUtil.getJSONString(1,"删除失败");
		}
		return CommunityUtil.getJSONString(0);
	}
	
	
}
