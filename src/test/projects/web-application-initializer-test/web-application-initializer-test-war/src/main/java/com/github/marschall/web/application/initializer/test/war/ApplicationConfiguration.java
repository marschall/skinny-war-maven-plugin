package com.github.marschall.web.application.initializer.test.war;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public DataSource dataSource() {
    try {
      return (DataSource) new InitialContext().lookup("java:comp/DefaultDataSource");
    } catch (NamingException e) {
      throw new BeanCreationException("could not look up default data source", e);
    }
  }

  @Bean
  public JdbcOperations jdbcTemplate() {
    return new JdbcTemplate(this.dataSource());
  }

  @Bean
  public PlatformTransactionManager txManager() {
    return new JtaTransactionManager();
  }

}
