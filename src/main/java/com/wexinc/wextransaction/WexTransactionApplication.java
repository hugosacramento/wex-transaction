package com.wexinc.wextransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.wexinc.wextransaction.infra.client.feign")
public class WexTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WexTransactionApplication.class, args);
    }

}
