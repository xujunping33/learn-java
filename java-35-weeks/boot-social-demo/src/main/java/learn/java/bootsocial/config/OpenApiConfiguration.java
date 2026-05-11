package learn.java.bootsocial.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "docker"})
public class OpenApiConfiguration {

    /** OpenAPI 中安全方案的名称（{@code @SecurityRequirement} 引用）；非浏览器里的 cookie 名 */
    public static final String SECURITY_SCHEME_NAME = "bearerToken";

    @Bean
    public OpenAPI openAPI(AppProperties appProperties) {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("boot-social-demo")
                                .description(
                                        "小型社交 API（Sa-Token）。先执行 register/login 拿到 token，然后在请求头带："
                                                + " **`Authorization: Bearer <token>`**（也可在 Swagger 的 **Authorize** 里填）。"
                                                + "\n\n"
                                                + "鉴权错误码（401/403）：`AUTH_REQUIRED`、`AUTH_EXPIRED`、`AUTH_INVALID_TOKEN`、`AUTH_INVALID_CREDENTIALS`、`FORBIDDEN`。")
                                .version("0.0.1"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Sa-Token（非 JWT 也可用 bearer 头传递 token）")));
    }
}
