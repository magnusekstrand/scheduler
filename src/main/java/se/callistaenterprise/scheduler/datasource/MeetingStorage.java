package se.callistaenterprise.scheduler.datasource;

import se.callistaenterprise.scheduler.model.Meeting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static se.callistaenterprise.scheduler.service.MeetingService.dateComparator;
import static se.callistaenterprise.scheduler.service.MeetingService.timeComparator;

public class MeetingStorage {

    private static final AtomicLong id = new AtomicLong(1L);
    private static final Set<Meeting> meetings = new HashSet<>();

    public static List<Meeting> findAll() {
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
