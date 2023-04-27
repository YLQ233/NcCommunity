package com.nc.nccommunity.controller;

import com.alibaba.fastjson.JSONObject;
import com.nc.nccommunity.entity.Message;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.MessageService;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
	@Autowired
	private MessageService messageService;
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private UserService userService;
	
	@GetMapping("/letter/list")
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
		int noticeUnreadCount = messageService.getUnreadMsgCnt(user.getId(), null);
		model.addAttribute("noticeUnreadCount", noticeUnreadCount);
		
		return "/site/letter";
	}
	
	@GetMapping("/letter/detail/{conversationId}")
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
	
	@PostMapping("/letter/send")
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
	
	@PostMapping("/letter/delete")
	@ResponseBody
	public String deleteMessage(int id){
		int rows = messageService.deleteMessage(id);
		if(rows == 0){
			return CommunityUtil.getJSONString(1,"删除失败");
		}
		return CommunityUtil.getJSONString(0);
	}
	
	@GetMapping("/notice/list")
	public String getNoticeList(Model model){
		User user = hostHolder.getUser();
//AllUnreadMsg
		int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);
		int noticeUnreadCount = messageService.getUnreadMsgCnt(user.getId(), null);
		model.addAttribute("noticeUnreadCount", noticeUnreadCount);
		
//comment_notice
		Message latestMsg = messageService.getLatestMsg(user.getId(), TOPIC_COMMENT);
		Map<String,Object> msgVO = new HashMap<>();
		
		if(latestMsg != null){
			msgVO.put("message", latestMsg);
			
			String content = HtmlUtils.htmlUnescape(latestMsg.getContent());
			Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
			msgVO.put("user", userService.getUserById((Integer) data.get("userId")));
			msgVO.put("entityType", data.get("entityType"));
			msgVO.put("entityId", data.get("entityId"));
			msgVO.put("postId", data.get("postId"));
			
			int unreadCnt = messageService.getUnreadMsgCnt(user.getId(), TOPIC_COMMENT);
			msgVO.put("unread", unreadCnt);
			
			int cnt = messageService.getGroupCnt(user.getId(), TOPIC_COMMENT);
			msgVO.put("count", cnt);
		}else{
			msgVO.put("message", null);
		}
		model.addAttribute("commentNotice", msgVO);
//like_notice
		latestMsg = messageService.getLatestMsg(user.getId(), TOPIC_LIKE);
		msgVO = new HashMap<>();
		
		if(latestMsg != null){
			msgVO.put("message", latestMsg);
			
			String content = HtmlUtils.htmlUnescape(latestMsg.getContent());
			Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
			msgVO.put("user", userService.getUserById((Integer) data.get("userId")));
			msgVO.put("entityType", data.get("entityType"));
			msgVO.put("entityId", data.get("entityId"));
			msgVO.put("postId", data.get("postId"));
			
			int unreadCnt = messageService.getUnreadMsgCnt(user.getId(), TOPIC_LIKE);
			msgVO.put("unread", unreadCnt);
			
			int cnt = messageService.getGroupCnt(user.getId(), TOPIC_LIKE);
			msgVO.put("count", cnt);
		}else{
			msgVO.put("message", null);
		}
		model.addAttribute("likeNotice", msgVO);

//follow_notice
		latestMsg = messageService.getLatestMsg(user.getId(), TOPIC_FOLLOW);
		msgVO = new HashMap<>();
		
		if(latestMsg != null){
			msgVO.put("message", latestMsg);
			
			String content = HtmlUtils.htmlUnescape(latestMsg.getContent());
			Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
			msgVO.put("user", userService.getUserById((Integer) data.get("userId")));
			msgVO.put("entityType", data.get("entityType"));
			msgVO.put("entityId", data.get("entityId"));
			msgVO.put("postId", data.get("postId"));
			
			int unreadCnt = messageService.getUnreadMsgCnt(user.getId(), TOPIC_FOLLOW);
			msgVO.put("unread", unreadCnt);
			
			int cnt = messageService.getGroupCnt(user.getId(), TOPIC_FOLLOW);
			msgVO.put("count", cnt);
		}else{
			msgVO.put("message", null);
		}
		model.addAttribute("followNotice", msgVO);
		
		return "/site/notice";
	}
	
	@GetMapping ("/notice/detail/{topic}")
	public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
		User user = hostHolder.getUser();
		
		page.setLimit(5);
		page.setPath("/notice/detail/" + topic);
		page.setRows(messageService.getGroupCnt(user.getId(), topic));
		
		List<Message> noticeList = messageService.getMsgList(user.getId(), topic, page.getOffset(), page.getLimit());
		List<Map<String, Object>> noticeVoList = new ArrayList<>();
		if (noticeList != null) {
			for (Message notice : noticeList) {
				Map<String, Object> map = new HashMap<>();
				// 通知
				map.put("notice", notice);
				// 内容
				String content = HtmlUtils.htmlUnescape(notice.getContent());
				Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
				map.put("user", userService.getUserById((Integer) data.get("userId")));
				map.put("entityType", data.get("entityType"));
				map.put("entityId", data.get("entityId"));
				map.put("postId", data.get("postId"));
				// 通知作者
				map.put("fromUser", userService.getUserById(notice.getFromId()));
				
				noticeVoList.add(map);
			}
		}
		model.addAttribute("notices", noticeVoList);
		
		// 设置已读
		List<Integer> ids = getUnreadIds(noticeList);
		if (!ids.isEmpty()) {
			messageService.readMessage(ids);
		}
		
		return "/site/notice-detail";
	}
}
