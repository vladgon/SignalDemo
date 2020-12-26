package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by vova on 12/21/20
 */
@Configuration
public class ProxyConfig {
    @Bean
    ProxyTunnel proxy(@Value("${host}") String host, @Value("${port}") int port) {
        return new ProxyTunnel(host, port);
    }
}
