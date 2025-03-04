package se.callistaenterprise.scheduler.config;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.entity.Meeting;

@Slf4j
@Configuration
public class StorageConfig {

  @Bean
  public MeetingStorage meetingStorage() {
    return setupMeetingStorage(new MeetingStorage());
  }

  private MeetingStorage setupMeetingStorage(final MeetingStorage storage) {
    Meeting m1 =
        Meeting.builder()
            .title("Lunch phone call")
            .date(LocalDate.now())
            .start(LocalTime.parse("12:00"))
            .end(LocalTime.parse("12:15"))
            .build();
    Meeting m2 =
        Meeting.builder()
            .title("Extended team standup")
            .date(LocalDate.now())
            .start(LocalTime.parse("09:15"))
            .end(LocalTime.parse("10:45"))
            .build();
    Meeting m3 =
        Meeting.builder()
            .title("Design review")
            .date(LocalDate.now())
            .start(LocalTime.parse("15:00"))
            .end(LocalTime.parse("15:30"))
            .build();

    log.info("Insert meeting #1: {}", storage.add(m1));
    log.info("Insert meeting #2: {}", storage.add(m2));
    log.info("Insert meeting #3: {}", storage.add(m3));

    return storage;
  }
}
