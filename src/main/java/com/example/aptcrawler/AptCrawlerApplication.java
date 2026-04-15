package com.example.aptcrawler;

import com.example.aptcrawler.scheduler.AptCrawlerScheduler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AptCrawlerApplication implements ApplicationRunner {

    private final AptCrawlerScheduler scheduler;

    public AptCrawlerApplication(AptCrawlerScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static void main(String[] args) {
        SpringApplication.run(AptCrawlerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        scheduler.run();
    }
}
