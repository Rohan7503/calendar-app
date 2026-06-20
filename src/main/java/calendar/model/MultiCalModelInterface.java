package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * This interface represents a model that manages multiple calendars, each with its own
 * timezone and set of events. It defines operations for creating, editing, selecting,
 * and copying events between calendars. Event-level operations themselves
 * are handled by the individual {@link SingleCalModelInterface} instances.
 */
public interface MultiCalModelInterface {
  /**
   * Creates a new calendar with the given unique name and timezone.
   *
   * @param calName  the name of the new calendar
   * @param timezone the timezone in IANA format (ex: "Asia/Kolkata")
   * @throws IllegalArgumentException if {@code calName} or {@code timezone} are invalid
   */
  void createCalendar(String calName, ZoneId timezone) throws IllegalArgumentException;

  /**
   * Edits an existing calendar’s property. The active calendar in use should be maintained, i.e,
   * if the calendar to edit was the active calendar, it should remain active after edition.
   *
   * @param calName      the name of the calendar to edit
   * @param propertyName the property to change
   * @param newValue     the new value for the property
   * @throws IllegalArgumentException if any parameter is invalid or the edit causes a conflict
   */
  void editCalendar(String calName, String propertyName, String newValue)
      throws IllegalArgumentException;

  /**
   * Sets the current calendar context for subsequent event operations.
   *
   * @param calName the name of the calendar to make active
   * @throws IllegalArgumentException if the specified calendar does not exist
   */
  void useCalendar(String calName) throws IllegalArgumentException;

  /**
   * Returns the currently active calendar.
   *
   * @return the active {@link SingleCalModelInterface}, or {@code null} if none is active
   */
  SingleCalModelInterface getActiveCalendar();

  /**
   * Returns the names of all existing calendars.
   *
   * @return a list of calendar names
   */
  List<String> listCalendars();

  /**
   * Returns the name of the currently active calendar.
   *
   * @return the active calendar's name, or {@code null} if none is active
   */
  String getActiveCalendarName();

  /**
   * Returns the timezone of the named calendar.
   *
   * @param calName the calendar name
   * @return the calendar's timezone
   * @throws IllegalArgumentException if no calendar with that name exists
   */
  ZoneId getTimezone(String calName) throws IllegalArgumentException;

  /**
   * Returns the named calendar.
   *
   * @param calName the calendar name
   * @return the {@link SingleCalModelInterface} for that calendar
   * @throws IllegalArgumentException if no calendar with that name exists
   */
  SingleCalModelInterface getCalendar(String calName) throws IllegalArgumentException;

  /**
   * Copies a single event from the active calendar to a target calendar,
   * starting at the specified date and time.
   *
   * @param eventName       the name of the event to copy
   * @param sourceStartTime the start time of the event in the source calendar
   * @param targetCalendar  the name of the target calendar
   * @param targetStartTime the new start time (in the target calendar’s timezone)
   * @throws IllegalArgumentException if the source or target calendar does not exist,
   *                                  the event is not found, or a conflict occurs
   */
  void copyEvent(String eventName, LocalDateTime sourceStartTime, String targetCalendar,
                 LocalDateTime targetStartTime)
      throws IllegalArgumentException, IllegalStateException;

  /**
   * Copies all events within a given date range (inclusive) from the active
   * calendar to a target calendar, starting at the specified target start time.
   * Partial overlaps in recurring events should copy only the relevant instances.
   *
   * @param startDate       the start of the date interval (inclusive)
   * @param endDate         the end of the date interval (inclusive)
   * @param targetCalendar  the name of the target calendar
   * @param targetStartDate the start date in the target calendar
   * @throws IllegalArgumentException if parameters are invalid or calendars are missing
   */
  void copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalendar,
                         LocalDate targetStartDate)
      throws IllegalArgumentException, IllegalStateException;
}
