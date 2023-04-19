package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.Comment;
import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.CommentService;
import com.nc.nccommunity.service.DiscussPostService;
import com.nc.nccommunity.service.LikeService;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private UserService userService;
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private CommentService commentService;
	@Autowired
	private LikeService likeService;
	
	@PostMapping("/add")
	@ResponseBody
	public String addDiscussPost(String title, String content){
		User user = hostHolder.getUser();
		if(user == null){
			return CommunityUtil.getJSONString(403,"需要登录");
		}
		DiscussPost discussPost = new DiscussPost();
		discussPost.setUserId(user.getId());
		discussPost.setTitle(title);
		discussPost.setContent(content);
		discussPost.setCreateTime(new Date());
		// type status score默认0
		
		discussPostService.addDiscussPost(discussPost);
		
		return CommunityUtil.getJSONString(0,"发布成功");
	}
	
	@GetMapping("/detail/{discussPostId}")
	public String findDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page){
		// post
		DiscussPost post = discussPostService.findDiscussPost(id);
		User user = userService.getUserById(post.getUserId());
		model.addAttribute("post", post);
		model.addAttribute("user", user);
		
		//like
		long likeCount = likeService.countLikeEntity(ENTITY_TYPE_POST, id);
		model.addAttribute("likeCount", likeCount);
		
		int likeStatus = hostHolder.getUser()!=null ? likeService.ifLiked(hostHolder.getUser().getId(), ENTITY_TYPE_POST, id)?1:0 : 0;
		model.addAttribute("likeStatus", likeStatus);
		
		// comment
		// 评论分页信息
		page.setLimit(5);
		page.setPath("/discuss/detail/" + id);
		page.setRows(post.getCommentCount());
		
		// 评论: 给帖子的评论
		// 回复: 给评论的评论
		//commentVo: author,comment,replies
		List<Map<String,Object>> commentVoList = new ArrayList<Map<String,Object>>();
		List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
		if(commentList != null){
			for(Comment comment: commentList){
				Map<String,Object> commentVo = new HashMap<>();
				commentVo.put("user", userService.getUserById(comment.getUserId()));
				commentVo.put("comment", comment);
				
				//like
				likeCount = likeService.countLikeEntity(ENTITY_TYPE_COMMENT, comment.getId());
				commentVo.put("likeCount", likeCount);
				
				likeStatus = hostHolder.getUser()!=null ? likeService.ifLiked(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId())?1:0 : 0;
				commentVo.put("likeStatus", likeStatus);
				
				//repliesVo: author,reply,target
				List<Map<String,Object>> repliesVoList = new ArrayList<>();
				List<Comment> repliesList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
				if(repliesList != null) {
					for (Comment reply : repliesList) {
						Map<String, Object> replyVo = new HashMap<>();
						replyVo.put("user", userService.getUserById(reply.getUserId()));
						replyVo.put("reply", reply);
						User target = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
						replyVo.put("target", target);
						//like
						likeCount = likeService.countLikeEntity(ENTITY_TYPE_COMMENT, reply.getId());
						replyVo.put("likeCount", likeCount);
						likeStatus = hostHolder.getUser()!=null ? likeService.ifLiked(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId())?1:0 : 0;
						replyVo.put("likeStatus", likeStatus);
						
						repliesVoList.add(replyVo);
					}
				}
				commentVo.put("replys", repliesVoList);
				
				//replies-count
				int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
				commentVo.put("replyCount", replyCount);
				
				commentVoList.add(commentVo);
			}
		}
		model.addAttribute("comments", commentVoList);
		
		return "/site/discuss-detail";
	}
	
	
}
