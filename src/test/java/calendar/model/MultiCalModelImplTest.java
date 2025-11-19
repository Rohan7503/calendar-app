package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for the MultiCalModelImpl class.
 */
public class MultiCalModelImplTest {
  MultiCalModelInterface multiCalModel;
  SingleCalModelInterface calendar;
  Event event;
  Event prototype;

  /**
   * Instantiate the model and the calendar and create an event and an event series prototype
   * before each test.
   */
  @Before
  public void setUp() {
    this.multiCalModel = new MultiCalModelImpl();
    this.calendar = new SingleCalModelImpl(ZoneId.of("America/New_York"));
    this.event = Event.getBuilder()
        .subject("Test Event")
        .start(LocalDateTime.parse("2025-11-04T11:00"))
        .end(LocalDateTime.parse("2025-11-04T12:00"))
        .description("Test Event Description")
        .build();
    this.prototype = Event.getBuilder()
        .subject("Test Event Series")
        .start(LocalDateTime.parse("2025-11-03T09:00"))
        .end(LocalDateTime.parse("2025-11-03T10:00"))
        .description("Test Event Series Description")
        .build();
  }

  @Test
  public void testConstructor() {
    List<String> calendarNames = multiCalModel.listCalendars();
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    assertEquals(new ArrayList<String>(), calendarNames);
    assertNull(cal);
  }

  @Test
  public void testCreateCalendar() {
    multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("Test Calendar 2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.createCalendar("Test Calendar 3", ZoneId.of("Asia/Kolkata"));
    List<String> calendarNames = multiCalModel.listCalendars();
    assertTrue(calendarNames.contains("Test Calendar 1"));
    assertTrue(calendarNames.contains("Test Calendar 2"));
    assertTrue(calendarNames.contains("Test Calendar 3"));
  }

  @Test
  public void testCreateCalendarInvalidDuplicateName() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.createCalendar("Test Calendar 2", ZoneId.of("America/Los_Angeles"));
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("Asia/Kolkata"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("A calendar with the same name already exists.", e.getMessage());
    }
  }

