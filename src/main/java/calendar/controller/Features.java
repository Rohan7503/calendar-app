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
}

