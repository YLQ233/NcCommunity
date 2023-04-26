package com.nc.nccommunity.dao;

import com.nc.nccommunity.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {
	@Insert({"insert into login_ticket(user_id,ticket,status,expired) values (#{userId},#{ticket},#{status},#{expired})"	})
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertLoginTicket(LoginTicket loginTicket);
	
	@Update({"update login_ticket set status=#{status} where ticket=#{ticket}"})
	int updateStatus(String ticket, int status);
	
	@Select({"select id,user_id,ticket,status,expired from login_ticket where ticket=#{ticket}" })
	LoginTicket selectByTicket(String ticket);
	
}
