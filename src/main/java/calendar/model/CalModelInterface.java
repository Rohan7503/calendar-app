package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the model component of the calendar application.
 * This interface defines all operations related to managing events,
 * including creation, modification, retrieval, and series handling.
 * The model is responsible for maintaining an internal list of {@link Event}
 * objects and providing access to them based on queries from the controller.
 * Event series are represented as multiple {@code Event} objects
 * derived from a single prototype event.
 */
public interface CalModelInterface {

  /**
   * Adds a single event to the calendar.
   *
   * @param e the event to be added
   * @throws IllegalArgumentException if the event is null or overlaps with an existing one
   */
  void addEvent(Event e) throws IllegalArgumentException;

  /**
   * Creates a series of events that repeat on the given weekdays
   * for a specified number of occurrences. If an event in the series conflicts
   * with an existing event, the operation is aborted.
   *
   * @param prototype the base event to replicate
   * @param weekdays  a string of weekdays (e.g., "MWF")
   * @param count     the number of occurrences to generate
   * @throws IllegalArgumentException if arguments are invalid or count is non-positive
   */
  void addEventSeriesForCount(Event prototype, String weekdays, int count)
      throws IllegalArgumentException;

  /**
   * Creates a series of events that repeat on the given weekdays
   * until the specified end date. If an event in the series conflicts
   * with an existing event, the operation is aborted.
   *
   * @param prototype the base event to replicate
   * @param weekdays  a string of weekdays (e.g., "MWF")
   * @param endDate   the last date for which the series should be generated
   * @throws IllegalArgumentException if arguments are invalid or the end date is before the
   *                                  prototype's start date
   */
  void addEventSeriesUntilDate(Event prototype, String weekdays, LocalDate endDate)
      throws IllegalArgumentException;

  /**
   * Edits a single event identified by its subject and time range.
   *
   * @param property the name of the property to edit (e.g., "subject", "location", "start", "end")
   * @param subject  the subject of the event to edit
   * @param start    the start date and time of the event
   * @param end      the end date and time of the event
   * @param newValue the new value to assign to the property
   * @throws IllegalArgumentException if the event does not exist or parameters are invalid
   */
  void editEvent(String property, String subject, LocalDateTime start, LocalDateTime end,
                 String newValue) throws IllegalArgumentException;

  /**
   * Edits either all events after this event (including this) or an entire event series.
   * If this event is not part of a series, this method has the same effect as {@code editEvent()}.
   *
   * @param property        the name of the property to edit (e.g., "subject", "location",
   *                        "start", "end")
   * @param subject         the subject of the event(s) to edit
   * @param start           the start date and time of the event
   * @param newValue        the new value to assign to the property
   * @param editWholeSeries if true, applies the edit to all events in the series; otherwise, edits
   *                        this event and all events after this in the series
   * @throws IllegalArgumentException if no matching events are found or arguments are invalid
   */
  void editEvents(String property, String subject, LocalDateTime start, String newValue,
                  boolean editWholeSeries) throws IllegalArgumentException;

  /**
   * Retrieves all events that occur within the specified date-time range sorted by start date/time.
   *
   * @param start the beginning of the range (inclusive)
   * @param end   the end of the range (inclusive)
   * @return a list of events within the range; empty if none found
   * @throws IllegalArgumentException if start or end is invalid
   */
  List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end)
      throws IllegalArgumentException;

  /**
   * Returns all events currently stored in the calendar sorted by start date/time.
   *
   * @return a list of all events
   */
  List<Event> getAllEvents();

  /**
   * Checks for availability at a specific date-time.
   *
   * @param dateTime the date and time to check for availability at
   * @return "Busy", if the user is busy at the specified date-time, "Available" otherwise
   * @throws IllegalArgumentException if the given date-time is invalid
   */
  String checkAvailability(LocalDateTime dateTime) throws IllegalArgumentException;
}
