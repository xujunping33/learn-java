package learn.java.ssmsocial.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mappers/*.xml"));
        return bean.getObject();
    }

    /** 扫描 Mapper 接口，生成代理 Bean。 */
    @Bean
    public static MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer cfg = new MapperScannerConfigurer();
        cfg.setBasePackage("learn.java.ssmsocial.mapper");
        return cfg;
    }
}

