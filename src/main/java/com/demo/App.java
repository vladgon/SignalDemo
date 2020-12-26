package com.demo;

import org.springframework.boot.SpringApplication;

/**
 * Created by vova on 12/21/20
 */
public class App {
    public static void main(String[] args) {
        SpringApplication.run(ProxyConfig.class, args).getBean(ProxyTunnel.class).listen();
    }
}
