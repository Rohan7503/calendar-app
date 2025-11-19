package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for the Event class.
 */
public class EventTest {
  Event event;

  /**
   * Create an event before each test.
   */
  @Before
  public void setUp() {
    this.event = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();
  }

  @Test
  public void testBuilder() {
    Event event = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .location(EventLocation.PHYSICAL)
        .build();
    assertEquals("Gym", event.getSubject());
    assertEquals(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0), event.getStart());
    assertEquals(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0), event.getEnd());
    assertEquals(EventLocation.PHYSICAL, event.getLocation());
    assertNull(event.getStatus());
    assertNull(event.getDescription());
    assertNull(event.getSeriesId());

    String seriesId = UUID.randomUUID().toString();
    Event event2 = event.toBuilder()
        .location(EventLocation.ONLINE)
        .description("This is a test description.")
        .seriesId(seriesId)
        .build();
    assertEquals("Gym", event2.getSubject());
    assertEquals(EventLocation.ONLINE, event2.getLocation());
    assertEquals("This is a test description.", event2.getDescription());
    assertEquals(seriesId, event2.getSeriesId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidLocationEnumValue() {
    EventLocation.fromString("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidStatusEnumValue() {
    EventStatus.fromString("invalid");
  }

  @Test
  public void testInvalidEventEmptySubject() {
    try {
      Event event = Event.getBuilder()
          .subject("")
          .start(LocalDateTime.parse("2025-10-20T14:30"))
          .end(LocalDateTime.parse("2025-10-20T15:30"))
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event subject cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testInvalidEventNullSubject() {
    try {
      Event event = Event.getBuilder()
          .start(LocalDateTime.parse("2025-10-20T14:30"))
          .end(LocalDateTime.parse("2025-10-20T15:30"))
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event subject cannot be null or empty.", e.getMessage());
    }
  }

  @Test
  public void testInvalidEventNoStart() {
    try {
      Event event = Event.getBuilder()
          .subject("Test event")
          .end(LocalDateTime.parse("2025-10-20T15:30"))
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Start and end times cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testInvalidEventNoEnd() {
    try {
      Event event = Event.getBuilder()
          .subject("Test event")
          .start(LocalDateTime.parse("2025-10-20T14:30"))
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Start and end times cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testInvalidEventEndBeforeStart() {
    try {
      Event event = Event.getBuilder()
          .subject("Test event")
          .start(LocalDateTime.parse("2025-10-20T14:30"))
          .end(LocalDateTime.parse("2025-10-20T13:30"))
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("End time cannot be before start time.", e.getMessage());
    }
  }

  @Test
  public void testInvalidEventSeriesPrototypeSpansManyDays() {
    try {
      Event event = Event.getBuilder()
          .subject("Test event")
          .start(LocalDateTime.parse("2025-10-20T14:30"))
          .end(LocalDateTime.parse("2025-10-21T15:30"))
          .seriesId(UUID.randomUUID().toString())
          .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Event series prototype must not span more than 1 day.", e.getMessage());
    }
  }

  @Test
  public void testEventSeriesPrototypeWithEmptySeriesId() {
    Event event = Event.getBuilder()
        .subject("Test event")
        .start(LocalDateTime.parse("2025-10-20T14:30"))
        .end(LocalDateTime.parse("2025-10-21T15:30"))
        .seriesId("")
        .build();
    assertEquals("Test event", event.getSubject());

  }

  @Test
  public void testEquals() {
    assertEquals(this.event, this.event);
    assertNotEquals(this.event, "");
    Event event2 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();
    assertEquals(this.event, event2);
  }

  @Test
  public void testHashCode() {
    assertEquals(this.event.hashCode(), this.event.hashCode());
    assertNotEquals("", this.event.hashCode());
    Event event2 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();
    Event event3 = Event.getBuilder()
        .subject("Play")
        .start(LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0))
        .end(LocalDateTime.of(2020, Month.JANUARY, 1, 1, 0, 0))
        .build();

    assertEquals(this.event.hashCode(), event2.hashCode());
    assertNotEquals(this.event.hashCode(), event3.hashCode());
  }
}