package se.callistaenterprise.scheduler.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import se.callistaenterprise.scheduler.dto.MeetingDto;
import se.callistaenterprise.scheduler.model.Meeting;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MeetingMapper {

    MeetingDto mapToMeetingDto(Meeting meeting);

    Meeting mapToMeeting(MeetingDto meetingDto);
}
