package learn.java.ssmsocial;

import learn.java.ssmsocial.config.DataSourceConfig;
import learn.java.ssmsocial.config.MyBatisConfig;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import learn.java.ssmsocial.web.interceptor.AuthInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@Import({DataSourceConfig.class, MyBatisConfig.class})
@ComponentScan({"learn.java.ssmsocial.web", "learn.java.ssmsocial.service"})
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/posts",
                        "/api/posts/*/comments",
                        "/api/posts/*/like");
    }

    /** 未匹配的 URL 交给容器 DefaultServlet，便于后续放静态页到 webapp。 */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}

