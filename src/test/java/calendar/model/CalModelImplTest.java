package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for the CalModelImpl class.
 */
public class CalModelImplTest {
  CalModelInterface model;
  Event event;

  /**
   * Instantiate the model and create an event before each test.
   */
  @Before
  public void setUp() {
    this.model = new CalModelImpl();
    this.event = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();
  }

  @Test
  public void testConstructor() {
    assertEquals(new ArrayList<Event>(), this.model.getAllEvents());
  }

  @Test
  public void testAddEvent() {
    Event event2 = Event.getBuilder()
        .subject("Work")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();
    Event event3 = Event.getBuilder()
        .subject("Play")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();
    this.model.addEvent(this.event);
    this.model.addEvent(event2);
    this.model.addEvent(event3);

    List<Event> expected = new ArrayList<>();
    expected.add(this.event);
    expected.add(event2);
    expected.add(event3);
    assertEquals(expected, this.model.getAllEvents());
  }

  @Test
  public void testAddEventNullEvent() {
    Event event = null;
    try {
      this.model.addEvent(event);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event cannot be null", e.getMessage());
    }
  }

  @Test
  public void testAddEventDuplicateEvent() {
    Event event2 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();

    this.model.addEvent(this.event);
    try {
      this.model.addEvent(event2);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event already exists", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCount() {
    String subject = "Test series event";
    LocalDateTime start = LocalDateTime.parse("2025-10-20T14:30");
    LocalDateTime end = LocalDateTime.parse("2025-10-20T15:30");
    String weekdays = "TWU";
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY);
    int count = 5;

    Event prototype = Event.getBuilder().subject(subject).start(start).end(end).build();
    this.model.addEventSeriesForCount(prototype, weekdays, count);

    List<Event> actual = this.model.getAllEvents();
    List<Event> expected = new ArrayList<>();

    assertEquals(count, actual.size());

    LocalDateTime curr = start;
    int created = 0;
    while (created < count) {
      if (created == 0) {
        expected.add(prototype);
        created++;
      } else {
        curr = curr.plusDays(1);
        if (repeatDays.contains(curr.getDayOfWeek())) {
          LocalDateTime currEnd = curr.withHour(end.getHour()).withMinute(end.getMinute());
          Event next = Event.getBuilder().subject(subject).start(curr).end(currEnd).build();
          expected.add(next);
          created++;
        }
      }
    }
    for (int i = 0; i < count; i++) {
      assertEquals(actual.get(0).getSeriesId(), actual.get(i).getSeriesId());
      assertEquals(expected.get(i).getSubject(), actual.get(i).getSubject());
      assertEquals(expected.get(i).getStart(), actual.get(i).getStart());
      assertEquals(expected.get(i).getEnd(), actual.get(i).getEnd());
    }
  }

  @Test
  public void testAddEventSeriesForCountStartOnRepeatingWeekday() {
    String subject = "Test series event";
    LocalDateTime start = LocalDateTime.parse("2025-10-20T14:30");
    LocalDateTime end = LocalDateTime.parse("2025-10-20T15:30");
    String weekdays = "MWU";
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY);
    int count = 5;

    Event prototype = Event.getBuilder().subject(subject).start(start).end(end).build();
    this.model.addEventSeriesForCount(prototype, weekdays, count);

    List<Event> actual = this.model.getAllEvents();
    List<Event> expected = new ArrayList<>();

    assertEquals(count, actual.size());

