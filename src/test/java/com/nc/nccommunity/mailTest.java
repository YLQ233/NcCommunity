package com.nc.nccommunity;

import com.nc.nccommunity.util.MailClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class mailTest {
	@Autowired
	private MailClient mailClient;
	@Autowired
	private TemplateEngine templateEngine;
	
	@Test
	public void testTXTMail(){
		mailClient.sendMail("2643635287@qq.com","TestSender","Sender is OK!");
	}
	@Test
	public void testHTMLMail(){
		Context context = new Context();
		context.setVariable("userName","ylq");
		String content = templateEngine.process("/mail/demo", context);
		System.out.println(content);
		mailClient.sendMail("2643635287@qq.com","TestSenderHTML",content);
	}
	
}
