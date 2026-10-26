package com.bookflow;

import org.springframework.boot.SpringApplication;

public class TestBookflowApplication {

  public static void main(String[] args) {
    SpringApplication.from(BookflowApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
