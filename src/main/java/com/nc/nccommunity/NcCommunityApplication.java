package com.nc.nccommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;

@SpringBootApplication
//@MapperScan("com.nc.nccommunity.dao")
public class NcCommunityApplication {
//	@PostConstruct//调用类构造器后，接着调用标注的方法，一般用于init方法
//	public void init() {
//		// 解决redis与ES都启动netty冲突问题
//		// see Netty4Utils.setAvailableProcessors()
//		System.setProperty("es.set.netty.runtime.available.processors", "false");
//	}
	public static void main(String[] args) {
		SpringApplication.run(NcCommunityApplication.class, args);
	}
	
}
