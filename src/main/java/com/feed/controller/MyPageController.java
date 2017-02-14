package com.feed.controller;

import com.feed.data.FollowRepository;
import com.feed.data.PostRepository;
import com.feed.data.UserRepository;
import com.feed.model.Follow;
import com.feed.model.Post;
import com.feed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class MyPageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepository followRepository;

    @RequestMapping(value = "/{userId}")
    public String myPage(@PathVariable String userId, Model model, ModelMap modelMap, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("sessionUser");

        if(user.getUserId().equals(userId)) {
            model.addAttribute("user", userRepository.findByUserId(userId));

            List<Post> posts = postRepository.findAllByUserId(userId);

            List<Follow> follows = followRepository.findAllByFollower(user.getUserId());
            for(Follow following : follows) {
                posts.addAll(postRepository.findAllByUserId(following.getFollowing()));
            }

            Collections.sort(posts, new Comparator<Post>() {
                @Override
                public int compare(Post o1, Post o2) {
                    return (int)o2.getId() - (int)o1.getId();
                }
            });

            //modelMap.put("posts", posts);

            return "MyPage";
        }
        else {
            model.addAttribute("user", userRepository.findByUserId(userId));

            List<Post> posts = postRepository.findAllByUserId(userId);

            modelMap.put("posts", posts);

            return "PersonalPage";
        }
    }
}
