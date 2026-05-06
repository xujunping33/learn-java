package learn.java.bootsocial;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import learn.java.bootsocial.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Day 182：Testcontainers MySQL + 薄 HTTP 主链路（注册 → 发帖 → 列表命中）。
 *
 * <p>由 Failsafe 在 {@code mvn verify} 中执行；需本机 Docker。无 Docker 时类级规则会跳过（不失败）。
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class SocialFlowIT {

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("ssm_social")
                    .withUsername("tc")
                    .withPassword("tc_secret")
                    .withUrlParam("characterEncoding", "utf8")
                    .withUrlParam("serverTimezone", "Asia/Shanghai")
                    .withInitScript("testcontainers-schema.sql");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AppProperties appProperties;

    @Test
    void registerThenCreatePostThenListContainsPost() throws Exception {
        String base = "http://127.0.0.1:" + port;
        RestTemplate rt = new RestTemplate();

        String u = "tc_u_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "secret12";
        String title = "it_title_" + UUID.randomUUID();

        HttpHeaders json = new HttpHeaders();
        json.setContentType(MediaType.APPLICATION_JSON);

        String regBody = "{\"username\":\"" + u + "\",\"password\":\"" + password + "\"}";
        ResponseEntity<String> reg =
                rt.postForEntity(base + "/api/auth/register", new HttpEntity<>(regBody, json), String.class);
        assertThat(reg.getStatusCode().value()).isEqualTo(200);
        String cookie = sessionCookieHeader(reg, appProperties.getSession().getCookieName());
        assertThat(cookie).isNotBlank();

        JsonNode regJson = objectMapper.readTree(reg.getBody());
        assertThat(regJson.path("ok").asBoolean()).isTrue();

        HttpHeaders authed = new HttpHeaders();
        authed.setContentType(MediaType.APPLICATION_JSON);
        authed.add(HttpHeaders.COOKIE, cookie);

        String postBody = "{\"title\":\"" + title + "\",\"content\":\"integration body\"}";
        ResponseEntity<String> postResp =
                rt.postForEntity(base + "/api/posts", new HttpEntity<>(postBody, authed), String.class);
        assertThat(postResp.getStatusCode().value()).isEqualTo(201);
        JsonNode postJson = objectMapper.readTree(postResp.getBody());
        long postId = postJson.path("data").path("id").asLong();
        assertThat(postId).isPositive();

        ResponseEntity<String> listResp = rt.getForEntity(base + "/api/posts?page=1&size=20", String.class);
        assertThat(listResp.getStatusCode().value()).isEqualTo(200);
        JsonNode listJson = objectMapper.readTree(listResp.getBody());
        assertThat(listJson.path("ok").asBoolean()).isTrue();
        JsonNode items = listJson.path("data").path("items");
        assertThat(items.isArray()).isTrue();

        boolean found = false;
        for (JsonNode it : items) {
            if (it.path("id").asLong() == postId && title.equals(it.path("title").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("list should contain created post id=%s title=%s", postId, title).isTrue();
    }

    private static String sessionCookieHeader(ResponseEntity<String> response, String cookieName) {
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).as("Set-Cookie present").isNotNull();
        String prefix = cookieName + "=";
        for (String raw : setCookies) {
            if (raw == null) {
                continue;
            }
            String first = raw.split(";", 2)[0].trim();
            if (first.startsWith(prefix)) {
                return first;
            }
        }
        throw new IllegalStateException("No " + cookieName + " in Set-Cookie: " + setCookies);
    }
}
