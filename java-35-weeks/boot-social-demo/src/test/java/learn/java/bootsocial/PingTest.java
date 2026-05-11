package learn.java.bootsocial;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PingTest {

    @LocalServerPort
    int port;

    @Test
    void pingReturnsOkTrue() {
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> resp = rt.getForEntity("http://localhost:" + port + "/api/ping", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).contains("\"ok\":true");
        assertThat(resp.getBody()).contains("\"data\":true");
    }
}

