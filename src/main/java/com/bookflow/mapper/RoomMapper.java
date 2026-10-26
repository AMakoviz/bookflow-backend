package com.bookflow.mapper;

import com.bookflow.config.MapStructConfiguration;
import com.bookflow.dto.response.RoomResponse;
import com.bookflow.entity.RoomEntity;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfiguration.class)
public interface RoomMapper {

  RoomResponse toResponse(RoomEntity room);
}
