package calendar.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Tests for {@link EventGrouping}, the pure (non-Swing) bucketing/ordering logic shared by the
 * calendar views.
 */
public class EventGroupingTest {

  private Event timed(String subject, String start, String end) {
    return Event.getBuilder()
        .subject(subject)
        .start(LocalDateTime.parse(start))
        .end(LocalDateTime.parse(end))
        .build();
  }

  private Event allDay(String subject, String date) {
    return Event.getBuilder()
        .subject(subject)
        .start(LocalDateTime.parse(date + "T08:00"))
        .end(LocalDateTime.parse(date + "T17:00"))
        .allDay(true)
        .build();
  }

  @Test
  public void testByStartDateBucketsAndOrders() {
    Event later = timed("Later", "2025-01-15T14:00", "2025-01-15T15:00");
    Event earlier = timed("Earlier", "2025-01-15T09:00", "2025-01-15T10:00");
    Event otherDay = timed("Other", "2025-01-16T09:00", "2025-01-16T10:00");

    Map<LocalDate, List<Event>> byDay =
        EventGrouping.byStartDate(List.of(later, otherDay, earlier));

    assertEquals(2, byDay.size());
    List<Event> jan15 = byDay.get(LocalDate.parse("2025-01-15"));
    assertEquals(2, jan15.size());
    assertEquals("Earlier", jan15.get(0).getSubject());
    assertEquals("Later", jan15.get(1).getSubject());
  }

  @Test
  public void testOnDayFiltersAndOrders() {
    Event a = timed("A", "2025-01-15T14:00", "2025-01-15T15:00");
    Event b = timed("B", "2025-01-15T09:00", "2025-01-15T10:00");
    Event c = timed("C", "2025-01-16T09:00", "2025-01-16T10:00");

    List<Event> result = EventGrouping.onDay(List.of(a, b, c), LocalDate.parse("2025-01-15"));

    assertEquals(2, result.size());
    assertEquals("B", result.get(0).getSubject());
    assertEquals("A", result.get(1).getSubject());
  }

  @Test
  public void testAllDayAndTimedSplit() {
    Event holiday = allDay("Holiday", "2025-01-15");
    Event meeting = timed("Meeting", "2025-01-15T09:00", "2025-01-15T10:00");
    List<Event> events = List.of(holiday, meeting);

    List<Event> allDayEvents = EventGrouping.allDay(events);
    List<Event> timedEvents = EventGrouping.timed(events);

    assertEquals(1, allDayEvents.size());
    assertTrue(allDayEvents.get(0).isAllDay());
    assertEquals(1, timedEvents.size());
    assertEquals("Meeting", timedEvents.get(0).getSubject());
  }

  @Test
  public void testEmptyInputProducesEmptyResults() {
    assertTrue(EventGrouping.byStartDate(List.of()).isEmpty());
    assertTrue(EventGrouping.onDay(List.of(), LocalDate.parse("2025-01-15")).isEmpty());
  }
}
