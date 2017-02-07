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
    public String join(User user) {

        if(userRepository.exists(user.getUserId())) {
            // 아이디 중복
            return "redirect:/join";
        }
        else { // 아이디 중복 아님
            user.setEnabled(false);
            user.setFollower(0l);
            user.setFollowing(0l);
            user.setPosting(0l);
            userRepository.save(user);
            return "redirect:/";
        }
    }
}
