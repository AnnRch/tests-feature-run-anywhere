package com.gymcrm.trainer_workload_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Test configuration for MongoDB.
 * 
 * Cross-platform support:
 * - Uses Testcontainers when Docker is available (works on Mac, Linux, Windows)
 * - Falls back to docker-compose MongoDB when Testcontainers is disabled
 * 
 * To disable Testcontainers and use docker-compose:
 * - Set property: testcontainers.enabled=false
 * - Or ensure docker-compose containers are running on port 27017 (MongoDB)
 */
@TestConfiguration(proxyBeanMethods = false)
public class MongoTestConfig {
    
    /**
     * Testcontainers MongoDB configuration.
     * Automatically creates and manages a MongoDB container.
     * Works on Mac, Linux, and Windows (when Docker is available).
     * 
     * Disabled when testcontainers.enabled=false
     */
    @Bean
    @ServiceConnection
    @ConditionalOnProperty(name = "testcontainers.enabled", havingValue = "true", matchIfMissing = true)
    public MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer("mongo:latest");
    }
}
