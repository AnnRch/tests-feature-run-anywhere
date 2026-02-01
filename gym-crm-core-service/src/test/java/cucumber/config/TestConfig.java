package cucumber.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration for component tests.
 * 
 * Cross-platform support:
 * - Uses Testcontainers when Docker is available (works on Mac, Linux, Windows)
 * - Falls back to docker-compose PostgreSQL when Testcontainers is disabled
 * 
 * To disable Testcontainers and use docker-compose:
 * - Set property: testcontainers.enabled=false
 * - Or ensure docker-compose containers are running on ports 5439 (PostgreSQL)
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestConfig {
    
    /**
     * Testcontainers PostgreSQL configuration.
     * Automatically creates and manages a PostgreSQL container.
     * Works on Mac, Linux, and Windows (when Docker is available).
     * 
     * Disabled when testcontainers.enabled=false
     */
    @Bean
    @ServiceConnection
    @ConditionalOnProperty(name = "testcontainers.enabled", havingValue = "true", matchIfMissing = true)
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("gymcrm_test")
                .withUsername("postgres")
                .withPassword("postgres");
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return Mockito.mock(JmsTemplate.class);
    }
}
