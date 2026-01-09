package com.zetumall.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "supabase")
@Getter
@Setter
public class SupabaseProperties {
    private String url;
    private String anonKey;
    private String jwtSecret;
    private String storageBucket;
}
