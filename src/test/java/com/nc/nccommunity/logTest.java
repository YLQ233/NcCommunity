package com.nc.nccommunity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
@Slf4j
class logTest {
	@Test
	void test(){
		System.out.println(log.getName());
		log.debug("debug");
		log.info("info");
		log.warn("warn");
		log.error("error");
	}
}
