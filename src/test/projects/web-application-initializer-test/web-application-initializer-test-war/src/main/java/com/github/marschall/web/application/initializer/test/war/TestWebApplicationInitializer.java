package com.github.marschall.web.application.initializer.test.war;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class TestWebApplicationInitializer implements WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext container) {

    // Create the 'root' Spring application context
    AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    rootContext.register(ApplicationConfiguration.class);

    // Manage the lifecycle of the root application context
    container.addListener(new ContextLoaderListener(rootContext));

    registerController(container, "/*");
  }

  private static void registerController(ServletContext container, String urlPattern) {

    // Create the dispatcher servlet's Spring application context
    AnnotationConfigWebApplicationContext dispatcherContext =
        new AnnotationConfigWebApplicationContext();
    dispatcherContext.register(DispatcherConfiguration.class);

    Dynamic dispatcher = container.addServlet("dispatcher-1", new DispatcherServlet(dispatcherContext));
    dispatcher.addMapping(urlPattern);

  }

}
