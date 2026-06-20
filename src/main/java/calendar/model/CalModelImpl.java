package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link CalModelInterface}.
 * Manages the storage, retrieval, and modification of Event objects.
 * Handles adding individual events, creating event series, editing events,
 * and retrieving events based on queries.
 */
public class CalModelImpl implements CalModelInterface {

  private final List<Event> events;

  /**
   * Initialise the model by creating an empty array list to store the calendar events.
   */
  public CalModelImpl() {
    this.events = new ArrayList<>();
  }

  @Override
  public void addEvent(Event e) throws IllegalArgumentException {
    if (e == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    for (Event event : this.events) {
      if (e.equals(event)) {
        throw new IllegalArgumentException("Event already exists");
      }
    }
    this.events.add(e);
  }

  @Override
  public void addEventSeriesForCount(Event prototype, String weekdays, int count)
      throws IllegalArgumentException {
    if (prototype == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be positive");
    }
    List<DayOfWeek> daysOfWeek = parseWeekdays(weekdays);
    List<Event> series = generateEventSeriesByCount(prototype, daysOfWeek, count);

    if (checkForConflictingEvents(series)) {
      throw new IllegalArgumentException("An event in the series already exists.");
    }
    for (Event e : series) {
      addEvent(e);
    }
  }

  @Override
  public void addEventSeriesUntilDate(Event prototype, String weekdays, LocalDate endDate)
      throws IllegalArgumentException {
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }
    if (endDate.isBefore(prototype.getStart().toLocalDate())) {
      throw new IllegalArgumentException("End date cannot be before the prototype's start date");
    }
    List<DayOfWeek> daysOfWeek = parseWeekdays(weekdays);
    List<Event> series = generateEventSeriesUntilDate(prototype, daysOfWeek, endDate);

    if (checkForConflictingEvents(series)) {
      throw new IllegalArgumentException("An event in the series already exists.");
    }
    for (Event e : series) {
      addEvent(e);
    }
  }

  @Override
  public void editEvent(String property, String subject, LocalDateTime start, LocalDateTime end,
                        String newValue) throws IllegalArgumentException {
    Event target = findUniqueEvent(subject, start, end);
    Event updated = applyEdit(target, property, newValue);
    this.events.remove(target);
    this.events.add(updated);
  }