    LocalDateTime curr = start;
    int created = 0;
    while (created < count) {
      if (created == 0) {
        expected.add(prototype);
        created++;
      } else {
        curr = curr.plusDays(1);
        if (repeatDays.contains(curr.getDayOfWeek())) {
          LocalDateTime currEnd = curr.withHour(end.getHour()).withMinute(end.getMinute());
          Event next = Event.getBuilder().subject(subject).start(curr).end(currEnd).build();
          expected.add(next);
          created++;
        }
      }
    }
    for (int i = 0; i < count; i++) {
      assertEquals(actual.get(0).getSeriesId(), actual.get(i).getSeriesId());
      assertEquals(expected.get(i).getSubject(), actual.get(i).getSubject());
      assertEquals(expected.get(i).getStart(), actual.get(i).getStart());
      assertEquals(expected.get(i).getEnd(), actual.get(i).getEnd());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidCountZero() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-20T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "TWU", 0);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Count must be positive", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidCountNegative() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-20T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "TWU", -4);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Count must be positive", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidPrototypeNull() {
    try {
      this.model.addEventSeriesForCount(null, "TWU", 5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event cannot be null", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidPrototypeSpanningManyDays() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-21T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "TWU", 5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event series prototype must not span more than 1 day.", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidWeekdaysNull() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-20T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, null, 5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Weekdays cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidWeekdaysEmpty() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-20T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "", 5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Weekdays cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidWeekdays1() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-20T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "MTWX", 5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid format for weekdays. A weekday string must "
          + "only contain the following characters: M, T, W, R, F, S or U, in that order, i.e, "
          + "W cannot appear before T or F cannot appear before M.", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesForCountInvalidWeekdays2() {
    Event prototype = Event.getBuilder()
        .subject("Test series event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-20T15:30"))
        .location(EventLocation.PHYSICAL)
        .description("Repeats on Tuesdays, Wednesdays, and Sundays")
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "TFM", 5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid format for weekdays. A weekday string must "
          + "only contain the following characters: M, T, W, R, F, S or U, in that order, i.e, "
          + "W cannot appear before T or F cannot appear before M.", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesUntilDate() {
    String subject = "Test series event";
    LocalDateTime start = LocalDateTime.parse("2025-10-20T14:30");
    LocalDateTime end = LocalDateTime.parse("2025-10-20T15:30");
    String weekdays = "WR";
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);
    LocalDate until = LocalDate.parse("2025-11-13");

    Event prototype = Event.getBuilder().subject(subject).start(start).end(end).build();
    this.model.addEventSeriesUntilDate(prototype, weekdays, until);

    List<Event> actual = this.model.getAllEvents();
    List<Event> expected = new ArrayList<>();

    LocalDateTime curr = start;
    while (!curr.toLocalDate().isAfter(until)) {
      if (curr.equals(start)) {
        expected.add(prototype);
      } else if (repeatDays.contains(curr.getDayOfWeek())) {
        LocalDateTime currEnd = curr.withHour(end.getHour()).withMinute(end.getMinute());
        Event next = Event.getBuilder().subject(subject).start(curr).end(currEnd).build();
        expected.add(next);
      }
      curr = curr.plusDays(1);
    }
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(actual.get(0).getSeriesId(), actual.get(i).getSeriesId());
      assertEquals(expected.get(i).getSubject(), actual.get(i).getSubject());
      assertEquals(expected.get(i).getStart(), actual.get(i).getStart());
      assertEquals(expected.get(i).getEnd(), actual.get(i).getEnd());
    }
  }

  @Test
  public void testAddEventSeriesUntilDateStartOnRepeatingWeekday() {
    String subject = "Test series event";
    LocalDateTime start = LocalDateTime.parse("2025-10-20T14:30");
    LocalDateTime end = LocalDateTime.parse("2025-10-20T15:30");
    String weekdays = "MR";
    List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
    LocalDate until = LocalDate.parse("2025-11-13");

    Event prototype = Event.getBuilder().subject(subject).start(start).end(end).build();
    this.model.addEventSeriesUntilDate(prototype, weekdays, until);

    List<Event> actual = this.model.getAllEvents();
    List<Event> expected = new ArrayList<>();

    LocalDateTime curr = start;
    while (!curr.toLocalDate().isAfter(until)) {
      if (curr.equals(start)) {
        expected.add(prototype);
      } else if (repeatDays.contains(curr.getDayOfWeek())) {
        LocalDateTime currEnd = curr.withHour(end.getHour()).withMinute(end.getMinute());
        Event next = Event.getBuilder().subject(subject).start(curr).end(currEnd).build();
        expected.add(next);
      }
      curr = curr.plusDays(1);
    }
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(actual.get(0).getSeriesId(), actual.get(i).getSeriesId());
      assertEquals(expected.get(i).getSubject(), actual.get(i).getSubject());
      assertEquals(expected.get(i).getStart(), actual.get(i).getStart());
      assertEquals(expected.get(i).getEnd(), actual.get(i).getEnd());
    }
  }

