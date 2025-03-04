package se.callistaenterprise.scheduler.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.callistaenterprise.scheduler.entity.Meeting;

class MeetingStorageTest {

  MeetingStorage meetingStorage;

  @BeforeEach
  public void beforeEach() {
    meetingStorage = new MeetingStorage();
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
    Meeting insertedMeeting = meetingStorage.add(validMeeting);
    assertThat(meetingStorage.size()).isEqualTo(1);

    // Assert
    assertThat(insertedMeeting).isNotNull();
    assertThat(insertedMeeting.getId()).isNotNull();
    assertThat(validMeeting.getTitle()).isEqualTo(insertedMeeting.getTitle());
    assertThat(validMeeting.getDate()).isEqualTo(insertedMeeting.getDate());
    assertThat(validMeeting.getStart()).isEqualTo(insertedMeeting.getStart());
    assertThat(validMeeting.getEnd()).isEqualTo(insertedMeeting.getEnd());
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
    assertThatRuntimeException()
        .isThrownBy(() -> meetingStorage.add(meetingWithId))
        .withMessage("Meeting.id must be null");
    assertThat(meetingStorage.size()).isEqualTo(0);
  }

  @Test
  void testInsertNullMeetingIsUnsuccessfulAndNoMeetingsAreInserted() {
    // Act
    Meeting result = meetingStorage.add(null);

    // Assert
    assertThat(result).isNull();
    assertThat(meetingStorage.size()).isEqualTo(0);
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

    assertThat(meetingStorage.add(initialMeeting)).isNotNull();

    // Act
    Meeting duplicateMeeting =
        Meeting.builder()
            .title("Weekly Standup")
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(9, 30))
            .build(); // Duplicate content

    // Assert
    assertThat(meetingStorage.add(duplicateMeeting)).isNotNull();
    assertThat(meetingStorage.size()).isEqualTo(2);
    assertThat(meetingStorage.getAll().getFirst().getId())
        .isNotEqualTo(meetingStorage.getAll().getLast().getId());
  }

  @Test
  void testInsertMeetingsWithDifferentDates() {
    // Arrange
    Meeting meeting1 =
        Meeting.builder()
            .title("Meeting 1")
            .date(LocalDate.now())
            .start(LocalTime.of(9, 0))
            .end(LocalTime.of(10, 0))
            .build();
    Meeting meeting2 =
        Meeting.builder()
            .title("Meeting 2")
            .date(LocalDate.now().plusDays(1))
            .start(LocalTime.of(10, 0))
            .end(LocalTime.of(11, 0))
            .build();

    // Act
    meetingStorage.add(meeting1);
    meetingStorage.add(meeting2);

    // Assert
    assertThat(meetingStorage.size()).isEqualTo(2);
    assertThat(meetingStorage.getAll()).containsExactlyInAnyOrder(meeting1, meeting2);
  }
}
