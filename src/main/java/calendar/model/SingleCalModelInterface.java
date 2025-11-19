package calendar.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This interface represents a single calendar with an associated timezone.
 * It extends {@link CalModelInterface} by adding support for
 * timezone management. All event times stored in this calendar are assumed
 * to be in the calendar's timezone.
 */
public interface SingleCalModelInterface extends CalModelInterface {
  /**
   * Finds a unique event in this calendar by subject and start time.
   * Optionally, an end time can be specified for a stricter match.
   *
   * @param subject the subject of the event
   * @param start   the start date/time of the event
   * @param end     the end date/time of the event, may be null
   * @return the uniquely matched {@link Event}
   * @throws IllegalArgumentException if no event or multiple events match
   */
  Event findEvent(String subject, LocalDateTime start, LocalDateTime end)
      throws IllegalArgumentException;

  /**
   * Returns the {@link ZoneId} of this calendar.
   *
   * @return the timezone of this calendar
   */
  ZoneId getTimezone();

  /**
   * Sets a new timezone for this calendar.
   * Implementations should decide how to handle existing event times when
   * the timezone changes.
   *
   * @param newTimezone the new timezone to set
   * @throws IllegalArgumentException if {@code newTimezone} is {@code null}
   */
  void setTimezone(ZoneId newTimezone) throws IllegalArgumentException;
}
