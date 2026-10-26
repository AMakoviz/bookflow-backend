package com.bookflow.exception;

import java.util.UUID;

public final class EmployeeNotFoundException extends RuntimeException {

  public EmployeeNotFoundException(UUID employeeId) {
    super("Employee %s was not found".formatted(employeeId));
  }
}
