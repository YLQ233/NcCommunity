package com.nc.nccommunity;

import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("Test Caffeine");
            post.setContent("Test Local Cache");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            postService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache() {
        System.out.println(postService.getDiscussPostList(0, 0, 10, 1));
        System.out.println(postService.getDiscussPostList(0, 0, 10, 1));
        System.out.println(postService.getDiscussPostList(0, 0, 10, 1));
        System.out.println(postService.getDiscussPostList(0, 0, 10, 0));
    }

}
