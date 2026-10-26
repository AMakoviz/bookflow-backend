package com.bookflow.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {

  @Bean
  Clock bookFlowClock() {
    return Clock.systemUTC();
  }

  @Bean
  ZoneId officeZone(@Value("${bookflow.office-zone}") String zoneId) {
    return ZoneId.of(zoneId);
  }
}
