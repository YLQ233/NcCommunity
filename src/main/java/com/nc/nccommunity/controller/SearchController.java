package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.service.ElasticsearchService;
import com.nc.nccommunity.service.LikeService;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        Map<String, Object> map1 = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
    
        // 搜索帖子
        List<DiscussPost> searchResult = (List) map1.get("list");
        int sum =  Integer.parseInt(map1.get("sum").toString());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.getUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.countLikeEntity(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : sum);

        return "/site/search";
    }

}
