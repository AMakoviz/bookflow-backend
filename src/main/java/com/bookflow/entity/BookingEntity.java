package com.bookflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings", schema = "booking")
@Getter
@Setter
public class BookingEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_id", nullable = false)
  private RoomEntity room;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private EmployeeEntity employee;

  @Column(name = "start_at", nullable = false)
  private Instant startAt;

  @Column(name = "end_at", nullable = false)
  private Instant endAt;

  @Column(name = "participant_count", nullable = false)
  private int participantCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BookingStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected BookingEntity() {}

  private BookingEntity(
      UUID id,
      RoomEntity room,
      EmployeeEntity employee,
      Instant startAt,
      Instant endAt,
      int participantCount,
      BookingStatus status,
      Instant createdAt) {
    this.id = id;
    this.room = room;
    this.employee = employee;
    this.startAt = startAt;
    this.endAt = endAt;
    this.participantCount = participantCount;
    this.status = status;
    this.createdAt = createdAt;
  }

  public static BookingEntity confirmed(
      RoomEntity room,
      EmployeeEntity employee,
      Instant startAt,
      Instant endAt,
      int participantCount,
      Instant createdAt) {
    return new BookingEntity(
        UUID.randomUUID(),
        room,
        employee,
        startAt,
        endAt,
        participantCount,
        BookingStatus.CONFIRMED,
        createdAt);
  }
}
