package se.callistaenterprise.scheduler.datasource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import se.callistaenterprise.scheduler.entity.Meeting;

public class MeetingStorage {

  public static final Comparator<Meeting> dateComparator =
      (o1, o2) -> o1.getDate().compareTo(o2.getDate());
  public static final Comparator<Meeting> timeComparator =
      (o1, o2) -> o1.getStart().compareTo(o2.getStart());
  private static final AtomicLong id = new AtomicLong(1L);

  private static MeetingList meetings;

  public MeetingStorage() {
    meetings = new MeetingList();
  }

  public synchronized Meeting add(Meeting meeting) {
    if (!meetings.add(meeting)) {
      return null;
    }
    return meetings.getLast();
  }

  /*
  Returns all meetings unsorted
  */
  public synchronized List<Meeting> getAll() {
    return meetings.stream().toList();
  }

  public synchronized Meeting getById(Long id) {
    return meetings.stream().filter(meeting -> meeting.getId().equals(id)).findFirst().orElse(null);
  }

  public synchronized boolean remove(Long id) {
    return meetings.removeIf(meeting -> meeting.getId().equals(id));
  }

  public int size() {
    return meetings.size();
  }

  /*
   Returns all meetings sorted by date and start time
  */
  public synchronized List<Meeting> sort() {
    return sort(List.of(dateComparator, timeComparator));
  }

  /*
   Returns all meetings sorted by the comparators given as arguments.
   The comparators are applied in the order they appear in the list.
  */
  public synchronized List<Meeting> sort(List<Comparator<Meeting>> comparators) {
    if (comparators == null) {
      return getAll(); // return unsorted list
    }

    Comparator<Meeting> comparator = comparators.getFirst();
    for (int i = 1; i < comparators.size(); i++) {
      comparator = comparator.thenComparing(comparators.get(i));
    }

    return meetings.stream().sorted(comparator).toList();
  }

  /*
   The actual storage of all meetings.
  */
  private static class MeetingList extends ArrayList<Meeting> {

    @Override
    public boolean add(Meeting meeting) {
      if (meeting == null) {
        return false;
      }

      if (meeting.getId() != null) {
        throw new RuntimeException("Meeting.id must be null");
      }

      meeting.setId(id.getAndIncrement());
      return super.add(meeting);
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Meeting meeting)) {
        return false;
      }

      if (meeting.getId() == null) {
        throw new RuntimeException("Meeting.id cannot be null");
      }

      return contains(meeting.getId());
    }

    public boolean contains(Long id) {
      return this.stream().anyMatch(meeting -> meeting.getId().equals(id));
    }
  }
}
