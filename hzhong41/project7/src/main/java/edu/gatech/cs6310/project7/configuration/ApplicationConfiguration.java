package edu.gatech.cs6310.project7.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Define Spring Beans for our application to use.
 */
@Slf4j
@Configuration
public class ApplicationConfiguration {

    @Value("${spring.datasource.url}")
    private String connectionString;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory() {
        return new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
                    Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                ContextResource resource = new ContextResource();
                resource.setName("jdbc/Project7");
                resource.setType(DataSource.class.getName());
                resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
                resource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                resource.setProperty("url", connectionString);
                resource.setProperty("username", username);
                resource.setProperty("password", password);
                resource.setProperty("maxActive", "100");
                resource.setProperty("maxIdle", "30");
                resource.setProperty("maxWait", "10000");

                context.getNamingResources().addResource(resource);
            }
        };
    }

    @Bean
    public InitialContext ctx() throws NamingException {

        return new InitialContext();
    }
}
