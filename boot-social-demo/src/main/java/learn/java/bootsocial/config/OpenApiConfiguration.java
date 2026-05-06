package learn.java.bootsocial.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class OpenApiConfiguration {

    /** OpenAPI 中安全方案的名称（{@code @SecurityRequirement} 引用）；非浏览器里的 cookie 名 */
    public static final String SECURITY_SCHEME_NAME = "sessionCookie";

    @Bean
    public OpenAPI openAPI(AppProperties appProperties) {
        String cookieName = appProperties.getSession().getCookieName();
        return new OpenAPI()
                .info(
                        new Info()
                                .title("boot-social-demo")
                                .description(
                                        "小型社交 API（Session）。Cookie 名为 **`"
                                                + cookieName
                                                + "`**（由 `app.session.cookie-name` 配置）。可先执行 "
                                                + "register/login，Swagger 同域请求会携带 Set-Cookie；或在开发者工具复制该 cookie 后在 **Authorize** 填入同名参数。")
                                .version("0.0.1"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.COOKIE)
                                                .name(cookieName)
                                                .description("HttpSession；注册或登录成功后由服务端 Set-Cookie 写入")));
    }
}
