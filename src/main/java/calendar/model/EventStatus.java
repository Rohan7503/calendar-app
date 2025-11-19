package calendar.model;

/**
 * This enum represents the values that the status of an event can take.
 */
public enum EventStatus {
  PUBLIC,
  PRIVATE;

  /**
   * Converts a string to the corresponding {@link EventStatus} enum.
   * Comparison is case-insensitive.
   *
   * @param s the string to convert
   * @return the corresponding EventStatus
   * @throws IllegalArgumentException if no matching status is found
   */
  public static EventStatus fromString(String s) {
    for (EventStatus status : EventStatus.values()) {
      if (status.name().equalsIgnoreCase(s)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid event status: " + s);
  }
}
