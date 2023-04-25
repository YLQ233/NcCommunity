package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.Event;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.event.EventProducer;
import com.nc.nccommunity.service.LikeService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
	@Autowired
	private LikeService likeService;
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private EventProducer eventProducer;
	
	@PostMapping("/like")
	@ResponseBody
	public String like(int entityType, int entityId, int entityUserId, int postId){
		User user = hostHolder.getUser();
		likeService.like(user.getId(), entityType, entityId, entityUserId);
		long count = likeService.countLikeEntity(entityType, entityId);
		int status = likeService.ifLiked(user.getId(), entityType, entityId)?1:0;
		
		//系统通知
		if(status == 1){
			Event event = new Event().setUserId(user.getId())
					.setTopic(TOPIC_LIKE)
					.setEntityType(entityType)
					.setEntityId(entityId)
					.setEntityUserId(entityUserId)
					.setData("postId", postId);
			
			
			eventProducer.fireEvent(event);
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("likeCount", count);
		map.put("likeStatus", status);
		
		return CommunityUtil.getJSONString(0, null, map);
	}
	
	
}
