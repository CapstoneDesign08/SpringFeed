package com.feed.controller;

import com.feed.data.FollowRepository;
import com.feed.data.UserRepository;
import com.feed.model.Follow;
import com.feed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class FollowController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @RequestMapping(value = "/follow/{userId}")
    public String follow(@PathVariable String userId, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("sessionUser");

        List<Follow> followList = followRepository.findAllByFollower(user.getUserId());

        boolean isFollow = false;
        long id = 0;

        for (Follow follow : followList) {
            if(follow.getFollowing().equals(userId)) {
                id = follow.getId();
                isFollow = true;
                break;
            }
        }

        if(isFollow) {
            followRepository.delete(id);

            long followCnt = user.getFollowing();
            followCnt--;
            user.setFollowing(followCnt);
            //userRepository.save(user);

            User fUser = userRepository.findByUserId(userId);
            followCnt = fUser.getFollower();
            followCnt--;
            fUser.setFollower(followCnt);
            //userRepository.save(fUser);
        }
        else {
            Follow follow = new Follow();
            follow.setFollower(user.getUserId());
            follow.setFollowing(userId);
            //followRepository.save(follow);

            long followCnt = user.getFollowing();
            followCnt++;
            user.setFollowing(followCnt);
            //userRepository.save(user);

            User fUser = userRepository.findByUserId(userId);
            followCnt = fUser.getFollower();
            followCnt++;
            fUser.setFollower(followCnt);
            //userRepository.save(fUser);
        }

        return "redirect:/" + userId;
    }
}