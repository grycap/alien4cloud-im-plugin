package es.upv.indigodc.alien4cloud.im.plugin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import alien4cloud.model.application.Application;

@SpringBootApplication
public class TestOrchestrator {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
  
}
