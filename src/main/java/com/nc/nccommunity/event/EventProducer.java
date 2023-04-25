package com.nc.nccommunity.event;

import com.alibaba.fastjson.JSONObject;
import com.nc.nccommunity.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
	@Autowired
	private KafkaTemplate template;
	
	public void fireEvent(Event event){
		template.send(event.getTopic(), JSONObject.toJSONString(event));
	}
}
