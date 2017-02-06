package com.feed.controller;

import com.feed.data.UserRepository;
import com.feed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class JoinController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/join")
    public String joinHome() {
        return "Join";
    }

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public String join(String userId, String password) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(password);
        user.setEnabled(false);

        userRepository.save(user);
        return "redirect:/";
    }
}
