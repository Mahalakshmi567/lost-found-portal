package com.lostfound.lostfoundportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async processing app-wide. EmailService's send() method runs on
 * a background thread because of this, so submitting a claim or reporting
 * a found item doesn't have to wait for the SMTP round trip to finish.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}