package com.feed.controller;

import com.feed.data.PostRepository;
import com.feed.data.UserRepository;
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
import java.util.List;

@Controller
public class MyPageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @RequestMapping(value = "/{userId}")
    public String myPage(@PathVariable String userId, Model model, ModelMap modelMap, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("sessionUser");

        if(user.getUserId().equals(userId)) {
            model.addAttribute("user", userRepository.findByUserId(userId));

            List<Post> posts = postRepository.findAllByUserId(userId);

            modelMap.put("posts", posts);

            return "MyPage";
        }
        else {
            model.addAttribute("user", userRepository.findByUserId(userId));

            List<Post> posts = postRepository.findAllByUserId(userId);

            modelMap.put("posts", posts);

            return "PersonalPage";
        }
    }

    /*@RequestMapping(value = "/{userId}", method = RequestMethod.POST)
    public String posting(@PathVariable String userId, Post post) {

        post.setUserId(userId);
        postRepository.save(post);

        User user = userRepository.findByUserId(userId);
        long posting = user.getPosting();
        posting++;
        user.setPosting(posting);
        userRepository.save(user);

        return "redirect:/" + userId;
    }*/
}
