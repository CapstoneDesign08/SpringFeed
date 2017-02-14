package com.feed.controller;

import com.feed.data.PostRepository;
import com.feed.data.UserRepository;
import com.feed.model.Post;
import com.feed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class FeedController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @RequestMapping(value = "/feed")
    public String feedHome(Model model, ModelMap modelMap, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("sessionUser");

        user.setEnabled(true);
        userRepository.save(user);

        model.addAttribute("user", user);

        List<Post> posts = postRepository.findAllByOrderByIdDesc();

        modelMap.put("posts", posts);

        return "Feed";
    }

    @RequestMapping(value = "/feed", method = RequestMethod.POST)
    public String posting(Post post, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("sessionUser");

        post.setUserId(user.getUserId());
        //postRepository.save(post);

        long posting = user.getPosting();
        posting++;
        user.setPosting(posting);
        //userRepository.save(user);

        return "redirect:/feed";
    }
}
