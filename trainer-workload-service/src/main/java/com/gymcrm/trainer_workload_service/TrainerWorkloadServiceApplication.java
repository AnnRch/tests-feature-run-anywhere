package com.gymcrm.trainer_workload_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.jms.annotation.EnableJms;

//@EnableJms
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.gymcrm.trainer_workload_service")
public class TrainerWorkloadServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainerWorkloadServiceApplication.class, args);
	}

}
