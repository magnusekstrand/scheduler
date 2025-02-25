package se.callistaenterprise.scheduler.datasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.callistaenterprise.scheduler.entity.Meeting;

class MeetingStorageTest {

  @BeforeEach
  public void beforeEach() {
    MeetingStorage.removeAll();
  }

  @Test
  void testInsertValidMeeting() {
    // Arrange
    Meeting validMeeting =
        Meeting.builder()
            .title("Team Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(11, 0))
            .build();

    // Act
    Meeting insertedMeeting = MeetingStorage.insert(validMeeting);

    // Assert
    assertNotNull(insertedMeeting);
    assertNotNull(insertedMeeting.getId());
    assertEquals(1, MeetingStorage.size());
    assertEquals(validMeeting.getTitle(), insertedMeeting.getTitle());
    assertEquals(validMeeting.getDate(), insertedMeeting.getDate());
    assertEquals(validMeeting.getStart(), insertedMeeting.getStart());
    assertEquals(validMeeting.getEnd(), insertedMeeting.getEnd());
  }

  @Test
  void testInsertMeetingWithNonNullIdThrowsException() {
    // Arrange
    Meeting meetingWithId =
        Meeting.builder()
            .id(1L) // Id should be null for insertion
            .title("Improper Meeting")
            .date(LocalDate.now())
            .start(LocalTime.of(14, 0))
            .end(LocalTime.of(15, 0))
            .build();

    // Act & Assert
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> MeetingStorage.insert(meetingWithId));
    assertEquals("Meeting.id must be null", exception.getMessage());
    assertEquals(0, MeetingStorage.size());
  }

  @Test
  void testInsertNullMeetingReturnsNull() {
    // Act
    Meeting result = MeetingStorage.insert(null);

    // Assert
    assertNull(result);
    assertEquals(0, MeetingStorage.size());
  }

  @Test
  void testInsertDuplicateMeeting() {
    // Arrange
    Meeting initialMeeting =
        Meeting.builder()
            .title("Weekly Standup")
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(9, 30))
            .build();

    MeetingStorage.insert(initialMeeting);

    // Act
    Meeting duplicateMeeting =
        Meeting.builder()
            .title("Weekly Standup")
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(9, 30))
            .build(); // Duplicate content

    Meeting result = MeetingStorage.insert(duplicateMeeting);

    // Assert
    assertNotNull(result);
    assertEquals(2, MeetingStorage.size());
  }
}
