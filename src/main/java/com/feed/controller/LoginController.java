package com.feed.controller;

import com.feed.data.UserRepository;
import com.feed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/login")
    public String loginHome() {
        return "Login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String userId, String password, HttpSession session) {

        User user;
        try {
            user = userRepository.findByUserId(userId);

            if (!password.equals(user.getPassword())) {
                return "ErrorPage";
            }
            if (user.isEnabled()) {
                return "ErrorPage";
            }

            session.setAttribute("sessionUser", user);
            return "redirect:/feed";

        } catch (Exception e) {
            return "ErrorPage";
        }
    }

    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("sessionUser");

        user.setEnabled(false);
        userRepository.save(user);

        session.removeAttribute("sessionUser");

        return "redirect:/login";
    }


}
