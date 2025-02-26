package se.callistaenterprise.scheduler.config;

import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "scheduler.working-hours")
public class WorkingHours {

  private final String start;
  private final String end;

  @ConstructorBinding
  public WorkingHours(String start, String end) {
    this.start = start;
    this.end = end;
  }

  public LocalTime getStart() {
    return LocalTime.parse(start);
  }

  public LocalTime getEnd() {
    return LocalTime.parse(end);
  }
}