  @Test
  public void testCreateCalendarInvalidEmptyName() {
    try {
      multiCalModel.createCalendar("", ZoneId.of("America/New_York"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar name cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testCreateCalendarInvalidNullName() {
    try {
      multiCalModel.createCalendar(null, ZoneId.of("America/New_York"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar name cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testCreateCalendarInvalidNullTimezone() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Timezone cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarName() {
    multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("Test Calendar 2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.editCalendar("Test Calendar 1", "name", "Cal1");
    List<String> calendarNames = multiCalModel.listCalendars();
    assertFalse(calendarNames.contains("Test Calendar 1"));
    assertTrue(calendarNames.contains("Test Calendar 2"));
    assertTrue(calendarNames.contains("Cal1"));
  }

  @Test
  public void testEditTimezone() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));

    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    cal.addEvent(event);

    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();
    cal.addEvent(event);

    multiCalModel.editCalendar("C1", "timezone", "America/Los_Angeles");
    List<Event> events = cal.getAllEvents();
    assertEquals("2025-11-04T11:00", events.get(0).getStart().toString());

    multiCalModel.useCalendar("C1");
    cal = multiCalModel.getActiveCalendar();
    events = cal.getAllEvents();
    assertEquals("2025-11-04T08:00", events.get(0).getStart().toString());
  }

  @Test
  public void testEditActiveCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));

    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    multiCalModel.editCalendar("C1", "name", "Cal1");
    assertEquals(cal, multiCalModel.getActiveCalendar());
  }

  @Test
  public void testEditCalendarInvalidProperty() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("Test Calendar 1", "location", "Cal1");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid property.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidCalendarNameNull() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar(null, "location", "Cal1");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar name cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidCalendarNameEmpty() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("", "location", "Cal1");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar name cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidCalendarNameDoesNotExist() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("C1", "location", "Cal1");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar with this name does not exist.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidPropertyNull() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("Test Calendar 1", null, "Cal1");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Property name cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidPropertyEmpty() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("Test Calendar 1", "", "Cal1");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Property name cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidNewValueNull() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("Test Calendar 1", "name", null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("New value cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarInvalidNewValueEmpty() {
    try {
      multiCalModel.createCalendar("Test Calendar 1", ZoneId.of("America/New_York"));
      multiCalModel.editCalendar("Test Calendar 1", "name", "");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("New value cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarNameInvalidDuplicate() {
    try {
      multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
      multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
      multiCalModel.editCalendar("C1", "name", "C2");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("A calendar with this name already exists.", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarTimezoneInvalidFormat() {
    try {
      multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
      multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
      multiCalModel.editCalendar("C1", "timezone", "PDT");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid or unsupported timezone.", e.getMessage());
    }
  }

  @Test
  public void testUseCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));

    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    cal.addEvent(event);

    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();
    cal.addEventSeriesForCount(prototype, "MTW", 3);

    List<Event> events;

    multiCalModel.useCalendar("C1");
    cal = multiCalModel.getActiveCalendar();
    events = cal.getAllEvents();
    assertEquals(1, events.size());

    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();
    events = cal.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testUseCalendarInvalidCalendarDoesNotExist() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    try {
      multiCalModel.useCalendar("C2");
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar with this name does not exist.", e.getMessage());
    }
  }

  @Test
  public void testCopyEvent() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    cal.addEvent(event);
    multiCalModel.copyEvent("Test Event", LocalDateTime.parse("2025-11-04T11:00"),
        "C2", LocalDateTime.parse("2025-11-06T09:00"));
    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();

    Event actual = cal.getAllEvents().get(0);
    assertEquals("Test Event", actual.getSubject());
    assertEquals("2025-11-06T09:00", actual.getStart().toString());
    assertEquals("2025-11-06T10:00", actual.getEnd().toString());
    assertEquals("Test Event Description", actual.getDescription());
  }

  @Test
  public void testCopyEventSameCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    cal.addEvent(event);
    multiCalModel.copyEvent("Test Event", LocalDateTime.parse("2025-11-04T11:00"),
        "C1", LocalDateTime.parse("2025-11-06T09:00"));

    List<Event> actual = cal.getAllEvents();
    assertEquals("Test Event", actual.get(0).getSubject());
    assertEquals("2025-11-04T11:00", actual.get(0).getStart().toString());
    assertEquals("2025-11-04T12:00", actual.get(0).getEnd().toString());
    assertEquals("Test Event Description", actual.get(0).getDescription());

    assertEquals("Test Event", actual.get(1).getSubject());
    assertEquals("2025-11-06T09:00", actual.get(1).getStart().toString());
    assertEquals("2025-11-06T10:00", actual.get(1).getEnd().toString());
    assertEquals("Test Event Description", actual.get(1).getDescription());
  }

  @Test
  public void testCopyEventInvalidDuplicateEventInNewCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));

    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    cal.addEvent(event);
    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();
    cal.addEvent(event);
    try {
      multiCalModel.copyEvent("Test Event", LocalDateTime.parse("2025-11-04T11:00"),
          "C2", LocalDateTime.parse("2025-11-04T11:00"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event already exists", e.getMessage());
    }

  }

  @Test
  public void testCopyEventInvalidNoActiveCalendar() {
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    try {
      multiCalModel.copyEvent("Test Event", LocalDateTime.parse("2025-11-04T11:00"),
          "C2", LocalDateTime.parse("2025-11-06T09:00"));
      fail();
    } catch (IllegalStateException e) {
      assertEquals("No active calendar selected.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventInvalidMultipleEventsSameSubjectStartTime() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    Event event2 = Event.getBuilder()
        .subject("Test Event")
        .start(LocalDateTime.parse("2025-11-04T11:00"))
        .end(LocalDateTime.parse("2025-11-04T13:00"))
        .build();
    cal.addEvent(event);
    cal.addEvent(event2);
    try {
      multiCalModel.copyEvent("Test Event", LocalDateTime.parse("2025-11-04T11:00"),
          "C2", LocalDateTime.parse("2025-11-06T09:00"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Multiple matching events found.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsSameDay() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    Event event2 = Event.getBuilder()
        .subject("Test Event 2")
        .start(LocalDateTime.parse("2025-11-04T14:00"))
        .end(LocalDateTime.parse("2025-11-04T15:00"))
        .build();
    cal.addEvent(event);
    cal.addEvent(event2);
    cal.addEventSeriesForCount(prototype, "MTW", 3);
    multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), LocalDate.parse("2025-11-04"),
        "C2", LocalDate.parse("2025-11-06"));
    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();

    List<Event> actual = cal.getAllEvents();
    assertEquals(3,  actual.size());
    for (Event e : actual) {
      if (e.getSubject().equals("Test Event")) {
        assertEquals("2025-11-06T08:00", e.getStart().toString());
        assertEquals("2025-11-06T09:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event 2")) {
        assertEquals("2025-11-06T11:00", e.getStart().toString());
        assertEquals("2025-11-06T12:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event Series")) {
        assertEquals("2025-11-06T06:00", e.getStart().toString());
        assertEquals("2025-11-06T07:00", e.getEnd().toString());
      }
    }
  }

  @Test
  public void testCopyEventsRangeOfDays() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    Event event2 = Event.getBuilder()
        .subject("Test Event 2")
        .start(LocalDateTime.parse("2025-11-04T14:00"))
        .end(LocalDateTime.parse("2025-11-04T15:00"))
        .build();
    cal.addEvent(event);
    cal.addEvent(event2);
    cal.addEventSeriesForCount(prototype, "MTW", 3);
    multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-03"), LocalDate.parse("2025-11-05"),
        "C2", LocalDate.parse("2025-11-06"));
    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();

    List<Event> actual = cal.getAllEvents();
    assertEquals(5, actual.size());
    for (Event e : actual) {
      if (e.getSubject().equals("Test Event")) {
        assertEquals("2025-11-07T08:00", e.getStart().toString());
        assertEquals("2025-11-07T09:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event 2")) {
        assertEquals("2025-11-07T11:00", e.getStart().toString());
        assertEquals("2025-11-07T12:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event Series")) {
        assertEquals("06:00", e.getStart().toLocalTime().toString());
        assertEquals("07:00", e.getEnd().toLocalTime().toString());
      }
    }
  }

  @Test
  public void testCopyEventsRangeOfDaysSameCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    Event event2 = Event.getBuilder()
        .subject("Test Event 2")
        .start(LocalDateTime.parse("2025-11-04T14:00"))
        .end(LocalDateTime.parse("2025-11-04T15:00"))
        .build();
    cal.addEvent(event);
    cal.addEvent(event2);
    cal.addEventSeriesForCount(prototype, "MTW", 3);
    multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-03"), LocalDate.parse("2025-11-05"),
        "C1", LocalDate.parse("2025-11-06"));

    List<Event> actual = cal.getAllEvents();
    assertEquals(10, actual.size());
    List<Event> actualSublist = actual.subList(5, actual.size());
    for (Event e : actualSublist) {
      if (e.getSubject().equals("Test Event")) {
        assertEquals("2025-11-07T11:00", e.getStart().toString());
        assertEquals("2025-11-07T12:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event 2")) {
        assertEquals("2025-11-07T14:00", e.getStart().toString());
        assertEquals("2025-11-07T15:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event Series")) {
        assertEquals("09:00", e.getStart().toLocalTime().toString());
        assertEquals("10:00", e.getEnd().toLocalTime().toString());
      }
    }
  }

  @Test
  public void testCopyEventsRangeOfDaysWithEventSeriesPartialOverlap() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    Event event2 = Event.getBuilder()
        .subject("Test Event 2")
        .start(LocalDateTime.parse("2025-11-04T14:00"))
        .end(LocalDateTime.parse("2025-11-04T15:00"))
        .build();
    cal.addEvent(event);
    cal.addEvent(event2);
    cal.addEventSeriesForCount(prototype, "MTW", 3);
    multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), LocalDate.parse("2025-11-05"),
        "C2", LocalDate.parse("2025-11-06"));
    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();

