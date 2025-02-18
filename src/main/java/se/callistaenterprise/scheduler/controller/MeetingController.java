package se.callistaenterprise.scheduler.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.callistaenterprise.scheduler.dto.MeetingDto;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.mapping.MeetingMapper;
import se.callistaenterprise.scheduler.model.Meeting;
import se.callistaenterprise.scheduler.service.MeetingService;

import java.time.LocalDate;
import java.util.List;

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
        try {
            Meeting meeting = meetingService.getMeeting(id);
            if (meeting == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(meetingMapper.mapToMeetingDto(meeting));
        } catch (BadRequestException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/meetings")
    public ResponseEntity<MeetingDto> addMeeting(@RequestBody MeetingDto meetingDto) {
        try {
            return ResponseEntity.ok(meetingMapper.mapToMeetingDto(
                meetingService.addMeeting(meetingMapper.mapToMeeting(meetingDto))));
        } catch (BadRequestException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/meetings/{date}/{meetingTimeInMinutes}")
    public ResponseEntity<List<MeetingDto>> getMeetingByDuration(@PathVariable LocalDate date, @PathVariable Long meetingTimeInMinutes) {
        try {
            List<Meeting> meetings = meetingService.addMeeting(date, meetingTimeInMinutes);
            return ResponseEntity.ok(meetings.stream().map(meetingMapper::mapToMeetingDto).toList());
        } catch (BadRequestException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
