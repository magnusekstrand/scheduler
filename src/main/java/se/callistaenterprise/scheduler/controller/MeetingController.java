package se.callistaenterprise.scheduler.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.callistaenterprise.scheduler.dto.MeetingDto;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.exception.NotFoundException;
import se.callistaenterprise.scheduler.mapping.MeetingMapper;
import se.callistaenterprise.scheduler.model.Either;
import se.callistaenterprise.scheduler.service.MeetingService;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
public class MeetingController {

  private final MeetingService meetingService;
  private final MeetingMapper meetingMapper;

  public MeetingController(MeetingService meetingService, MeetingMapper meetingMapper) {
    this.meetingService = meetingService;
    this.meetingMapper = meetingMapper;
  }

  @GetMapping("/meetings")
  public List<MeetingDto> getMeetings() {
    return meetingService.getMeetings().stream().map(meetingMapper::mapToMeetingDto).toList();
  }

  @GetMapping("/meetings/{id}")
  public ResponseEntity<MeetingDto> getMeeting(@PathVariable Long id) {
    Either<Meeting, Errors> response = meetingService.getMeeting(id);
    if (response.hasErrors()) {
      throw new NotFoundException("Meeting not found, id = " + id);
    }
    return ResponseEntity.ok(meetingMapper.mapToMeetingDto(response.getLeft()));
  }

  @GetMapping("/meetings/find")
  public ResponseEntity<List<MeetingDto>> getAvailableMeetingsByDuration(
      @RequestParam LocalDate date, @RequestParam(name = "duration") Long meetingTimeInMinutes) {
    List<MeetingDto> response =
        meetingService.addMeeting(date, meetingTimeInMinutes).stream()
            .map(meetingMapper::mapToMeetingDto)
            .toList();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/meetings")
  public ResponseEntity<MeetingDto> addMeeting(@RequestBody MeetingDto meetingDto) {
    Either<Meeting, Errors> response =
        meetingService.addMeeting(meetingMapper.mapToMeeting(meetingDto));
    if (response.hasErrors()) {
      throw new BadRequestException(response.getAllErrors().toString());
    }
    return ResponseEntity.ok(meetingMapper.mapToMeetingDto(response.getLeft()));
  }
}