  @Override
  public void editEvents(String property, String subject, LocalDateTime start, String newValue,
                         boolean editWholeSeries) throws IllegalArgumentException {
    Event base = findUniqueEvent(subject, start, null);
    if (base.getSeriesId() == null) {
      Event updated = applyEdit(base, property, newValue);
      this.events.remove(base);
      this.events.add(updated);
      return;
    }

    boolean changesTime = property.equalsIgnoreCase("start");
    String newSeriesId = changesTime ? UUID.randomUUID().toString() : base.getSeriesId();

    List<Event> toEdit = getSeriesEventsToEdit(base, editWholeSeries);
    List<Event> updatedEvents = new ArrayList<>();
    for (Event e : toEdit) {
      Event updated = applyEdit(e, property, newValue)
          .toBuilder()
          .seriesId(newSeriesId)
          .build();
      updatedEvents.add(updated);
    }
    this.events.removeAll(toEdit);
    this.events.addAll(updatedEvents);

  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end date/time cannot be null.");
    }
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start date/time cannot be after end date/time.");
    }
    return this.events.stream()
        .filter(e -> e.getStart().isBefore(end) && e.getEnd().isAfter(start))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(this.events.stream()
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList()));
  }

  /**
   * Atomically replaces all events in this calendar with the given list.
   * The replacement is validated first: if the new list would contain two events that share
   * the same identity (subject, start, end), the operation is aborted and the calendar is left
   * unchanged. This makes bulk rewrites such as timezone conversion transactional — either every
   * event is replaced or none is.
   *
   * @param newEvents the events to replace the current contents with
   * @throws IllegalArgumentException if the new list contains duplicate event identities
   */
  protected void replaceAllEvents(List<Event> newEvents) throws IllegalArgumentException {
    for (int i = 0; i < newEvents.size(); i++) {
      for (int j = i + 1; j < newEvents.size(); j++) {
        if (newEvents.get(i).equals(newEvents.get(j))) {
          throw new IllegalArgumentException(
              "Operation would create duplicate events; aborted.");
        }
      }
    }
    this.events.clear();
    this.events.addAll(newEvents);
  }

  @Override
  public String checkAvailability(LocalDateTime dateTime) throws IllegalArgumentException {
    if (dateTime == null) {
      throw new IllegalArgumentException("Date/time cannot be null.");
    }
    List<Event> overlapping = getEventsInRange(dateTime, dateTime.plusSeconds(1));

    return overlapping.isEmpty() ? "Available" : "Busy";
  }


  /**
   * Helper method to check any event in the given list of events conflicts with an existing event.
   *
   * @param eventsList The list of events to check for conflicts
   * @return True if there are any conflicting events, false otherwise
   */
  private boolean checkForConflictingEvents(List<Event> eventsList) {
    return eventsList.stream().anyMatch(this.events::contains);
  }

  /**
   * Helper method to parse a weekday string and convert it into a list of DayOfWeek objects.
   * A weekday string must only contain the following characters: M, T, W, R, F, S or U, in that
   * order, i.e, W cannot appear before T or F cannot appear before M.
   *
   * @param weekdays The string to validate
   * @throws IllegalArgumentException if the string is invalid
   */
  private List<DayOfWeek> parseWeekdays(String weekdays) throws IllegalArgumentException {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty.");
    }
    if (!weekdays.toUpperCase().matches("M?T?W?R?F?S?U?")) {
      throw new IllegalArgumentException("Invalid format for weekdays. A weekday string must "
          + "only contain the following characters: M, T, W, R, F, S or U, in that order, i.e, "
          + "W cannot appear before T or F cannot appear before M.");
    }
    List<DayOfWeek> days = new ArrayList<>();
    for (char c : weekdays.toUpperCase().toCharArray()) {
      if (c == 'M') {
        days.add(DayOfWeek.MONDAY);
      } else if (c == 'T') {
        days.add(DayOfWeek.TUESDAY);
      } else if (c == 'W') {
        days.add(DayOfWeek.WEDNESDAY);
      } else if (c == 'R') {
        days.add(DayOfWeek.THURSDAY);
      } else if (c == 'F') {
        days.add(DayOfWeek.FRIDAY);
      } else if (c == 'S') {
        days.add(DayOfWeek.SATURDAY);
      } else if (c == 'U') {
        days.add(DayOfWeek.SUNDAY);
      }
    }
    return days;
  }


  /**
   * Generates a list of events based on the given prototype, repeating on the specified weekdays.
   * The first event matches the prototype's start date, and subsequent events follow
   * the order of days in {@code daysOfWeek} until {@code count} events are created.
   *
   * @param prototype  the base event to replicate
   * @param daysOfWeek the weekdays on which events should occur
   * @param count      the number of events to generate
   * @return a list of {@link Event} objects representing the generated series
   */
  private List<Event> generateEventSeriesByCount(Event prototype, List<DayOfWeek> daysOfWeek,
                                                 int count) {
    List<Event> series = new ArrayList<>();

    Event first = prototype.toBuilder()
        .seriesId(UUID.randomUUID().toString())
        .build();
    series.add(first);
    int added = 1;

    LocalDateTime current = prototype.getStart().plusDays(1);
    int endHour = prototype.getEnd().getHour();
    int endMinute = prototype.getEnd().getMinute();
    while (added < count) {
      if (daysOfWeek.contains(current.getDayOfWeek())) {
        Event e = prototype.toBuilder()
            .start(current)
            .end(current.withHour(endHour).withMinute(endMinute))
            .seriesId(first.getSeriesId())
            .build();
        series.add(e);
        added++;
      }
      current = current.plusDays(1);
    }
    return series;
  }


  /**
   * Generates a list of events starting from the prototype and repeating on specified weekdays
   * until a given end date (inclusive).
   *
   * @param prototype  The base event to replicate
   * @param daysOfWeek List of weekdays on which the event should occur
   * @param endDate    The last date for which the series should be generated
   * @return List of Event objects
   */
  private List<Event> generateEventSeriesUntilDate(Event prototype, List<DayOfWeek> daysOfWeek,
                                                   LocalDate endDate) {
    List<Event> series = new ArrayList<>();
    String seriesId = UUID.randomUUID().toString();

    Event first = prototype.toBuilder()
        .seriesId(seriesId)
        .build();
    series.add(first);

    LocalDateTime current = prototype.getStart().plusDays(1);
    int endHour = prototype.getEnd().getHour();
    int endMinute = prototype.getEnd().getMinute();
    while (!current.toLocalDate().isAfter(endDate)) {
      if (daysOfWeek.contains(current.getDayOfWeek())) {
        Event e = prototype.toBuilder()
            .start(current)
            .end(current.withHour(endHour).withMinute(endMinute))
            .seriesId(seriesId)
            .build();
        series.add(e);
      }
      current = current.plusDays(1);
    }
    return series;
  }

  /**
   * Finds a unique event that matches the given subject, start, and optionally end time.
   * Throws an exception if no event or multiple events match.
   *
   * @param subject the subject of the event
   * @param start   the start date/time of the event
   * @param end     the end date/time of the event, may be null
   * @return the uniquely matched event
   * @throws IllegalArgumentException if no event or multiple events match
   */
  protected Event findUniqueEvent(String subject, LocalDateTime start, LocalDateTime end)
      throws IllegalArgumentException {
    List<Event> matches = new ArrayList<>();

    for (Event e : this.events) {
      boolean match = e.getSubject().equals(subject) && e.getStart().equals(start);
      if (end != null) {
        match = match && e.getEnd().equals(end);
      }
      if (match) {
        matches.add(e);
      }
    }
    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No matching event found.");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple matching events found.");
    }
    return matches.get(0);
  }

  /**
   * Retrieves all events in the same series as the given base event, optionally filtering
   * to include only those events that occur at or after the base event.
   * If the given event is not part of a series, a singleton list containing only the base
   * event is returned.
   *
   * @param base            the reference event whose seriesId determines which events to retrieve
   * @param editWholeSeries true to include all events in the series;
   *                        false to include only those events that occur
   *                        at or after the base event
   * @return a list of events to be edited, sorted in ascending order by start time
   */
  private List<Event> getSeriesEventsToEdit(Event base, boolean editWholeSeries) {
    String seriesId = base.getSeriesId();
    if (seriesId == null) {
      return List.of(base);
    }
    List<Event> seriesEvents = this.events.stream()
        .filter(e -> seriesId.equals(e.getSeriesId()))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());

    return editWholeSeries
        ? seriesEvents
        : seriesEvents.stream()
        .filter(e -> !e.getStart().isBefore(base.getStart()))
        .collect(Collectors.toList());
  }


  /**
   * Applies a single property change to an event. If the event is part a series, ensures that the
   * modified event satisfies a series event's criteria.
   *
   * @param e        The event whose property is to be edited
   * @param property The property to be edited
   * @param newValue The new value of the property to be edited
   * @return A new event with the single property modified
   * @throws IllegalArgumentException If the property name or newValue data type is invalid
   */
  private Event applyEdit(Event e, String property, String newValue)
      throws IllegalArgumentException {
    Event.EventBuilder builder = e.toBuilder();
    EventProperty parsedProperty = EventProperty.fromString(property);
    switch (parsedProperty) {
      case SUBJECT:
        builder.subject(newValue);
        break;
      case DESCRIPTION:
        builder.description(newValue);
        break;
      case LOCATION:
        builder.location(EventLocation.fromString(newValue));
        break;
      case STATUS:
        builder.status(EventStatus.fromString(newValue));
        break;
      case START:
        try {
          LocalDateTime parsedValue = LocalDateTime.parse(newValue);
          builder.start(e.getStart()
              .withHour(parsedValue.getHour()).withMinute(parsedValue.getMinute()));
        } catch (DateTimeParseException ex) {
          throw new IllegalArgumentException("Invalid format for start date/time.");
        }
        break;
      case END:
        try {
          LocalDateTime parsedValue = LocalDateTime.parse(newValue);
          builder.end(e.getEnd()
              .withHour(parsedValue.getHour()).withMinute(parsedValue.getMinute()));
        } catch (DateTimeParseException ex) {
          throw new IllegalArgumentException("Invalid format for end date/time.");
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid event property.");
    }
    Event editedEvent = builder.build();
    if (parsedProperty == EventProperty.SUBJECT || parsedProperty == EventProperty.START
        || parsedProperty == EventProperty.END) {
      for (Event event : this.events) {
        if (event.equals(editedEvent)) {
          throw new IllegalArgumentException("Duplicate event found. Edit aborted.");
        }
      }
    }
    return editedEvent;
  }

}
