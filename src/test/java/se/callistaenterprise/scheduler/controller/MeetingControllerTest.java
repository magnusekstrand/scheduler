package se.callistaenterprise.scheduler.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static se.callistaenterprise.scheduler.model.Either.right;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;
import se.callistaenterprise.scheduler.dto.MeetingDto;
import se.callistaenterprise.scheduler.entity.Meeting;
import se.callistaenterprise.scheduler.mapping.MeetingMapper;
import se.callistaenterprise.scheduler.model.Either;
import se.callistaenterprise.scheduler.service.MeetingService;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MeetingService meetingService;

  @MockitoBean private MeetingMapper meetingMapper;

  @Test
  void addMeeting_shouldReturnOk_whenMeetingIsValid() throws Exception {
    MeetingDto requestDto =
        new MeetingDto(
            null, "Team Meeting", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0));
    MeetingDto responseDto =
        new MeetingDto(
            1L, "Team Meeting", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0));
    Meeting meeting =
        Meeting.builder()
            .id(1L)
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(11, 0))
            .build();

    when(meetingMapper.mapToMeeting(any(MeetingDto.class))).thenReturn(meeting);
    when(meetingMapper.mapToMeetingDto(any(Meeting.class))).thenReturn(responseDto);
    when(meetingService.addMeeting(any(Meeting.class))).thenReturn(Either.left(meeting));

    String requestJson = objectMapper.writeValueAsString(requestDto);
    System.out.println(requestJson);

    mockMvc
        .perform(
            post("/api/scheduler/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk());
  }

  @Test
  void getMeeting_shouldReturnMeeting_whenMeetingExists() throws Exception {
    Meeting meeting =
        Meeting.builder()
            .id(1L)
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(11, 0))
            .build();
    MeetingDto responseDto =
        new MeetingDto(
            1L, "Team Meeting", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0));

    when(meetingService.getMeeting(1L)).thenReturn(Either.left(meeting));
    when(meetingMapper.mapToMeetingDto(meeting)).thenReturn(responseDto);

    mockMvc
        .perform(get("/api/scheduler/meetings/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void getMeeting_shouldReturnNotFound_whenMeetingDoesNotExist() throws Exception {
    Errors errors = mock(Errors.class);
    when(errors.hasErrors()).thenReturn(true);
    when(meetingService.getMeeting(1L)).thenReturn(right(errors));

    mockMvc
        .perform(get("/api/scheduler/meetings/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void addMeeting_shouldReturnBadRequest_whenMeetingIsInvalid() throws Exception {
    MeetingDto invalidMeetingDto = new MeetingDto(null, null, null, null, null);
    Errors errors = mock(Errors.class);

    when(errors.hasErrors()).thenReturn(true);
    when(meetingService.addMeeting(any())).thenReturn(right(errors));

    mockMvc
        .perform(
            post("/api/scheduler/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMeetingDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getMeetings_shouldReturnListOfMeetings() throws Exception {
    MeetingDto meeting1 =
        new MeetingDto(
            1L, "Team Meeting", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0));
    MeetingDto meeting2 =
        new MeetingDto(
            2L, "Project Update", LocalDate.now(), LocalTime.of(12, 0), LocalTime.of(13, 0));

    when(meetingService.getMeetings())
        .thenReturn(
            List.of(
                Meeting.builder()
                    .id(1L)
                    .title("Team Meeting")
                    .date(LocalDate.now())
                    .start(LocalTime.of(10, 0))
                    .end(LocalTime.of(11, 0))
                    .build(),
                Meeting.builder()
                    .id(2L)
                    .title("Project Update")
                    .date(LocalDate.now())
                    .start(LocalTime.of(12, 0))
                    .end(LocalTime.of(13, 0))
                    .build()));
    when(meetingMapper.mapToMeetingDto(any(Meeting.class)))
        .thenReturn(meeting1)
        .thenReturn(meeting2);

    mockMvc
        .perform(get("/api/scheduler/meetings").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void getMeetings_shouldReturnEmptyList_whenNoMeetingsExist() throws Exception {
    when(meetingService.getMeetings()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/scheduler/meetings").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
