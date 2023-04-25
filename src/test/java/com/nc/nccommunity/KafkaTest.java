package com.nc.nccommunity;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
@Slf4j
public class KafkaTest {
	@Autowired
	private Producer producer;
	
	@Test
	void test() throws InterruptedException {
		producer.send("test", "hey,");
		producer.send("test", " bro");
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}

@Component
class Producer{
	@Autowired
	private KafkaTemplate template;
	public void send(String topic, String message){
		template.send(topic,message);
	}
}

@Component
class Consumer{
	@KafkaListener(topics={"test"})
	public void receive(ConsumerRecord record){
		System.out.println(record.value());
	}
	
}