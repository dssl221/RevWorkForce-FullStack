package com.revworkforce.revworkforce_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/leaves")
    public String leaves() {
        return "leaves";
    }

    @GetMapping("/goals")
    public String goals() {
        return "goals";
    }

    @GetMapping("/performance")
    public String performance() {
        return "performance";
    }

    @GetMapping("/employees")
    public String employees() {
        return "employees";
    }

    @GetMapping("/announcements")
    public String announcements() {
        return "announcements";
    }

    @GetMapping("/holidays")
    public String holidays() {
        return "holidays";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
}
