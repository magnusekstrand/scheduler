package se.callistaenterprise.scheduler.datasource;

import se.callistaenterprise.scheduler.model.Meeting;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class MeetingStorage {

    private static final AtomicLong id = new AtomicLong();
    private static final Set<Meeting> meetings = new HashSet<>();

    public static List<Meeting> findAll() {
        final Comparator<Meeting> dateComparator = (o1, o2) -> o1.getDate().compareTo(o2.getDate());
        final Comparator<Meeting> timeComparator = (o1, o2) -> o1.getStart().compareTo(o2.getStart());
        return meetings.stream().sorted(dateComparator.thenComparing(timeComparator)).toList();
    }

    public static Meeting find(Long id) {
        return meetings.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    public static Meeting insert(Meeting meeting) {
        if (meeting == null) {
            return null;
        }

        if (meeting.getId() != null) {
            throw new RuntimeException("Meeting.id must be null");
        }

        meeting.setId(id.getAndIncrement());

        if (meetings.add(meeting)) {
            return meeting;
        } else {
            return null;
        }
    }

    public static void removeAll() {
        meetings.clear();
    }

    public static int size() {
        return meetings.size();
    }
}
