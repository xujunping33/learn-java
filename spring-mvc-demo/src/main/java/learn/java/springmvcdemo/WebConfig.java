package learn.java.springmvcdemo;

import learn.java.springmvcdemo.web.interceptor.AuthInterceptor;
import learn.java.springmvcdemo.web.interceptor.RequestLoggingInterceptor;
import learn.java.springmvcdemo.web.interceptor.TrafficStatsInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan("learn.java.springmvcdemo.web")
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final AuthInterceptor authInterceptor;
    private final TrafficStatsInterceptor trafficStatsInterceptor;

    public WebConfig(
            RequestLoggingInterceptor requestLoggingInterceptor,
            AuthInterceptor authInterceptor,
            TrafficStatsInterceptor trafficStatsInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
        this.authInterceptor = authInterceptor;
        this.trafficStatsInterceptor = trafficStatsInterceptor;
    }

    /** Day160：全 `/api/**` 打访问日志；仅 `/api/students/**` 要求 `X-Api-Key`（`/api/ping` 不在模式中故无需密钥）。 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trafficStatsInterceptor).addPathPatterns("/api/**").order(0);
        registry.addInterceptor(requestLoggingInterceptor).addPathPatterns("/api/**").order(1);
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/students/**", "/api/stats").order(2);
    }

    /** Day159：`/api/**` 允许来自 Vite 常用端口的跨域；浏览器会先 `OPTIONS` 预检再发正式请求。 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /** 未匹配的 URL 交给容器 DefaultServlet，便于 `src/main/webapp` 下静态页。 */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
