package com.nc.nccommunity.controller;

import com.nc.nccommunity.annotation.LoginRequired;
import com.nc.nccommunity.entity.Comment;
import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.*;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sun.activation.registries.LogSupport.log;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private UserService userService;
	@Autowired
	private LikeService likeService;
	@Autowired
	private FollowService followService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private DiscussPostService discussPostService;
	
	@Value("${community.path.domain}")
	private String domain;
	@Value("${server.servlet.context-path}")
	private String contextPath;
	@Value("${community.path.upload}")
	private String uploadPath;
	
//	@LoginRequired
	@GetMapping("/setting")
	public String toSettingPage(){
		return "/site/setting";
	}
	
//	@LoginRequired
	@PostMapping("/upload")
	public String uploadHeader(@RequestPart("headerImage")MultipartFile headerImg, Model model) {
		//空图处理
		if(headerImg == null) {
			model.addAttribute("error", "Missing header image");
			return "/site/setting";
		}
		String fileName = headerImg.getOriginalFilename();
		//判断文件有无后缀
		String suffix = fileName.substring(fileName.lastIndexOf('.'));
		if(StringUtils.isBlank(suffix)){
			model.addAttribute("error", "Unmatched format");
			return "/site/setting";
		}
		fileName = CommunityUtil.generateUUID() + suffix;//防止文件重名
		//存放到指定位置
		File file = new File(uploadPath + "/" + fileName);
		//若没有目录，则创建
		if(!file.exists())
			file.mkdir();
		try {
			headerImg.transferTo(file);
		} catch (IOException e) {
			log("Failed to upload file" + e.getMessage());
			throw new RuntimeException("Failed to upload file. Server Error.",e);
		}
		//修改用户头像
		int id = hostHolder.getUser().getId();
		String headUrl = domain + contextPath + "/user/header/" + fileName;
		userService.updateHeader(id, headUrl);
		
		return "redirect:/index";
	}
	
	@GetMapping("/header/{fileName}")
	public void getHeader(@PathVariable("fileName") String filename, HttpServletResponse response){
		filename  = uploadPath + "/" + filename;//带路径的文件名
		String suffix = filename.substring(filename.lastIndexOf('.') +1);
		response.setContentType("image/" + suffix);//设置格式 响应图片
		try (	FileInputStream fis = new FileInputStream(filename);
				OutputStream os = response.getOutputStream();	) {
			byte[] buffer = new byte[1024];
			int b = 0;
			while ((b = fis.read(buffer)) != -1) {
				os.write(buffer, 0, b);
			}
		} catch (IOException e) {
			log.error("读取头像失败: " + e.getMessage());
		}
	}
	
//	@LoginRequired
	@PostMapping("/updatePassword")
	public String updatePassword(String oldpw, String newpw, Model model){
		User user = hostHolder.getUser();
		Map<String, Object> map = userService.updatePassword(user, oldpw, newpw);
		if(map.isEmpty()){
			model.addAttribute("msg","密码修改成功，请重新登录");
			model.addAttribute("target","/logout");
			return "/site/operate-result";
		}else{
			model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
			model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
			return "/site/setting";
		}
	}
	
	@GetMapping("/profile/{userId}")
	public String getProfilePage(@PathVariable("userId")int userId, Model model){
		User user = userService.getUserById(userId);
		if(user == null)	throw new RuntimeException("该用户不存在!");
		model.addAttribute("user",user);
		
		int likeCount = likeService.countLikeUser(userId);
		model.addAttribute("likeCount",likeCount);
		
		long followeeCount = followService.getFolloweeCount(userId, ENTITY_TYPE_USER);
		long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER, userId);
		model.addAttribute("followeeCount",followeeCount);
		model.addAttribute("followerCount",followerCount);
		
		boolean hasFollowed = false;
		if (hostHolder.getUser() != null) {
			hasFollowed = followService.isFollower(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
		}
		model.addAttribute("hasFollowed", hasFollowed);
		
		return "/site/profile";
	}
	
	// 我(TA)的帖子
	@RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
	public String getMyPost(@PathVariable("userId") int userId, Page page, Model model) {
		User user = userService.getUserById(userId);
		if(user == null)	throw new RuntimeException("用户不存在！");
		model.addAttribute("user", user);
		
		// 分页信息
		page.setPath("/user/mypost/" + userId);
		page.setRows(discussPostService.getDiscussPostRows(userId));
		
		// 帖子列表
		List<DiscussPost> list = discussPostService.getDiscussPostList(userId, page.getOffset(), page.getLimit(), 0);
		List<Map<String,Object>> discussVOList = new ArrayList<Map<String,Object>>();
		if(list != null)
		for(DiscussPost post : list) {
			Map<String,Object> map = new HashMap();
			map.put("discussPost", post);
			map.put("likeCount", likeService.countLikeEntity(ENTITY_TYPE_POST, post.getId()));
			discussVOList.add(map);
		}
		model.addAttribute("discussPosts", discussVOList);
		
		return "site/my-post";
	}
	
	// 我(TA)的回复
	@RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
	public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
		User user = userService.getUserById(userId);
		if(user == null)	throw new RuntimeException("用户不存在！");
		model.addAttribute("user", user);
		
		// 分页信息
		page.setPath("/user/myreply/" + userId);
		page.setRows(commentService.findUserCount(userId));
		
		// 回复列表
		List<Comment> commentList = commentService.findUserComments(userId, page.getOffset(), page.getLimit());
		List<Map<String, Object>> commentVOList = new ArrayList<>();
		if (commentList != null) {
			for (Comment comment : commentList) {
				Map<String, Object> map = new HashMap<>();
				map.put("comment", comment);
				DiscussPost post = discussPostService.getDiscussPostById(comment.getEntityId());
				map.put("discussPost", post);
				commentVOList.add(map);
			}
		}
		model.addAttribute("comments", commentVOList);
		
		
		return "site/my-reply";
	}
}
