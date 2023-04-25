package com.nc.nccommunity.event;

import com.alibaba.fastjson.JSONObject;
import com.nc.nccommunity.entity.Event;
import com.nc.nccommunity.entity.Message;
import com.nc.nccommunity.service.MessageService;
import com.nc.nccommunity.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EventConsumer implements CommunityConstant{
	@Autowired
	MessageService messageService;
	
	@KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
	public void receive(ConsumerRecord record){
		if(record == null || record.value() == null){
			log.error("空消息!!!");
			return;
		}
		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		
		Message message = new Message();
		
		message.setFromId(SYSTEM_USER_ID);
		message.setToId(event.getEntityUserId());
		message.setConversationId(event.getTopic());
		message.setCreateTime(new Date());
		
		Map<String, Object> content = new HashMap<>();
		content.put("userId", event.getUserId());
		content.put("entityType", event.getEntityType());
		content.put("entityId", event.getEntityId());
		if(!event.getData().isEmpty()){
			for(Map.Entry<String, Object> entry : event.getData().entrySet()){
				content.put(entry.getKey(), entry.getValue());
			}
		}
		message.setContent(JSONObject.toJSONString(content));
		
		messageService.addMessage(message);
	}
	
	
}