    List<Event> actual = cal.getAllEvents();
    assertEquals(4, actual.size());
    for (Event e : actual) {
      if (e.getSubject().equals("Test Event")) {
        assertEquals("2025-11-06T08:00", e.getStart().toString());
        assertEquals("2025-11-06T09:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event 2")) {
        assertEquals("2025-11-06T11:00", e.getStart().toString());
        assertEquals("2025-11-06T12:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event Series")) {
        assertEquals("06:00", e.getStart().toLocalTime().toString());
        assertEquals("07:00", e.getEnd().toLocalTime().toString());
      }
    }
  }

  @Test
  public void testCopyEventsRangeOfDaysWithEventsAlreadyInTargetCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    cal.addEventSeriesForCount(prototype, "MTW", 3);

    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();
    Event event2 = Event.getBuilder()
        .subject("Test Event 2")
        .start(LocalDateTime.parse("2025-11-08T14:00"))
        .end(LocalDateTime.parse("2025-11-08T15:00"))
        .build();
    cal.addEvent(event2);

    multiCalModel.useCalendar("C1");
    multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), LocalDate.parse("2025-11-05"),
        "C2", LocalDate.parse("2025-11-06"));
    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();

    List<Event> actual = cal.getAllEvents();
    assertEquals(3, actual.size());
    for (Event e : actual) {
      if (e.getSubject().equals("Test Event 2")) {
        assertEquals("2025-11-08T14:00", e.getStart().toString());
        assertEquals("2025-11-08T15:00", e.getEnd().toString());
      } else if (e.getSubject().equals("Test Event Series")) {
        assertEquals("06:00", e.getStart().toLocalTime().toString());
        assertEquals("07:00", e.getEnd().toLocalTime().toString());
      }
    }
  }

  @Test
  public void testCopyEventsInvalidEventSeriesTimeChangesToSpanMultipleDays() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("Asia/Tokyo"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();

    cal.addEventSeriesForCount(prototype, "MTW", 3);
    try {
      multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-03"), LocalDate.parse("2025-11-04"),
          "C2", LocalDate.parse("2025-11-06"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event series prototype must not span more than 1 day.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsRangeOfDaysInvalidDuplicateEventInNewCalendar() {
    multiCalModel.createCalendar("C1", ZoneId.of("America/New_York"));
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C1");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    cal.addEventSeriesForCount(prototype, "MTW", 3);

    multiCalModel.useCalendar("C2");
    cal = multiCalModel.getActiveCalendar();
    Event event2 = Event.getBuilder()
        .subject("Test Event Series")
        .start(LocalDateTime.parse("2025-11-07T06:00"))
        .end(LocalDateTime.parse("2025-11-07T07:00"))
        .description("Just an event, not a series. Simulating duplicate event collision.")
        .build();
    cal.addEvent(event2);

    multiCalModel.useCalendar("C1");
    try {
      multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-03"), LocalDate.parse("2025-11-05"),
          "C2", LocalDate.parse("2025-11-06"));
    } catch (IllegalArgumentException e) {
      assertEquals("Duplicate event in target calendar. Copy aborted.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsInvalidNoActiveCalendar() {
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    try {
      multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), LocalDate.parse("2025-11-04"),
          "C2", LocalDate.parse("2025-11-06"));
      fail();
    } catch (IllegalStateException e) {
      assertEquals("No active calendar selected.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsInvalidNullStartDate() {
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C2");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    try {
      multiCalModel.copyEventsBetween(null, LocalDate.parse("2025-11-04"), "C2",
          LocalDate.parse("2025-11-06"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Start date and end date cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsInvalidNullEndDate() {
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C2");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    try {
      multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), null, "C2",
          LocalDate.parse("2025-11-06"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Start date and end date cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsInvalidNullTargetStartDate() {
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C2");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    try {
      multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), LocalDate.parse("2025-11-04"),
          "C2", null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Target start date cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testCopyEventsInvalidEndDateBeforeStartDate() {
    multiCalModel.createCalendar("C2", ZoneId.of("America/Los_Angeles"));
    multiCalModel.useCalendar("C2");
    SingleCalModelInterface cal = multiCalModel.getActiveCalendar();
    try {
      multiCalModel.copyEventsBetween(LocalDate.parse("2025-11-04"), LocalDate.parse("2025-11-03"),
          "C2", LocalDate.parse("2025-11-06"));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("End date cannot be before start date.", e.getMessage());
    }
  }

}