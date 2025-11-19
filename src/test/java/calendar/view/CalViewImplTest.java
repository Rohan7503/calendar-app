package calendar.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.EventLocation;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for CalViewImpl class.
 */
public class CalViewImplTest {
  private ByteArrayOutputStream outContent;
  private ByteArrayOutputStream errContent;
  private CalViewImpl view;

  /**
   * Instantiate the view with ByteArrayOutputStream objects for output and error messages.
   */
  @Before
  public void setUp() {
    this.outContent = new ByteArrayOutputStream();
    this.errContent = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(outContent);
    PrintStream err = new PrintStream(errContent);
    this.view = new CalViewImpl(out, err);
  }

  @Test
  public void testDisplayMessage() {
    String message = "Hello World!";
    view.displayMessage(message);
    assertEquals(message, outContent.toString().trim());
  }

  @Test
  public void testDisplayError() {
    String message = "Error!";
    view.displayError(message);
    assertEquals("Error: " + message, errContent.toString().trim());
  }

  @Test
  public void testDisplayEvents() {
    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T00:00"))
        .end(LocalDateTime.parse("2025-10-27T01:00"))
        .location(EventLocation.ONLINE)
        .build();
    Event event2 = Event.getBuilder()
        .subject("Play")
        .start(LocalDateTime.parse("2025-10-26T03:00"))
        .end(LocalDateTime.parse("2025-10-26T15:00"))
        .location(EventLocation.PHYSICAL)
        .build();
    Event event3 = Event.getBuilder()
        .subject("Tennis")
        .start(LocalDateTime.parse("2025-10-28T15:00"))
        .end(LocalDateTime.parse("2025-10-28T19:00"))
        .build();

    List<Event> events = Arrays.asList(event1, event2, event3);
    view.displayEvents(events);

    assertTrue(outContent.toString().trim().contains("Events:"));
    assertTrue(outContent.toString().trim().contains("Play"));
    assertTrue(outContent.toString().trim().contains("2025-10-26"));
    assertTrue(outContent.toString().trim().contains("15:00"));
    assertTrue(outContent.toString().trim().contains("Tennis"));
    assertTrue(outContent.toString().trim().contains("2025-10-28"));
    assertTrue(outContent.toString().trim().contains("Gym"));
  }

  @Test
  public void testDisplayEventsInvalidNull() {
    view.displayEvents(null);
    assertEquals("No events found." + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testDisplayEventsInvalidEmpty() {
    view.displayEvents(Collections.emptyList());
    assertEquals("No events found." + System.lineSeparator(), outContent.toString());
  }
}