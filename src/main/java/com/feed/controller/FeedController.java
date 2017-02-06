package com.feed.controller;

import com.feed.data.UserRepository;
import com.feed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class FeedController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/feed")
    public String feedHome(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession();

        User user = (User)session.getAttribute("sessionUser");

        user.setEnabled(true);
        userRepository.save(user);

        model.addAttribute("user", user);
        return "Feed";
    }
}
