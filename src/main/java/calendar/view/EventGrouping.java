package calendar.view;

import calendar.model.Event;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Pure helpers for bucketing and ordering events for the calendar views. Keeping this logic free
 * of Swing makes it easy to unit-test and lets the month, week, and day views share it.
 */
final class EventGrouping {

  private EventGrouping() {
  }

  /**
   * Groups events by the calendar date on which they start.
   *
   * @param events the events to group
   * @return a date-keyed map whose lists are ordered by start time
   */
  static Map<LocalDate, List<Event>> byStartDate(List<Event> events) {
    Map<LocalDate, List<Event>> byDay = new TreeMap<>();
    for (Event event : ordered(events)) {
      byDay.computeIfAbsent(event.getStart().toLocalDate(), d -> new ArrayList<>()).add(event);
    }
    return byDay;
  }

  /**
   * Returns the events that start on the given day, ordered by start time.
   *
   * @param events the events to filter
   * @param day    the day of interest
   * @return the matching events, ordered by start time
   */
  static List<Event> onDay(List<Event> events, LocalDate day) {
    return ordered(events).stream()
        .filter(e -> e.getStart().toLocalDate().equals(day))
        .collect(Collectors.toList());
  }

  /**
   * Returns only the all-day events from the given list.
   *
   * @param events the events to filter
   * @return the all-day events
   */
  static List<Event> allDay(List<Event> events) {
    return events.stream().filter(Event::isAllDay).collect(Collectors.toList());
  }

  /**
   * Returns only the timed (non-all-day) events from the given list.
   *
   * @param events the events to filter
   * @return the timed events
   */
  static List<Event> timed(List<Event> events) {
    return events.stream().filter(e -> !e.isAllDay()).collect(Collectors.toList());
  }

  private static List<Event> ordered(List<Event> events) {
    return events.stream()
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }
}
