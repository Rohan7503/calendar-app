package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single calendar event.
 * An event has a subject, description, start and end times,
 * location, status, and an all-day flag. Each event may optionally belong to a series
 * (indicated by a non-null seriesId). An event belonging to a series
 * must not span more than 1 day.
 * All-day events are marked by the {@link #isAllDay()} flag rather than being inferred
 * from their start/end times.
 * Use {@code EventBuilder} to build an object of this class.
 */
public class Event {

  private final String subject;
  private final String description;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final EventLocation location;
  private final EventStatus status;
  private final String seriesId;
  private final boolean allDay;

  /**
   * Construct an event using its builder object's fields.
   *
   * @param builder The builder object
   */
  private Event(EventBuilder builder) {
    this.subject = builder.subject;
    this.description = builder.description;
    this.start = builder.start;
    this.end = builder.end;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = builder.seriesId;
    this.allDay = builder.allDay;

    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty.");
    }
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end times cannot be null.");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time cannot be before start time.");
    }
    if (seriesId != null && !seriesId.isEmpty()
        && !start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException("Event series prototype must not span more than 1 day.");
    }
  }

  /**
   * Getter method for subject.
   *
   * @return The subject of the event.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Getter method for description.
   *
   * @return The description of the event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Getter method for starting datetime.
   *
   * @return The starting datetime of the event.
   */
  public LocalDateTime getStart() {
    return start;
  }

  /**
   * Getter method for ending datetime.
   *
   * @return The ending datetime of the event.
   */
  public LocalDateTime getEnd() {
    return end;
  }

  /**
   * Getter method for location.
   *
   * @return The location of the event.
   */
  public EventLocation getLocation() {
    return location;
  }

  /**
   * Getter method for status.
   *
   * @return The status of the event - public or private.
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Getter method for seriesId.
   *
   * @return The series ID of the event - to identify which series it belongs to. Null if it does
   *         not belong to a series.
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Reports whether this event is an all-day event.
   * All-day events are a first-class concept: the flag is authoritative and is set explicitly
   * at construction time. It is not inferred from the start/end times, so an ordinary timed
   * event that happens to run from 08:00 to 17:00 is not treated as all-day.
   *
   * @return {@code true} if this event is an all-day event, {@code false} otherwise.
   */
  public boolean isAllDay() {
    return allDay;
  }

  /**
   * Returns a new builder of the Event class.
   *
   * @return An EventBuilder object
   */
  public static EventBuilder getBuilder() {
    return new EventBuilder();
  }

  /**
   * Returns a builder pre-populated with the fields of this {@code Event}.
   * This method is useful when creating a modified copy of an existing event,
   * since {@link Event} is immutable.
   * Example:
   * <pre>
   * Event updated = original.toBuilder()
   *     .location("Chennai")
   *     .build();
   * </pre>
   * The returned builder contains the same field values as this event,
   * allowing modification of just certain fields before creating
   * a new {@code Event} instance.
   *
   * @return a new {@link EventBuilder} initialized with this event’s fields
   */
  public EventBuilder toBuilder() {
    return Event.getBuilder()
        .subject(this.subject)
        .description(this.description)
        .start(this.start)
        .end(this.end)
        .location(this.location)
        .status(this.status)
        .seriesId(this.seriesId)
        .allDay(this.allDay);
  }


  /**
   * Two events are considered equal when they share the same subject, start time, and end time.
   * This is the application-wide identity rule for an event: description, location, status,
   * series membership, and the all-day flag are deliberately excluded. Duplicate detection,
   * edits, and copy operations all rely on this contract, so two events with the same
   * subject/start/end but differing metadata are treated as the same event.
   *
   * @param o the object to compare against.
   * @return {@code true} if {@code o} is an event with the same subject, start, and end.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return subject.equals(event.subject)
        && start.equals(event.start)
        && end.equals(event.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }


  /**
   * Builder for {@link Event}. Use this to construct new instances of the Event class.
   *
   * <pre>
   * Event e = Event.getBuilder()
   *     .subject("Meeting")
   *     .description("Project kickoff")
   *     .start(LocalDateTime.of(2025, 10, 22, 10, 0))
   *     .end(LocalDateTime.of(2025, 10, 22, 11, 0))
   *     .location("Conference Room A")
   *     .status("private")
   *     .build();
   * </pre>
   */
  public static class EventBuilder {
    private String subject;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventLocation location;
    private EventStatus status;
    private String seriesId;
    private boolean allDay;

    /**
     * Set the subject field of the builder.
     *
     * @param subject The value to set the subject field with
     * @return An EventBuilder object
     */
    public EventBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Set the description field of the builder.
     *
     * @param description The value to set the description field with
     * @return An EventBuilder object
     */
    public EventBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Set the start field of the builder.
     *
     * @param start The value to set the start field with
     * @return An EventBuilder object
     */
    public EventBuilder start(LocalDateTime start) {
      this.start = start;
      return this;
    }

    /**
     * Set the end field of the builder.
     *
     * @param end The value to set the end field with
     * @return An EventBuilder object
     */
    public EventBuilder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Set the location field of the builder.
     *
     * @param location The value to set the location field with
     * @return An EventBuilder object
     */
    public EventBuilder location(EventLocation location) {
      this.location = location;
      return this;
    }

    /**
     * Set the status field of the builder.
     *
     * @param status The value to set the status field with
     * @return An EventBuilder object
     */
    public EventBuilder status(EventStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Set the seriesId field of the builder.
     *
     * @param seriesId The value to set the seriesId field with
     * @return An EventBuilder object
     */
    public EventBuilder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Marks whether the event being built is an all-day event. Defaults to {@code false}.
     *
     * @param allDay {@code true} to build an all-day event
     * @return An EventBuilder object
     */
    public EventBuilder allDay(boolean allDay) {
      this.allDay = allDay;
      return this;
    }

    /**
     * Builds an {@link Event} instance.
     *
     * @return a new Event
     * @throws IllegalArgumentException if validation fails
     */
    public Event build() {
      return new Event(this);
    }
  }
}