  @Test
  public void testAddEventSeriesUntilDateInvalidDateNull() {
    String subject = "Test series event";
    LocalDateTime start = LocalDateTime.parse("2025-10-20T14:30");
    LocalDateTime end = LocalDateTime.parse("2025-10-20T15:30");
    String weekdays = "WR";
    LocalDate until = null;
    Event prototype = Event.getBuilder().subject(subject).start(start).end(end).build();
    try {
      this.model.addEventSeriesUntilDate(prototype, weekdays, until);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("End date cannot be null", e.getMessage());
    }
  }

  @Test
  public void testAddEventSeriesUntilDateInvalidDateBeforeStart() {
    String subject = "Test series event";
    LocalDateTime start = LocalDateTime.parse("2025-10-20T14:30");
    LocalDateTime end = LocalDateTime.parse("2025-10-20T15:30");
    String weekdays = "WR";
    LocalDate until = LocalDate.parse("2025-10-19");
    Event prototype = Event.getBuilder().subject(subject).start(start).end(end).build();
    try {
      this.model.addEventSeriesUntilDate(prototype, weekdays, until);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("End date cannot be before the prototype's start date", e.getMessage());
    }
  }

  @Test
  public void testEditEventDescription() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);

    model.editEvent("description", subject, start, end, "Updated description");
    Event edited = model.getAllEvents().get(0);

    assertEquals("Updated description", edited.getDescription());
    assertEquals(subject, edited.getSubject());
    assertEquals(EventLocation.PHYSICAL, edited.getLocation());
  }

  @Test
  public void testEditEventLocation() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);

    model.editEvent("location", subject, start, end, "online");
    Event edited = model.getAllEvents().get(0);

    assertEquals(EventLocation.ONLINE, edited.getLocation());
  }

  @Test
  public void testEditEventStatus() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);

    model.editEvent("status", subject, start, end, "private");
    Event edited = model.getAllEvents().get(0);

    assertEquals(EventStatus.PRIVATE, edited.getStatus());
  }

  @Test
  public void testEditEventStart() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);

    model.editEvent("start", subject, start, end, "2025-10-25T09:30");
    Event edited = model.getAllEvents().get(0);

    assertEquals(LocalDateTime.parse("2025-10-25T09:30"), edited.getStart());
    assertEquals(LocalDateTime.parse("2025-10-25T11:00"), edited.getEnd());
  }

  @Test
  public void testEditEventEnd() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);

    model.editEvent("end", subject, start, end, "2025-10-25T11:30");
    Event edited = model.getAllEvents().get(0);

    assertEquals(LocalDateTime.parse("2025-10-25T10:00"), edited.getStart());
    assertEquals(LocalDateTime.parse("2025-10-25T11:30"), edited.getEnd());
  }

  @Test
  public void testEditEventInvalidNonExistentEvent() {
    this.model.addEvent(this.event);
    String subject = "Gym";
    LocalDateTime start = LocalDateTime.parse("2020-01-01T00:00");
    LocalDateTime end = LocalDateTime.parse("2020-01-01T11:00");
    try {
      model.editEvent("start", subject, start, end, "2025-09-25T09:30");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("No matching event found.", e.getMessage());
    }
  }

  @Test
  public void testEditEventInvalidEdit() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);
    try {
      model.editEvent("start", subject, start, end, "2025-10-25T19:30");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("End time cannot be before start time.", e.getMessage());
    }
  }

  @Test
  public void testEditEventInvalidProperty() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);
    try {
      model.editEvent("starx", subject, start, end, "2025-10-25T19:30");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Unknown event property: starx", e.getMessage());
    }
  }

  @Test
  public void testEditEventInvalidStartFormat() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);
    try {
      model.editEvent("start", subject, start, end, "Chennai");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid format for start date/time.", e.getMessage());
    }
  }

  @Test
  public void testEditEventInvalidEndFormat() {
    String subject = "Meeting";
    LocalDateTime start = LocalDateTime.parse("2025-10-25T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-10-25T11:00");
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("Team sync-up")
        .location(EventLocation.PHYSICAL)
        .status(EventStatus.PUBLIC)
        .build();
    model.addEvent(event);
    try {
      model.editEvent("end", subject, start, end, "Chennai");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid format for end date/time.", e.getMessage());
    }
  }

  @Test
  public void testEditEvents() {
    Event prototype = Event.getBuilder().subject("First")
        .start(LocalDateTime.parse("2025-05-05T10:00"))
        .end(LocalDateTime.parse("2025-05-05T11:00"))
        .build();
    model.addEventSeriesForCount(prototype, "MW", 6);

    model.editEvents("subject", "First", LocalDateTime.parse("2025-05-12T10:00"),
        "Second", false);
    List<Event> actual = model.getAllEvents();
    actual.sort(Comparator.comparing(Event::getStart));
    for (int i = 0; i < actual.size(); i++) {
      if (i < 2) {
        assertEquals("First", actual.get(i).getSubject());
      } else {
        assertEquals("Second", actual.get(i).getSubject());
      }
    }

    model.editEvents("subject", "First", LocalDateTime.parse("2025-05-05T10:00"),
        "Third", true);
    actual = model.getAllEvents();
    for (Event e : actual) {
      assertEquals("Third", e.getSubject());
    }

    model.editEvents("start", "Third", LocalDateTime.parse("2025-05-12T10:00"),
        "2025-05-12T10:30", false);
    model.editEvents("subject", "Third", LocalDateTime.parse("2025-05-05T10:00"),
        "Fourth", true);
    model.editEvents("subject", "Third", LocalDateTime.parse("2025-05-12T10:30"),
        "Fifth", true);
    actual = model.getAllEvents();
    actual.sort(Comparator.comparing(Event::getStart));
    for (int i = 0; i < actual.size(); i++) {
      if (i < 2) {
        assertEquals("Fourth", actual.get(i).getSubject());
      } else {
        assertEquals("Fifth", actual.get(i).getSubject());
      }
    }
  }

  @Test
  public void testEditEventsWithEventNotPartOfSeries() {
    Event prototype = Event.getBuilder().subject("First")
        .start(LocalDateTime.parse("2025-05-05T10:00"))
        .end(LocalDateTime.parse("2025-05-05T11:00"))
        .build();
    model.addEventSeriesForCount(prototype, "MW", 6);
    model.addEvent(this.event);

    model.editEvents("subject", "Gym", LocalDateTime.parse("2020-01-01T00:00"),
        "Party", true);

    List<Event> actual = model.getAllEvents();
    for (Event e : actual) {
      if (e.getStart().equals(LocalDateTime.parse("2020-01-01T00:00"))) {
        assertEquals("Party", e.getSubject());
      } else {
        assertEquals("First", e.getSubject());
      }
    }
  }

  @Test
  public void testGetEventsInRange1() {
    Event prototype = Event.getBuilder().subject("First")
        .start(LocalDateTime.parse("2025-05-05T10:00"))
        .end(LocalDateTime.parse("2025-05-05T11:00"))
        .build();
    model.addEventSeriesForCount(prototype, "MW", 6);

    Event prototype2 = Event.getBuilder().subject("Second")
        .start(LocalDateTime.parse("2024-05-05T10:00"))
        .end(LocalDateTime.parse("2024-05-05T11:00"))
        .build();
    model.addEventSeriesForCount(prototype2, "MW", 6);

    List<Event> actual = model.getEventsInRange(LocalDateTime.parse("2025-01-01T00:00"),
        LocalDateTime.parse("2026-01-01T00:00"));
    for (Event e : actual) {
      assertEquals("First", e.getSubject());
    }

    actual = model.getEventsInRange(LocalDateTime.parse("2024-01-01T00:00"),
        LocalDateTime.parse("2025-01-01T00:00"));
    for (Event e : actual) {
      assertEquals("Second", e.getSubject());
    }
  }

  @Test
  public void testGetEventsInRange2() {
    Event prototype = Event.getBuilder().subject("First")
        .start(LocalDateTime.parse("2025-05-05T10:00"))
        .end(LocalDateTime.parse("2025-05-05T11:00"))
        .build();
    model.addEventSeriesForCount(prototype, "MW", 6);

    Event prototype2 = Event.getBuilder().subject("Second")
        .start(LocalDateTime.parse("2024-05-05T10:00"))
        .end(LocalDateTime.parse("2024-05-05T11:00"))
        .build();
    model.addEventSeriesForCount(prototype2, "MW", 6);

    List<Event> actual = model.getEventsInRange(LocalDateTime.parse("2024-01-01T00:00"),
        LocalDateTime.parse("2026-01-01T00:00"));
    actual.sort(Comparator.comparing(Event::getStart));
    for (int i = 0; i < actual.size(); i++) {
      if (i < 6) {
        assertEquals("Second", actual.get(i).getSubject());
      } else {
        assertEquals("First", actual.get(i).getSubject());
      }
    }

    actual = model.getEventsInRange(LocalDateTime.parse("2024-05-16T00:00"),
        LocalDateTime.parse("2025-05-06T00:00"));
    actual.sort(Comparator.comparing(Event::getStart));
    for (int i = 0; i < actual.size(); i++) {
      if (i < 1) {
        assertEquals("Second", actual.get(i).getSubject());
      } else {
        assertEquals("First", actual.get(i).getSubject());
      }
    }
  }

  @Test
  public void testGetEventsInRangeInvalid1() {
    try {
      model.getEventsInRange(null, null);
    }  catch (IllegalArgumentException e) {
      assertEquals("Start and end date/time cannot be null.",  e.getMessage());
    }
  }

  @Test
  public void testGetEventsInRangeInvalid2() {
    try {
      model.getEventsInRange(LocalDateTime.parse("2025-10-02T00:00"), null);
    }  catch (IllegalArgumentException e) {
      assertEquals("Start and end date/time cannot be null.",  e.getMessage());
    }
  }

  @Test
  public void testGetEventsInRangeInvalid3() {
    try {
      model.getEventsInRange(LocalDateTime.parse("2025-10-02T00:00"),
          LocalDateTime.parse("2025-10-01T00:00"));
    }  catch (IllegalArgumentException e) {
      assertEquals("Start date/time cannot be after end date/time.",  e.getMessage());
    }
  }

  @Test
  public void testCheckAvailability() {
    Event event1 = Event.getBuilder()
        .subject("Event 1")
        .start(LocalDateTime.parse("2025-10-25T10:00"))
        .end(LocalDateTime.parse("2025-10-25T11:00"))
        .build();

    Event event2 = Event.getBuilder()
        .subject("Event 2")
        .start(LocalDateTime.parse("2025-10-25T14:00"))
        .end(LocalDateTime.parse("2025-10-25T15:00"))
        .build();

    this.model.addEvent(event1);
    this.model.addEvent(event2);

    assertEquals("Busy", model.checkAvailability(LocalDateTime.parse("2025-10-25T10:30")));
    assertEquals("Busy", model.checkAvailability(LocalDateTime.parse("2025-10-25T14:00")));
    assertEquals("Available", model.checkAvailability(LocalDateTime.parse("2025-10-25T13:00")));
    assertEquals("Available", model.checkAvailability(LocalDateTime.parse("2025-10-25T15:00")));
  }

  @Test
  public void testCheckAvailabilityInvalidDateTime() {
    try {
      assertEquals("Busy", model.checkAvailability(null));
    } catch (IllegalArgumentException e) {
      assertEquals("Date/time cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testAddEventAndEventSeries() {
    Event prototype1 = Event.getBuilder()
        .subject("Event series 1")
        .start(LocalDateTime.parse("2019-12-29T19:00"))
        .end(LocalDateTime.parse("2019-12-29T20:00"))
        .build();
    Event prototype2 = Event.getBuilder()
        .subject("Event series 2")
        .start(LocalDateTime.parse("2020-01-02T13:00"))
        .end(LocalDateTime.parse("2020-01-02T13:30"))
        .build();
    this.model.addEventSeriesForCount(prototype1, "TU", 2);
    this.model.addEvent(this.event);
    this.model.addEventSeriesForCount(prototype2, "RF", 2);

    List<Event> actual = this.model.getAllEvents();
    assertEquals(5, actual.size());
    for (Event e : actual) {
      if (e.getSubject().equals("Event series 1")) {
        assertEquals(LocalTime.parse("19:00"), e.getStart().toLocalTime());
      } else if (e.getSubject().equals("Event series 2")) {
        assertEquals(LocalTime.parse("13:00"), e.getStart().toLocalTime());
      } else {
        assertEquals("Gym", e.getSubject());
      }
    }
  }

  @Test
  public void testAddEventAndEventSeriesForCountInvalidColliding() {
    this.model.addEvent(this.event);
    Event prototype = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2019-12-29T00:00"))
        .end(LocalDateTime.parse("2019-12-29T01:00"))
        .build();
    try {
      this.model.addEventSeriesForCount(prototype, "MTWRFSU", 7);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("An event in the series already exists.", e.getMessage());
    }
  }

  @Test
  public void testAddEventAndEventSeriesByDateInvalidColliding() {
    this.model.addEvent(this.event);
    Event prototype = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2019-12-29T00:00"))
        .end(LocalDateTime.parse("2019-12-29T01:00"))
        .build();
    try {
      this.model.addEventSeriesUntilDate(prototype, "MTWRFSU", LocalDate.parse("2020-01-05"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("An event in the series already exists.", e.getMessage());
    }
  }

  @Test
  public void testEditEventWithEventsAndSeriesInCalendar() {
    Event prototype = Event.getBuilder()
        .subject("Series")
        .start(LocalDateTime.parse("2025-10-25T13:00"))
        .end(LocalDateTime.parse("2025-10-25T14:00"))
        .build();
    Event event1 = Event.getBuilder()
        .subject("Event 1")
        .start(LocalDateTime.parse("2025-10-25T13:00"))
        .end(LocalDateTime.parse("2025-10-25T14:00"))
        .build();

    this.model.addEventSeriesForCount(prototype, "SU", 2);
    this.model.addEvent(event1);

    try {
      this.model.editEvent("subject", "Event 1", LocalDateTime.parse("2025-10-25T13:00"),
          LocalDateTime.parse("2025-10-25T14:00"), "Series");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Duplicate event found. Edit aborted.", e.getMessage());
    }
    this.model.editEvent("subject", "Event 1", LocalDateTime.parse("2025-10-25T13:00"),
        LocalDateTime.parse("2025-10-25T14:00"), "First event");
    this.model.editEvents("subject", "Series", LocalDateTime.parse("2025-10-25T13:00"),
        "Event series", true);
    List<Event> actual = this.model.getAllEvents();
    for (Event e : actual) {
      if (e.getSeriesId() != null) {
        assertEquals("Event series", e.getSubject());
      } else {
        assertEquals("First event", e.getSubject());
      }
    }
  }

  @Test
  public void testEditEventsWithAmbiguousEvents() {
    Event prototype = Event.getBuilder()
        .subject("Event")
        .start(LocalDateTime.parse("2025-10-25T13:00"))
        .end(LocalDateTime.parse("2025-10-25T14:00"))
        .build();
    Event event1 = Event.getBuilder()
        .subject("Event")
        .start(LocalDateTime.parse("2025-10-25T13:00"))
        .end(LocalDateTime.parse("2025-10-25T13:30"))
        .build();

    this.model.addEventSeriesForCount(prototype, "SU", 2);
    this.model.addEvent(event1);
    try {
      this.model.editEvents("subject", "Event", LocalDateTime.parse("2025-10-25T13:00"),
          "New event", true);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Multiple matching events found.", e.getMessage());
    }
  }
}