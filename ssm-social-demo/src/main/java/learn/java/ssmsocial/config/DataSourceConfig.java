package learn.java.ssmsocial.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        Properties props = DbProps.load();

        HikariConfig cfg = new HikariConfig();
        // Some environments don't auto-register MySQL driver via ServiceLoader;
        // setting it explicitly avoids "No suitable driver".
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setJdbcUrl(props.getProperty("db.url"));
        cfg.setUsername(props.getProperty("db.user"));
        cfg.setPassword(props.getProperty("db.password", ""));
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(10_000);
        cfg.setPoolName("ssm-social");

        return new HikariDataSource(cfg);
    }
}

