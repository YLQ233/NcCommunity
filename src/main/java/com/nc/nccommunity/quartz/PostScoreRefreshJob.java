package com.nc.nccommunity.quartz;

import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.service.DiscussPostService;
import com.nc.nccommunity.service.ElasticsearchService;
import com.nc.nccommunity.service.LikeService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class PostScoreRefreshJob implements Job, CommunityConstant {
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private LikeService likeService;
	@Autowired
	private ElasticsearchService elasticsearchService;
	
	// 牛客纪元
	private static final Date epoch;
	static {
		try {
			epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
		} catch (ParseException e) {
			throw new RuntimeException("初始化起始年份失败!", e);
		}
	}
	
	/**
	 * 定义任务的接口Job的execute方法
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		BoundSetOperations op = redisTemplate.boundSetOps(RedisUtil.getPostScoreKey());
		if(op.size() == 0) {
			log.info("无帖子需刷新分数，任务取消！");
		}
		
		log.info("[任务开始] 正在刷新帖子分数: " + op.size());
		while (op.size() > 0){
			this.refresh((Integer) op.pop());
		}
		log.info("[任务结束] 帖子分数刷新完毕!");
	}
	
	
	
	private void refresh(int postId) {
		DiscussPost post = discussPostService.getDiscussPostById(postId);
		
		if (post == null) {
			log.error("该帖子不存在: id = " + postId);
			return;
		}
		
		// 是否精华
		boolean wonderful = post.getStatus() == 1;
		// 评论数量
		int commentCount = post.getCommentCount();
		// 点赞数量
		long likeCount = likeService.countLikeEntity(ENTITY_TYPE_POST, postId);
		
		// 计算权重
		double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
		// 分数 = 帖子权重 + 距离天数
		double score = Math.log10(Math.max(w, 1))
				+ (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
		// 更新帖子分数
		discussPostService.updateScore(postId, score);
		// 同步搜索数据
		post.setScore(score);
		elasticsearchService.saveDiscussPost(post);
	}
	
}
