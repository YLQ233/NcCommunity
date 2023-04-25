package com.nc.nccommunity.controller;

import com.google.code.kaptcha.Producer;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class LoginController implements CommunityConstant {
	@Autowired
	private UserService userService;
	@Autowired
	private Producer kaptchaProducer;
	@Autowired
	private RedisTemplate redisTemplate;
	@Value("${server.servlet.context-path}")
	private String contextPath;
	
	@GetMapping("/register")
	public String toRegisterPage(){
		return "/site/register";
	}
	
	@PostMapping("/register")
	public String register(Model model, User user){
		Map<String, Object> map = userService.register(user);
		if(map==null || map.isEmpty()){
			model.addAttribute("msg","点击邮箱链接即可激活");
			model.addAttribute("target", "redirect:/index");
			return "/site/operate-result";
		} else {
			model.addAttribute("usernameMsg", map.get("usernameMsg"));
			model.addAttribute("passwordMsg", map.get("passwordMsg"));
			model.addAttribute("emailMsg", map.get("emailMsg"));
			return "/site/register";
		}
	}
	
	@GetMapping("/activation/{userId}/{code}")
	public String activation(Model model, @PathVariable("userId")int userId, @PathVariable("code")String code){
		int result = userService.activation(userId, code);
		if (result == ACTIVATION_SUCCESS) {
			model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
			model.addAttribute("target", "/login");
		} else if (result == ACTIVATION_REPEAT) {
			model.addAttribute("msg", "无效操作,该账号已经激活过了!");
			model.addAttribute("target", "/index");
		} else {
			model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
			model.addAttribute("target", "/index");
		}
		return "/site/operate-result";
	}
	
	@GetMapping("/login")
	public String toLoginPage(){
		return "/site/login";
	}
	
	
	
	@GetMapping("/kaptcha")
	public void getKaptcha(HttpServletResponse response) {
		// 生成验证码
		String text = kaptchaProducer.createText();
		BufferedImage image = kaptchaProducer.createImage(text);
		
		// 用cookie令验证码对应用户
		String owner = CommunityUtil.generateUUID();
		Cookie cookie = new Cookie("kaptchaOwner", owner);
		cookie.setMaxAge(60);
		cookie.setPath(contextPath);
		response.addCookie(cookie);
		
		// 将验证码存入redis
		String kaptchaKey = RedisUtil.getKaptchaKey(owner);
		redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);
		
		// 将图片输出给浏览器
		response.setContentType("image/png");
		try {
			OutputStream os = response.getOutputStream();
			ImageIO.write(image, "png", os);
		} catch (IOException e) {
			log.error("响应验证码失败:" + e.getMessage());
		}
	}
	
	@PostMapping("/login")
	public String login(String username, String password, String code, boolean rememberme, Model model,
						@CookieValue(value = "kaptchaOwner", required = false) String kaptchaOwner, HttpServletResponse response) {
		//get kaptcha
		String kaptcha = null;
		if (StringUtils.isNotBlank(kaptchaOwner)) {
			String redisKey = RedisUtil.getKaptchaKey(kaptchaOwner);
			kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
		}
		
		if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
			model.addAttribute("codeMsg", "验证码不正确!");
			return "/site/login";
		}
		
		//un+pw
		int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
		Map<String,Object> map = userService.login(username,password,expiredSeconds);
		if(map.containsKey("ticket")){
			Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
			cookie.setPath(contextPath);
			cookie.setMaxAge(expiredSeconds);
			response.addCookie(cookie);
			return "redirect:/index";
		}else{
			model.addAttribute("usernameMsg", map.get("usernameMsg"));
			model.addAttribute("passwordMsg", map.get("passwordMsg"));
			return "/site/login";
		}
	}
	
	@GetMapping("/logout")
	public String logout(@CookieValue("ticket") String ticket){
		userService.logout(ticket);
		return "redirect:/login";
	}
	
	@GetMapping("/forget")
	public String toForgetPage(){
		return "/site/forget";
	}
	
	@PostMapping("/forget/password")
	public String forgetPassword(String email, String verifyCode, String password, Model model, HttpSession session){
		String correctCode = (String) session.getAttribute(email+"_verifyCode");
		if(StringUtils.isBlank(verifyCode) || StringUtils.isBlank(correctCode) || !correctCode.equals(verifyCode)){
			model.addAttribute("codeMsg", "验证码错误!");
			return "/site/forget";
		}
		Map<String,Object> map = userService.resetPassword(email, password);
		if(map.containsKey("user")) {
			model.addAttribute("msg","密码修改成功，请重新登录");
			model.addAttribute("target","/logout");
			return "/site/operate-result";
		}else{
			model.addAttribute("passwordMsg", model.getAttribute("passwordMsg"));
			model.addAttribute("emailMsg", model.getAttribute("emailMsg"));
			return "/site/forget";
		}
	}
	
	@GetMapping("/forget/code")
	@ResponseBody
	public String getForgetCode(String email, HttpSession session){
		//null
		if (StringUtils.isBlank(email)) {
			return CommunityUtil.getJSONString(1, "邮箱不能为空！");
		}
		//verify email
		if(!userService.isEmailExist(email)) {
			return CommunityUtil.getJSONString(1, "邮箱未注册");
		}
		//save code
		session.setAttribute(email+"_verifyCode", userService.getForgetCode(email));
		
		return CommunityUtil.getJSONString(0);
	}
	
}
