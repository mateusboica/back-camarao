package back.camarao.sistema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017/test",
		"app.security.jwt.secret=01234567890123456789012345678901",
		"app.security.jwt.expiration-ms=3600000"
})
class SistemaApplicationTests {

	@Test
	void contextLoads() {
	}

}
