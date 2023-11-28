package com.github.marschall.web.application.initializer.test.war;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class DispatcherConfiguration {

  @Bean
  public TestController testController() {
    return new TestController();
  }

}
