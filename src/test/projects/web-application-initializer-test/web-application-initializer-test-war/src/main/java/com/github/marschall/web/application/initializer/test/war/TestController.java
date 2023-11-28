package com.github.marschall.web.application.initializer.test.war;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @GetMapping(path = "/", produces = "text/plain")
  public String test() {
    return "test";
  }
}
