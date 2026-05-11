package learn.java.ssmsocial;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/** Day162：Servlet 3+ 容器启动入口（代替 web.xml），注册 DispatcherServlet。 */
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] {WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
}

