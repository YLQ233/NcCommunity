package com.nc.nccommunity.dao;

import com.nc.nccommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiscussPostMapper {
	List<DiscussPost> getDiscussPostList(int userId, int offset, int limit);
	//帖子总数(如果只有一个参数,并且在<if>里使用,则必须加别名.)
	int getDiscussPostRows(@Param("userId") int userId);
}
