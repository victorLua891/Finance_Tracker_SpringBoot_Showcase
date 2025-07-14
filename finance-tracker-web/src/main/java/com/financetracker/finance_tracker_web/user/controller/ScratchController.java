package com.financetracker.finance_tracker_web.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScratchController {
    @GetMapping("/scratch")
    public String Scratch(){
        return "user/Scratch";

    }
}
