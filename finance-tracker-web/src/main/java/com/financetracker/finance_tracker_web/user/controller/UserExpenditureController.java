package com.financetracker.finance_tracker_web.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

//NOTE: user specific if wanted admin would be /admin/expenditure.
@Controller
public class UserExpenditureController {

    @GetMapping("/expenditure")
    public String expenditurePage(Model model){
        return "user/Expenditure";
    }
}
