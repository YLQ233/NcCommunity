package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.Comment;
import com.nc.nccommunity.entity.Event;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.event.EventProducer;
import com.nc.nccommunity.service.CommentService;
import com.nc.nccommunity.service.DiscussPostService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private CommentService commentService;
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private EventProducer eventProducer;
	
	@PostMapping("/add/{discussPostId}")
	public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment) {
		User user = hostHolder.getUser();
		comment.setUserId(user.getId());
		comment.setCreateTime(new Date());
		comment.setStatus(0);
		commentService.addComment(comment);
		
		//系统通知
		Event event = new Event().setTopic(TOPIC_COMMENT)
				.setEntityId(discussPostId)
				.setEntityType(comment.getEntityType())
				.setUserId(user.getId())
				.setData("postId", discussPostId);
		if(comment.getEntityType() == ENTITY_TYPE_POST){
			int entityUserId = discussPostService.getDiscussPostById(comment.getEntityId()).getUserId();
			event.setEntityUserId(entityUserId);
		}else{
			int entityUserId = commentService.findCommentById(comment.getEntityId()).getUserId();
			event.setEntityUserId(entityUserId);
		}
		eventProducer.fireEvent(event);
		
		
		if (comment.getEntityType() == ENTITY_TYPE_POST) {
			// 触发发帖事件
			event = new Event()
					.setTopic(TOPIC_PUBLISH)
					.setUserId(comment.getUserId())
					.setEntityType(ENTITY_TYPE_POST)
					.setEntityId(discussPostId);
			eventProducer.fireEvent(event);
		}
		
		return "redirect:/discuss/detail/" + discussPostId;
	}
	
	
	
}
