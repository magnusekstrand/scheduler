package se.callistaenterprise.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.model.Meeting;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
public class ApplicationInitializer implements
    ApplicationListener<ContextRefreshedEvent> {

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        Meeting m1 = Meeting.builder().title("Lunch phone call").date(LocalDate.now()).start(LocalTime.parse("12:00")).end(LocalTime.parse("12:15")).build();
        Meeting m2 = Meeting.builder().title("Extended team standup").date(LocalDate.now()).start(LocalTime.parse("09:15")).end(LocalTime.parse("10:45")).build();
        Meeting m3 = Meeting.builder().title("Design review").date(LocalDate.now()).start(LocalTime.parse("15:00")).end(LocalTime.parse("15:30")).build();

        log.info("Insert meeting #1: {}", MeetingStorage.insert(m1));
        log.info("Insert meeting #2: {}", MeetingStorage.insert(m2));
        log.info("Insert meeting #3: {}", MeetingStorage.insert(m3));
    }
}
