package com.nc.nccommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.nc.nccommunity.dao")
public class NcCommunityApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(NcCommunityApplication.class, args);
	}
	
}
