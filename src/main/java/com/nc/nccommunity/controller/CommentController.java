package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.Comment;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.CommentService;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private CommentService commentService;
	
	@PostMapping("/add/{discussPostId}")
	public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment) {
		User uesr = hostHolder.getUser();
		comment.setUserId(uesr.getId());
		comment.setCreateTime(new Date());
		comment.setStatus(0);
		commentService.addComment(comment);
		
		return "redirect:/discuss/detail/" + discussPostId;
	}
	
}
