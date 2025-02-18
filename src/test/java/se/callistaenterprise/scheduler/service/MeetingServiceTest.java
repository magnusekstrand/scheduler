package se.callistaenterprise.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import se.callistaenterprise.scheduler.datasource.MeetingStorage;
import se.callistaenterprise.scheduler.exception.BadRequestException;
import se.callistaenterprise.scheduler.model.Meeting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class MeetingServiceTest {

    private final MeetingService meetingService = new MeetingService();

    @BeforeEach
    void beforeEach() {
        MeetingStorage.removeAll();
    }

    @Test
    void testAddMeetingSuccessfully() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = "Team Standup";
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(9, 30);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();

            mockedStatic.when(() -> MeetingStorage.insert(any(Meeting.class))).thenReturn(meetingToAdd);

            Meeting addedMeeting = meetingService.addMeeting(meetingToAdd);

            assertNotNull(addedMeeting);
            assertEquals(title, addedMeeting.getTitle());
            assertEquals(date, addedMeeting.getDate());
            assertEquals(start, addedMeeting.getStart());
            assertEquals(end, addedMeeting.getEnd());

            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)));
        }
    }

    @Test
    void testAddMeetingThrowsBadRequestForConflictingTime() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = "Design Review";
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(10, 0);
            LocalTime end = LocalTime.of(11, 0);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();

            List<Meeting> existingMeetings = List.of(
                Meeting.builder().title("Existing Meeting").date(date).start(LocalTime.of(9, 30)).end(LocalTime.of(10, 30)).build()
            );

            when(MeetingStorage.findAll()).thenReturn(existingMeetings);

            assertThrows(BadRequestException.class, () -> meetingService.addMeeting(meetingToAdd));
            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)), never());
        }
    }

    @Test
    void testAddMeetingThrowsBadRequestForInvalidInput() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = "";
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(10, 0);
            LocalTime end = LocalTime.of(9, 0);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();

            assertThrows(BadRequestException.class, () -> meetingService.addMeeting(meetingToAdd));
            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)), never());
        }
    }

    @Test
    void testAddMeetingThrowsBadRequestForNullTitle() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = null;
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(10, 0);
            LocalTime end = LocalTime.of(11, 0);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();

            assertThrows(BadRequestException.class, () -> meetingService.addMeeting(meetingToAdd));
            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)), never());
        }
    }

    @Test
    void testAddMeetingTimeAvailableAtStartOfDay() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = "Early Meeting";
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(9, 30);

            List<Meeting> existingMeetings = List.of(
                    Meeting.builder().title("Later Meeting").date(date).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build()
            );

            when(MeetingStorage.findAll()).thenReturn(existingMeetings);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();
            when(MeetingStorage.insert(any(Meeting.class))).thenReturn(meetingToAdd);

            Meeting addedMeeting = meetingService.addMeeting(meetingToAdd);

            assertNotNull(addedMeeting);
            assertEquals(title, addedMeeting.getTitle());
            assertEquals(date, addedMeeting.getDate());
            assertEquals(start, addedMeeting.getStart());
            assertEquals(end, addedMeeting.getEnd());

            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)));
        }
    }

    @Test
    void testAddMeetingTimeAvailableAtEndOfDay() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = "Wrap-Up Meeting";
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(16, 30);
            LocalTime end = LocalTime.of(17, 0);

            List<Meeting> existingMeetings = List.of(
                    Meeting.builder().title("Earlier Meeting").date(date).start(LocalTime.of(15, 0)).end(LocalTime.of(16, 0)).build()
            );

            when(MeetingStorage.findAll()).thenReturn(existingMeetings);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();
            when(MeetingStorage.insert(any(Meeting.class))).thenReturn(meetingToAdd);

            Meeting addedMeeting = meetingService.addMeeting(meetingToAdd);

            assertNotNull(addedMeeting);
            assertEquals(title, addedMeeting.getTitle());
            assertEquals(date, addedMeeting.getDate());
            assertEquals(start, addedMeeting.getStart());
            assertEquals(end, addedMeeting.getEnd());

            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)));
        }
    }

    @Test
    void testAddMeetingTimeAvailableBetweenExistingMeetings() {
        try (MockedStatic<MeetingStorage> mockedStatic = mockStatic(MeetingStorage.class)) {
            String title = "Lunch Meeting";
            LocalDate date = LocalDate.of(2023, 10, 10);
            LocalTime start = LocalTime.of(11, 30);
            LocalTime end = LocalTime.of(12, 30);

            List<Meeting> existingMeetings = List.of(
                Meeting.builder().title("Team Standup").date(date).start(LocalTime.of(9, 0)).end(LocalTime.of(9, 30)).build(),
                Meeting.builder().title("Design Review").date(date).start(LocalTime.of(10, 15)).end(LocalTime.of(11, 15)).build(),
                Meeting.builder().title("Retrospective").date(date).start(LocalTime.of(13, 0)).end(LocalTime.of(15, 0)).build(),
                Meeting.builder().title("Team information meeting").date(date.plusDays(1)).start(LocalTime.of(11, 0)).end(LocalTime.of(12, 0)).build()
            );

            when(MeetingStorage.findAll()).thenReturn(existingMeetings);

            Meeting meetingToAdd = Meeting.builder().title(title).date(date).start(start).end(end).build();
            when(MeetingStorage.insert(any(Meeting.class))).thenReturn(meetingToAdd);

            Meeting addedMeeting = meetingService.addMeeting(meetingToAdd);

            assertNotNull(addedMeeting);
            assertEquals(title, addedMeeting.getTitle());
            assertEquals(date, addedMeeting.getDate());
            assertEquals(start, addedMeeting.getStart());
            assertEquals(end, addedMeeting.getEnd());

            mockedStatic.verify(() -> MeetingStorage.insert(any(Meeting.class)));
        }
    }

}
