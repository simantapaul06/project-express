package com.projectexpress.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false"
})
@ActiveProfiles("test")
class JobServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
