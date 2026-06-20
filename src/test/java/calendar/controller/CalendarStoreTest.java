package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.EventLocation;
import calendar.model.EventStatus;
import calendar.model.MultiCalModelImpl;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Test;

/**
 * Tests for {@link CalendarStore}: serialization round-trips and file save/load.
 */
public class CalendarStoreTest {

  private final CalendarStore store = new CalendarStore();

  private MultiCalModelInterface sampleModel() {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("Work", ZoneId.of("America/New_York"));
    model.createCalendar("Home", ZoneId.of("Europe/Paris"));
    model.useCalendar("Work");

    SingleCalModelInterface work = model.getCalendar("Work");
    work.addEvent(Event.getBuilder()
        .subject("Standup")
        .start(LocalDateTime.parse("2025-11-04T09:00"))
        .end(LocalDateTime.parse("2025-11-04T09:15"))
        .location(EventLocation.ONLINE)
        .status(EventStatus.PRIVATE)
        .description("Daily sync, room 2")
        .build());
    work.addEvent(Event.getBuilder()
        .subject("Holiday")
        .start(LocalDateTime.parse("2025-12-25T08:00"))
        .end(LocalDateTime.parse("2025-12-25T17:00"))
        .allDay(true)
        .build());
    return model;
  }

  @Test
  public void testRoundTripPreservesCalendarsAndEvents() {
    String serialized = store.serialize(sampleModel());

    MultiCalModelInterface restored = new MultiCalModelImpl();
    store.deserializeInto(serialized, restored);

    assertTrue(restored.listCalendars().contains("Work"));
    assertTrue(restored.listCalendars().contains("Home"));
    assertEquals("Work", restored.getActiveCalendarName());
    assertEquals(ZoneId.of("Europe/Paris"), restored.getTimezone("Home"));

    List<Event> workEvents = restored.getCalendar("Work").getAllEvents();
    assertEquals(2, workEvents.size());
    Event standup = workEvents.get(0);
    assertEquals("Standup", standup.getSubject());
    assertEquals(EventLocation.ONLINE, standup.getLocation());
    assertEquals(EventStatus.PRIVATE, standup.getStatus());
    assertEquals("Daily sync, room 2", standup.getDescription());
    assertFalse(standup.isAllDay());
    assertTrue(workEvents.get(1).isAllDay());
  }

  @Test
  public void testRoundTripPreservesSeriesIdAndSpecialCharacters() {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.getCalendar("C1").addEvent(Event.getBuilder()
        .subject("Tab\tand\nnewline")
        .start(LocalDateTime.parse("2025-11-04T09:00"))
        .end(LocalDateTime.parse("2025-11-04T10:00"))
        .seriesId("series-123")
        .build());

    MultiCalModelInterface restored = new MultiCalModelImpl();
    store.deserializeInto(store.serialize(model), restored);

    Event event = restored.getCalendar("C1").getAllEvents().get(0);
    assertEquals("Tab\tand\nnewline", event.getSubject());
    assertEquals("series-123", event.getSeriesId());
  }

  @Test
  public void testSaveAndLoadFile() throws IOException {
    Path file = Files.createTempDirectory("calstore").resolve("state.dat");
    store.save(sampleModel(), file);
    assertTrue(Files.exists(file));

    MultiCalModelInterface restored = new MultiCalModelImpl();
    boolean loaded = store.load(restored, file);
    assertTrue(loaded);
    assertEquals(2, restored.listCalendars().size());

    Files.deleteIfExists(file);
    Files.deleteIfExists(file.getParent());
  }

  @Test
  public void testLoadMissingFileReturnsFalse() {
    MultiCalModelInterface restored = new MultiCalModelImpl();
    boolean loaded = store.load(restored, Path.of("does-not-exist-12345.dat"));
    assertFalse(loaded);
    assertTrue(restored.listCalendars().isEmpty());
  }

  @Test
  public void testEmptyModelSerializesToEmptyString() {
    assertEquals("", store.serialize(new MultiCalModelImpl()));
  }
}
