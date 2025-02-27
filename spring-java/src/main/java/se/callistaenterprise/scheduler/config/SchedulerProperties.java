package se.callistaenterprise.scheduler.config;

import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

  private final List<String> weekends;
  private final WorkingHours workingHours;

  @ConstructorBinding
  public SchedulerProperties(List<String> weekends, WorkingHours workingHours) {
    this.weekends = weekends;
    this.workingHours = workingHours;
  }

  public List<String> getWeekends() {
    return weekends.stream().map(String::toUpperCase).toList();
  }

  public static class WorkingHours {
    private final String start;
    private final String end;

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
}
