package com.bookflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employees", schema = "booking")
@Getter
@Setter
public class EmployeeEntity {

  @Id private UUID id;

  @Column(name = "display_name", nullable = false, length = 160)
  private String displayName;

  @Column(nullable = false, length = 254)
  private String email;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected EmployeeEntity() {}
}
