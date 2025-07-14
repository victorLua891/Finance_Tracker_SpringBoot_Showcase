package com.financetracker.finance_tracker_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
		"com.financetracker.finance_tracker_web.model",
		"com.financetracker.finance_tracker_web.common.model",
		"com.financetracker.finance_tracker_web.financial_tracker.model",
		//"com.financetracker.finance_tracker_web.admin.model" //Dont just use admin: you do not want to scan views/controllers etc.
})
@EnableJpaRepositories(basePackages = {
		"com.financetracker.finance_tracker_web.common.repository",
		"com.financetracker.finance_tracker_web.financial_tracker.repository"
}) // Scan for JPA repositories
public class FinanceTrackerWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerWebApplication.class, args);
	}

}
