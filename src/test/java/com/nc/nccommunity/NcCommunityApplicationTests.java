package com.nc.nccommunity;

import com.nc.nccommunity.dao.DiscussPostMapper;
import com.nc.nccommunity.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class NcCommunityApplicationTests {
	
	@Test
	void contextLoads() {
	}
	@Autowired
	DiscussPostMapper discussPostMapper;
	@Test
	void testDiscussPost(){
	
	}
	
}
