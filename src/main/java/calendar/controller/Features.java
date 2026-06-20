package calendar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a set of callback functions that a view uses to communicate
 * user actions back to the controller. The controller implements this
 * interface and performs all model operations.
 */
public interface Features {

  /**
   * Create a new calendar using the given timezone.
   *
   * @param name     the name of the new calendar.
   * @param timezone a valid ZoneId string.
   */
  void createCalendar(String name, String timezone);

  /**
   * Makes this calendar active.
   *
   * @param name the name of the calendar to switch to.
   */
  void selectCalendar(String name);

  /**
   * Load the events for this day from the active calendar and instruct the view to display them.
   *
   * @param date the date to get events on.
   */
  void requestEventsForDay(String date);

  /**
   * Creates a single event on the active calendar.
   *
   * @param subject the event title.
   * @param start   start date/time in the calendar's timezone.
   * @param end     end date/time in the calendar's timezone.
   */
  void createEvent(String subject, String start, String end);

  /**
   * Creates a single all-day event on the active calendar.
   *
   * @param subject the event title.
   * @param date    the date of the all-day event (YYYY-MM-DD).
   */
  void createAllDayEvent(String subject, String date);

  /**
   * Creates an event series by count.
   *
   * @param subject  event subject.
   * @param start    first occurrence start.
   * @param end      first occurrence end.
   * @param weekdays weekdays on which the event repeats.
   * @param count    number of occurrences.
   */
  void createEventSeriesByCount(String subject, String start, String end,
                                String weekdays, String count);

  /**
   * Creates an event series until a given date.
   *
   * @param subject   event subject.
   * @param start     first occurrence start.
   * @param end       first occurrence end.
   * @param weekdays  weekdays on which the event repeats.
   * @param untilDate end date for the series (inclusive).
   */
  void createEventSeriesUntilDate(String subject, String start, String end,
                                  String weekdays, String untilDate);

  /**
   * Edit a single event.
   *
   * @param property the property to edit.
   * @param subject  the subject of the event to identify it.
   * @param start    the start date/time of the event.
   * @param end      the end date/time of the event.
   * @param newValue the new value for the property.
   */
  void editEvent(String property, String subject, String start, String end,
                 String newValue);

  /**
   * Edits either all events after this event (including this) or an entire event series.
   *
   * @param property        the property to edit.
   * @param subject         the subject of the event to identify it.
   * @param start           the start date/time of the event.
   * @param newValue        the new value for the property.
   * @param editWholeSeries if true, applies the edit to all events in the series; otherwise, edits
   *                        this event and all events after this in the series.
   */
  void editEvents(String property, String subject, String start, String newValue,
                  boolean editWholeSeries);

  /**
   * Deletes a single event from the active calendar.
   *
   * @param subject the subject of the event to delete.
   * @param start   the start date/time of the event.
   * @param end     the end date/time of the event.
   */
  void deleteEvent(String subject, String start, String end);

  /**
   * Deletes one or more events from the active calendar. If the matched event is part of a
   * series, either the whole series or this event and all later events in the series are
   * deleted; otherwise only the matched event is deleted.
   *
   * @param subject           the subject of the event(s) to delete.
   * @param start             the start date/time of the matched event.
   * @param deleteWholeSeries if true, deletes the whole series; otherwise deletes this event
   *                          and all later events in the series.
   */
  void deleteEvents(String subject, String start, boolean deleteWholeSeries);

  /**
   * Edits a property of an existing calendar.
   *
   * @param name     the name of the calendar to edit.
   * @param property the property to change ("name" or "timezone").
   * @param newValue the new value for the property.
   */
  void editCalendar(String name, String property, String newValue);

  /**
   * Exports the active calendar to the given file path. The file type is determined by the
   * file extension (.csv or .ics).
   *
   * @param path the destination file path.
   */
  void exportCalendar(String path);

  /**
   * Copies all events in a date range from the active calendar to a target calendar.
   *
   * @param startDate       the first date of the source range (inclusive).
   * @param endDate         the last date of the source range (inclusive).
   * @param targetCalendar  the name of the destination calendar.
   * @param targetStartDate the date in the target calendar the range should start at.
   */
  void copyEvents(String startDate, String endDate, String targetCalendar,
                  String targetStartDate);

  /**
   * Reports whether the user is busy or available at the given date/time on the active calendar.
   *
   * @param dateTime the date/time to check.
   */
  void showStatus(String dateTime);

  /**
   * Loads the events of the active calendar in the given date/time range and asks the view to
   * present them.
   *
   * @param start the start of the range.
   * @param end   the end of the range.
   */
  void requestEventsInRange(String start, String end);

  /**
   * Loads the events of the active calendar within the given range and asks the view to render
   * them onto the month grid. Used to populate day-cell event indicators.
   *
   * @param startDate the start of the displayed month range.
   * @param endDate   the end of the displayed month range.
   */
  void requestMonthView(String startDate, String endDate);

  /**
   * Persists the current calendars and events to local storage. Best-effort; intended to be
   * called when the application is closing.
   */
  void persist();
}

