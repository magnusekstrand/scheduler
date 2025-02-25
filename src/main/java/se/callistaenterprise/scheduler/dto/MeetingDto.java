package se.callistaenterprise.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class MeetingDto {
    private Long id;

    private String title;

    private LocalDate date;
    private LocalTime start;
    private LocalTime end;

    public MeetingDto(Long id, String title, LocalDate date, LocalTime start, LocalTime end) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.start = start;
        this.end = end;
    }
}
