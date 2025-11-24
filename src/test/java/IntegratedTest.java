import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for the integrated Calendar application, combining the model,
 * view, and controller components.
 */
public class IntegratedTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  /**
   * Set the System.out and System.err streams to our custom PrintStreams.
   */
  @Before
  public void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Reset the System.out and System.err streams to defaults.
   */
  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);
  }

  @Test
  public void testInteractiveCreateEventPrintEvent() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Play from 2025-10-27T15:00 to 2025-10-27T19:00")
        .append(System.lineSeparator())
        .append("print events on 2025-10-27")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Play")
        .append(" starting on ").append("2025-10-27").append(" at ").append("15:00")
        .append(", ending on ").append("2025-10-27").append(" at ").append("19:00")
        .append(System.lineSeparator())
        .append("Exiting...").append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveCreateEventAndEventSeriesPrintEventsFromDateToDate() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Exam from 2025-09-02T09:30 to 2025-09-02T10:30")
        .append(System.lineSeparator())
        .append("create event \"PDP Class\" from 2025-09-02T10:00 to 2025-09-02T11:00 ")
        .append("repeats MWF for 3 times")
        .append(System.lineSeparator())
        .append("print events from 2025-09-02T00:00 to 2025-09-06T00:00")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-09-02").append(" at ").append("09:30")
        .append(", ending on ").append("2025-09-02").append(" at ").append("10:30")
        .append(System.lineSeparator())
        .append(" - ").append("\"PDP Class\"")
        .append(" starting on ").append("2025-09-02").append(" at ").append("10:00")
        .append(", ending on ").append("2025-09-02").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("\"PDP Class\"")
        .append(" starting on ").append("2025-09-03").append(" at ").append("10:00")
        .append(", ending on ").append("2025-09-03").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("\"PDP Class\"")
        .append(" starting on ").append("2025-09-05").append(" at ").append("10:00")
        .append(", ending on ").append("2025-09-05").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append("Exiting...").append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveEditEventPrintEvent() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Play from 2025-10-27T15:00 to 2025-10-27T19:00")
        .append(System.lineSeparator())
        .append("edit event location Play from 2025-10-27T15:00 to 2025-10-27T19:00 ")
        .append("with online")
        .append(System.lineSeparator())
        .append("print events on 2025-10-27")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Play")
        .append(" starting on ").append("2025-10-27").append(" at ").append("15:00")
        .append(", ending on ").append("2025-10-27").append(" at ").append("19:00")
        .append(", Location: ONLINE").append(System.lineSeparator())
        .append("Exiting...").append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveEditEventsPrintEventsFromDateToDate() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Exam from 2025-09-02T09:30 to 2025-09-02T10:30")
        .append(System.lineSeparator())
        .append("create event PDP from 2025-09-02T10:00 to 2025-09-02T11:00 ")
        .append("repeats MWF for 3 times")
        .append(System.lineSeparator())
        .append("edit events subject PDP from 2025-09-03T10:00 with PDPClass")
        .append(System.lineSeparator())
        .append("print events from 2025-09-02T00:00 to 2025-09-06T00:00")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-09-02").append(" at ").append("09:30")
        .append(", ending on ").append("2025-09-02").append(" at ").append("10:30")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-09-02").append(" at ").append("10:00")
        .append(", ending on ").append("2025-09-02").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-09-03").append(" at ").append("10:00")
        .append(", ending on ").append("2025-09-03").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-09-05").append(" at ").append("10:00")
        .append(", ending on ").append("2025-09-05").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append("Exiting...").append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveEditSeriesPrintEventsFromDateToDate() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Exam from 2025-09-02T09:30 to 2025-09-02T10:30")
        .append(System.lineSeparator())
        .append("create event PDP from 2025-09-02T10:00 to 2025-09-02T11:00 ")
        .append("repeats MWF for 3 times")
        .append(System.lineSeparator())
        .append("edit series start PDP from 2025-09-03T10:00 with 2025-09-03T09:50")
        .append(System.lineSeparator())
        .append("print events from 2025-09-02T00:00 to 2025-09-06T00:00")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-09-02").append(" at ").append("09:30")
        .append(", ending on ").append("2025-09-02").append(" at ").append("10:30")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-09-02").append(" at ").append("09:50")
        .append(", ending on ").append("2025-09-02").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-09-03").append(" at ").append("09:50")
        .append(", ending on ").append("2025-09-03").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-09-05").append(" at ").append("09:50")
        .append(", ending on ").append("2025-09-05").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append("Exiting...").append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testHeadless() {
    CalendarRunner.main(new String[] {"--mode", "headless", "./src/test/java/testCommands.txt"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"),
        output.indexOf("Calendar created successfully", output.indexOf("Events:")));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-09-02").append(" at ").append("09:30")
        .append(", ending on ").append("2025-09-02").append(" at ").append("10:30")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-09-02").append(" at ").append("09:45")
        .append(", ending on ").append("2025-09-02").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-09-03").append(" at ").append("09:50")
        .append(", ending on ").append("2025-09-03").append(" at ").append("11:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-09-05").append(" at ").append("09:50")
        .append(", ending on ").append("2025-09-05").append(" at ").append("11:00")
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);

    trimmedOutput = output.substring(output.indexOf("Events:", output.indexOf("Events:") + 1));

    expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-01-01").append(" at ").append("06:30")
        .append(", ending on ").append("2025-01-01").append(" at ").append("07:30")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-01-01").append(" at ").append("06:45")
        .append(", ending on ").append("2025-01-01").append(" at ").append("08:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDPClass")
        .append(" starting on ").append("2025-01-02").append(" at ").append("06:50")
        .append(", ending on ").append("2025-01-02").append(" at ").append("08:00")
        .append(System.lineSeparator())
        .append("Exiting...").append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveCreateAllDayEventPrintEvent() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event \"Daily Sprint\" on 2025-11-04")
        .append(System.lineSeparator())
        .append("print events on 2025-11-04")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("\"Daily Sprint\"")
        .append(" starting on ").append("2025-11-04").append(" at ").append("08:00")
        .append(", ending on ").append("2025-11-04").append(" at ").append("17:00")
        .append(System.lineSeparator())
        .append("Exiting...")
        .append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveCreateAllDayEventAndEventSeriesUntilDatePrintEvents() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event \"Daily Sprint\" on 2025-11-04 repeats RF until 2026-01-28")
        .append(System.lineSeparator())
        .append("print events from 2025-11-21T10:00 to 2025-11-27T11:00 ")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("\"Daily Sprint\"")
        .append(" starting on ").append("2025-11-21").append(" at ").append("08:00")
        .append(", ending on ").append("2025-11-21").append(" at ").append("17:00")
        .append(System.lineSeparator())
        .append(" - ").append("\"Daily Sprint\"")
        .append(" starting on ").append("2025-11-27").append(" at ").append("08:00")
        .append(", ending on ").append("2025-11-27").append(" at ").append("17:00")
        .append(System.lineSeparator())
        .append("Exiting...")
        .append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testInteractiveShowStatus() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("show status on 2025-10-28T10:00")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();

    StringBuilder expected = new StringBuilder();
    expected.append("Available");

    assertTrue(output.contains(expected.toString()));
  }

  @Test
  public void testCreateEditUseCalendar() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("create calendar --name C2 --timezone America/Los_Angeles")
        .append(System.lineSeparator())
        .append("edit calendar --name C1 --property timezone America/Los_Angeles")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();

    assertTrue(output.contains("Calendar created successfully"));
    assertTrue(output.contains("Calendar Edited Successfully"));
    assertTrue(output.contains("Now using calendar : C1"));
  }

  @Test
  public void testCreateCalendarCopyEvent() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("create calendar --name C2 --timezone America/Los_Angeles")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Exam from 2025-09-02T09:30 to 2025-09-02T10:30")
        .append(System.lineSeparator())
        .append("copy event Exam on 2025-09-02T09:30 --target C2 to 2025-01-01T00:00")
        .append(System.lineSeparator())
        .append("use calendar --name C2")
        .append(System.lineSeparator())
        .append("print events from 2025-01-01T00:00 to 2025-09-06T00:00")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-01-01").append(" at ").append("00:00")
        .append(", ending on ").append("2025-01-01").append(" at ").append("01:00")
        .append(System.lineSeparator())
        .append("Exiting...")
        .append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testCreateCalendarCopyEventsSameDay() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("create calendar --name C2 --timezone America/Los_Angeles")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Exam from 2025-09-02T09:30 to 2025-09-02T10:30")
        .append(System.lineSeparator())
        .append("create event PDP from 2025-09-02T10:00 to 2025-09-02T11:00 ")
        .append("repeats MWF for 3 times")
        .append(System.lineSeparator())
        .append("copy events on 2025-09-02 --target C2 to 2025-01-01")
        .append(System.lineSeparator())
        .append("use calendar --name C2")
        .append(System.lineSeparator())
        .append("print events on 2025-01-01")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-01-01").append(" at ").append("06:30")
        .append(", ending on ").append("2025-01-01").append(" at ").append("07:30")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-01-01").append(" at ").append("07:00")
        .append(", ending on ").append("2025-01-01").append(" at ").append("08:00")
        .append(System.lineSeparator())
        .append("Exiting...")
        .append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

  @Test
  public void testCreateCalendarCopyEventsRangeOfDays() {
    StringBuilder input = new StringBuilder();
    input.append("create calendar --name C1 --timezone America/New_York")
        .append(System.lineSeparator())
        .append("create calendar --name C2 --timezone America/Los_Angeles")
        .append(System.lineSeparator())
        .append("use calendar --name C1")
        .append(System.lineSeparator())
        .append("create event Exam from 2025-09-02T09:30 to 2025-09-02T10:30")
        .append(System.lineSeparator())
        .append("create event PDP from 2025-09-02T10:00 to 2025-09-02T11:00 ")
        .append("repeats MWF for 3 times")
        .append(System.lineSeparator())
        .append("copy events between 2025-09-02 and 2025-10-02 --target C2 to 2025-01-01")
        .append(System.lineSeparator())
        .append("use calendar --name C2")
        .append(System.lineSeparator())
        .append("print events from 2025-01-01T00:00 to 2025-01-10T00:00")
        .append(System.lineSeparator())
        .append("Exit")
        .append(System.lineSeparator());

    System.setIn(new ByteArrayInputStream(input.toString().getBytes()));
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString();
    String trimmedOutput = output.substring(output.indexOf("Events:"));

    StringBuilder expected = new StringBuilder();
    expected.append("Events:")
        .append(System.lineSeparator())
        .append(" - ").append("Exam")
        .append(" starting on ").append("2025-01-01").append(" at ").append("06:30")
        .append(", ending on ").append("2025-01-01").append(" at ").append("07:30")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-01-01").append(" at ").append("07:00")
        .append(", ending on ").append("2025-01-01").append(" at ").append("08:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-01-02").append(" at ").append("07:00")
        .append(", ending on ").append("2025-01-02").append(" at ").append("08:00")
        .append(System.lineSeparator())
        .append(" - ").append("PDP")
        .append(" starting on ").append("2025-01-04").append(" at ").append("07:00")
        .append(", ending on ").append("2025-01-04").append(" at ").append("08:00")
        .append(System.lineSeparator())
        .append("Exiting...")
        .append(System.lineSeparator())
        .append(System.lineSeparator());

    assertEquals(expected.toString(), trimmedOutput);
  }

}