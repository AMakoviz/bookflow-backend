package com.bookflow.mapper;

import com.bookflow.config.MapStructConfiguration;
import com.bookflow.dto.response.BookingResponse;
import com.bookflow.entity.BookingEntity;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfiguration.class, uses = RoomMapper.class)
public interface BookingMapper {

  @Mapping(target = "employeeId", source = "employee.id")
  @Mapping(target = "start", source = "startAt")
  @Mapping(target = "end", source = "endAt")
  BookingResponse toResponse(BookingEntity booking, @Context ZoneId officeZone);

  default OffsetDateTime toOffsetDateTime(Instant value, @Context ZoneId officeZone) {
    return value == null ? null : value.atZone(officeZone).toOffsetDateTime();
  }
}
