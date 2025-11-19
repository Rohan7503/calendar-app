package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for the SingleCalModelImpl class.
 */
public class SingleCalModelImplTest {
  SingleCalModelInterface calendar;
  Event event;
  Event prototype;

  /**
   * Instantiate the calendar and create an event and an event series prototype before each test.
   */
  @Before
  public void setUp() {
    this.calendar = new SingleCalModelImpl(ZoneId.of("America/New_York"));
    this.event = Event.getBuilder()
        .subject("Test Event")
        .start(LocalDateTime.parse("2025-11-04T11:00"))
        .end(LocalDateTime.parse("2025-11-04T12:00"))
        .build();
    this.prototype = Event.getBuilder()
        .subject("Test Event Series")
        .start(LocalDateTime.parse("2025-11-03T09:00"))
        .end(LocalDateTime.parse("2025-11-03T10:00"))
        .build();
  }

  @Test
  public void testInvalidConstructor() {
    try {
      new SingleCalModelImpl(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Timezone cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testFindEvent() {
    this.calendar.addEvent(this.event);
    Event foundEvent =
        this.calendar.findEvent("Test Event", LocalDateTime.parse("2025-11-04T11:00"),
            LocalDateTime.parse("2025-11-04T12:00"));
    assertEquals(this.event, foundEvent);
  }

  @Test
  public void testGetTimezone() {
    ZoneId tz = this.calendar.getTimezone();
    assertEquals(ZoneId.of("America/New_York"), tz);
  }

  @Test
  public void testSetTimezone() {
    calendar.addEvent(event);
    calendar.addEventSeriesForCount(prototype, "MWF", 5);
    calendar.setTimezone(ZoneId.of("America/Los_Angeles"));
    List<Event> events = calendar.getAllEvents();
    for (Event e : events) {
      if (e.getSubject().contains("Series")) {
        assertEquals(LocalTime.parse("06:00"), e.getStart().toLocalTime());
        assertEquals(LocalTime.parse("07:00"), e.getEnd().toLocalTime());
      } else {
        assertEquals(LocalDateTime.parse("2025-11-04T08:00"), e.getStart());
        assertEquals(LocalDateTime.parse("2025-11-04T09:00"), e.getEnd());
      }
    }
  }

  @Test
  public void testSetTimezoneInvalidNull() {
    try {
      calendar.setTimezone(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Timezone cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testSetTimezoneToSameTimezone() {
    calendar.addEvent(event);
    calendar.addEventSeriesForCount(prototype, "MWF", 5);
    calendar.setTimezone(ZoneId.of("America/New_York"));
    List<Event> events = calendar.getAllEvents();
    for (Event e : events) {
      if (e.getSubject().contains("Series")) {
        assertEquals(LocalTime.parse("09:00"), e.getStart().toLocalTime());
        assertEquals(LocalTime.parse("10:00"), e.getEnd().toLocalTime());
      } else {
        assertEquals(LocalDateTime.parse("2025-11-04T11:00"), e.getStart());
        assertEquals(LocalDateTime.parse("2025-11-04T12:00"), e.getEnd());
      }
    }
  }
}