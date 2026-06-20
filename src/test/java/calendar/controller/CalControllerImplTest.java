package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.CalModelInterface;
import calendar.model.Event;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for verifying the behavior of the {@link CalControllerImpl}
 * when operating in interactive mode (i.e., receiving commands directly from
 * user input through an input stream, rather than preloaded files).
 */
public class CalControllerImplTest {

  /**
   * Nested class MockModel.
   * this class acts as spy model class
   * to help in the testing of controller purely
   * isolated from model and view cohesion.
   * It mimics the original model class, but not
   * implements the methods exactly.
   */
  static class MockModel extends MultiCalControllerImplTest.MockModel {
    MockModel(StringBuilder log) {
      super(log);
    }
  }

  /**
   * Nested class SingleMockModel.
   * this class acts as spy model class
   * of the class that represents single calendar.
   * This class is created to help mock model class.
   */
  static class SingleMockModel implements SingleCalModelInterface {
    private final StringBuilder log;
    private final List<Event> events;

    SingleMockModel(StringBuilder log) {
      this.log = log;
      this.events = new ArrayList<>();
    }

    @Override
    public void addEvent(Event e) throws IllegalArgumentException {
      if (e == null) {
        log.append("Event cannot be null");
        return;
      }
      for (Event event : this.events) {
        if (e.equals(event)) {
          throw new IllegalArgumentException("Event already exists");
        }
      }
      this.events.add(e);
      log.append("subject=")
          .append(e.getSubject())
          .append(", start=")
          .append(e.getStart())
          .append(", end=")
          .append(e.getEnd())
          .append(System.lineSeparator());
    }

    @Override
    public void addEventSeriesForCount(Event prototype, String weekdays, int count)
        throws IllegalArgumentException {
      if (count <= 0) {
        throw new IllegalArgumentException("Count must be positive");
      }
      log.append("subject=")
          .append(prototype.getSubject())
          .append(", start=")
          .append(prototype.getStart())
          .append(", end=")
          .append(prototype.getEnd())
          .append(", weekdays=")
          .append(weekdays + " ")
          .append(", count=")
          .append(count)
          .append(System.lineSeparator());
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
      log.append("subject=")
          .append(prototype.getSubject())
          .append(", start=")
          .append(prototype.getStart())
          .append(", end=")
          .append(prototype.getEnd())
          .append(", weekdays=")
          .append(weekdays + " ")
          .append(", endDate=")
          .append(endDate + " ")
          .append(System.lineSeparator());

    }

    @Override
    public void editEvent(String property, String subject, LocalDateTime start, LocalDateTime end,
                          String newValue) throws IllegalArgumentException {
      log.append("property=")
          .append(property + " ")
          .append(", subject=")
          .append(subject + " ")
          .append(", start=")
          .append(start + " ")
          .append(", end=")
          .append(end + " ")
          .append(", newValue=")
          .append(newValue + " ")
          .append(System.lineSeparator());
    }

    @Override
    public void editEvents(String property, String subject, LocalDateTime start, String newValue,
                           boolean editWholeSeries) throws IllegalArgumentException {
      log.append("property=")
          .append(property + " ")
          .append(", subject=")
          .append(subject + " ")
          .append(", start=")
          .append(start + " ")
          .append(", newValue=")
          .append(newValue + " ")
          .append(", editWholeSeries=")
          .append(editWholeSeries + " ")
          .append(System.lineSeparator());
    }

    @Override
    public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end)
        throws IllegalArgumentException {
      if (start == null || end == null) {
        throw new IllegalArgumentException("Start and end date/time cannot be null.");
      }
      if (start.isAfter(end)) {
        throw new IllegalArgumentException("Start date/time cannot be after end date/time.");
      }
      log.append("start=")
          .append(start.toString() + " ")
          .append(", end=")
          .append(end.toString() + " ")
          .append(System.lineSeparator());
      return events;
    }

    @Override
    public List<Event> getAllEvents() {
      return List.of();
    }

    @Override
    public String checkAvailability(LocalDateTime dateTime) throws IllegalArgumentException {
      log.append("status on date: " + dateTime.toString() + " ")
          .append(System.lineSeparator());
      return "";
    }

    @Override
    public void deleteEvent(String subject, LocalDateTime start, LocalDateTime end)
        throws IllegalArgumentException {
      log.append("subject=")
          .append(subject + " ")
          .append(", start=")
          .append(start + " ")
          .append(", end=")
          .append(end + " ")
          .append(System.lineSeparator());
    }

    @Override
    public void deleteEvents(String subject, LocalDateTime start, boolean deleteWholeSeries)
        throws IllegalArgumentException {
      log.append("subject=")
          .append(subject + " ")
          .append(", start=")
          .append(start + " ")
          .append(", deleteWholeSeries=")
          .append(deleteWholeSeries + " ")
          .append(System.lineSeparator());
    }

    @Override
    public Event findEvent(String subject, LocalDateTime start, LocalDateTime end)
        throws IllegalArgumentException {
      return null;
    }

    @Override
    public ZoneId getTimezone() {
      return null;
    }

    @Override
    public void setTimezone(ZoneId newTimezone) throws IllegalArgumentException {

    }
  }


  /**
   * Nested class MockView.
   * this class acts as spy view class
   * to help in the testing of controller purely
   * isolated from model and view cohesion.
   * It mimics the original view class, but not
   * implements the methods exactly.
   */
  static class MockView implements CalViewInterface {
    private final StringBuilder log;

    public MockView(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void displayMessage(String msg) {
      log.append(msg);
    }

    @Override
    public void displayError(String msg) {
      log.append(msg + System.lineSeparator());
    }

    @Override
    public void displayEvents(List<Event> events) {
      log.append("event : {");
      log.append("subject: Meeting");
      log.append(", start: 2025-10-28T10:00");
      log.append(", end: 2025-10-28T11:00");
      log.append(", seriesId: S1");
      log.append("}").append(System.lineSeparator());

      log.append("event : {");
      log.append("subject: Workshop");
      log.append(", start: 2025-10-29T09:00");
      log.append(", end: 2025-10-29T12:00");
      log.append(", seriesId: S2");
      log.append("}").append(System.lineSeparator());
    }

    public String getLogs() {
      return log.toString();
    }
  }

  private final MultiCalModelInterface mockModel = new MockModel(new StringBuilder());
  private final CalViewInterface mockView = new MockView(new StringBuilder());
  private CalControllerInterface calController;
  private File inputFile;
  private File outputFile;
  private OutputStream outStream;
  private Readable inStream = new InputStreamReader(System.in);


  @Test
  public void testInteractiveModeInvalidCommandWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "publish events"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue(
        "Expected menu instructions",
        logs.contains("Unknown command keyword:")
    );
  }

  @Test
  public void testInteractiveModeMultipleCommandsWithoutActiveCalendar() {
    String userInput = String.join(System.lineSeparator(),
        "create event Meeting from 2025-10-02T10:00 to 2025-10-02T11:00",
        "",
        "create event Lunch from 2025-10-03T12:00 to 2025-10-03T13:00",
        "",
        "exit"
    );
    inStream = new StringReader(userInput);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockView) mockView).getLogs().trim();

    assertTrue("Expected instruction for use-calendar command!",
        logs.contains("To start the program, select a calendar "
            + "or create one if not exist."));
  }

  @Test
  public void testInteractiveModeMultipleCommandsWithActiveCalendar() {
    String userInput = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event Meeting from 2025-10-02T10:00 to 2025-10-02T11:00"
        + System.lineSeparator() + "create event Lunch from 2025-10-03T12:00 to 2025-10-03T13:00"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(userInput);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs().trim();

    assertTrue("Expected event details",
        logs.contains("subject=Meeting, start=2025-10-02T10:00, end=2025-10-02T11:00"
            + System.lineSeparator()
            + "subject=Lunch, start=2025-10-03T12:00, end=2025-10-03T13:00"));
  }

  @Test
  public void testInteractiveModeInvalidKeywordCommand() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "crete event Meeting from 2025-10-02T10:00 to 2025-10-02T11:00"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected 'error...' message to be displayed",
        logs.contains("Unknown command keyword: crete")
    );
  }

  @Test
  public void testInteractiveModeCreateSeriesInvalidDateTimeFormatCommand() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" "
        + "from 2025-2-28T10:00 to 2025-2-28T11:00 repeats WRF for 20 times"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected invalid syntax format message to be displayed",
        logs.contains("Invalid create series syntax. Expected Format: " + System.lineSeparator()
            + "• Timed event: create event <eventSubject> "
            + "from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm> "
            + "repeats <MTWRFSU> for <N> times " + System.lineSeparator()
            + "• All-day event: create event <eventSubject> "
            + "on <YYYY-MM-DD> repeats <MTWRFSU> for <N> times")
    );
  }

  @Test
  public void testInteractiveModeCreateSeriesInvalidWeekdayFormatCommand() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" "
        + "from 2025-2-28T10:00 to 2025-2-28T11:00 repeats GTH for 20 times"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected invalid syntax message to be displayed",
        logs.contains("Invalid create series syntax.")
    );
  }

  @Test
  public void testInteractiveModeCreateEventCommandWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event Meeting from 2025-10-02T10:00 to 2025-10-02T11:00"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();


    assertTrue(logs.contains("subject=Meeting, start=2025-10-02T10:00, "
        + "end=2025-10-02T11:00"));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Event Created successfully!"));

  }

  @Test
  public void testInteractiveModeCreateEventInvalidSyntaxWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event Meeting from 2025-10-02T10:00"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();


    assertTrue(viewLogs.contains("Invalid create event syntax"));

  }

  @Test
  public void testInteractiveModeCreateEventAllDayInvalidDateWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" on 2025-54-04"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();


    assertTrue(viewLogs.contains("Invalid Date"));

  }

  @Test
  public void testInteractiveModeInvalidDateCreateEventCommandWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event Meeting from 2025-10-2T10:00 to 2025-10-02T11:00"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Invalid date-time format: " + System.lineSeparator()
            + "Expected format: YYYY-MM-DDThh:mm (e.g., 2025-11-04T10:30)"));

  }

  @Test
  public void testInteractiveModeCreateSeriesCountInvalidDateCommandWithActiveCalendar() {
    String createSeriesUntil = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-45-28T11:00 repeats WRF for 20 times"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesUntil);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid Date Time"));
  }

  @Test
  public void testInteractiveModeCreateSeriesUntilAllDayInvalidDateCommandWithActiveCalendar() {
    String createSeriesUntil = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator()
        + "create event \"Daily Sprint\" on 2025-11-04 repeats RF until 2026-78-28"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesUntil);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid Date"));
  }

  @Test
  public void testInteractiveModeCreateSeriesUntilAllDayInvalidDateCommandWithActiveCalendar2() {
    String createSeriesUntil = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator()
        + "create event \"Daily Sprint\" on 2025-45-04 repeats RF until 2026-05-28"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesUntil);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid Date"));
  }

  @Test
  public void testInteractiveModeCreateSeriesCountAllDayInvalidDateCommandWithActiveCalendar() {
    String createSeriesUntil = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator()
        + "create event \"Daily Sprint\" on 2025-54-04 repeats RF for 10 times"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesUntil);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid Date"));
  }

  @Test
  public void testInteractiveModeCreateSeriesCountCommandWithActiveCalendar() {
    String createSeriesUntil = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-10-28T11:00 repeats WRF for 20 times"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesUntil);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("subject=\"Daily Sprint\", start=2025-10-28T10:00, "
        + "end=2025-10-28T11:00, weekdays=WRF , count=20"));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Series Created successfully!"));
  }

  @Test
  public void testInteractiveModeCreateSeriesUntilInvalidDateTimeCommandWithActiveCalendar() {
    String createSeriesCount = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-54-28T11:00 repeats WRF until 2026-03-28"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesCount);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid Date Time"));
  }

  @Test
  public void testInteractiveModeCreateSeriesUntilCommandWithActiveCalendar() {
    String createSeriesCount = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-10-28T11:00 repeats WRF until 2026-03-28"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createSeriesCount);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("subject=\"Daily Sprint\", start=2025-10-28T10:00, "
        + "end=2025-10-28T11:00, weekdays=WRF , endDate=2026-03-28"));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Series Created successfully!"));
  }

  @Test
  public void testInteractiveModeCreateInvalidDateAllDayEventCommandWithActiveCalendar() {
    String createAllDayEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" on 2025-11-4"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createAllDayEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();
    assertTrue(logs.contains("Invalid date format: " + System.lineSeparator()
        + "Expected format: YYYY-MM-DD (e.g., 2025-11-04)"));
  }

  @Test
  public void testInteractiveModeCreateAllDayEventCommandWithActiveCalendar() {
    String createAllDayEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" on 2025-11-04"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createAllDayEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(logs.contains("subject=\"Daily Sprint\", start=2025-11-04T08:00, "
        + "end=2025-11-04T17:00"));
  }

  @Test
  public void testInteractiveModeCreateAllDayEventSeriesUntilCommandWithActiveCalendar() {
    String createAllDaySeriesUntil = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" on 2025-11-04 "
        + "repeats RF until 2026-01-28"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(createAllDaySeriesUntil);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("subject=\"Daily Sprint\", start=2025-11-04T08:00, "
        + "end=2025-11-04T17:00, weekdays=RF , endDate=2026-01-28"));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Series Created successfully!"));
  }

  @Test
  public void testInteractiveModeCreateAllDayEventSeriesCountCommandWithActiveCalendar() {
    String createAllDayEventCount = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event \"Daily Sprint\" on 2025-11-04 "
        + "repeats RF for 10 times" + System.lineSeparator() + "exit";

    inStream = new StringReader(createAllDayEventCount);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("subject=\"Daily Sprint\", start=2025-11-04T08:00, "
        + "end=2025-11-04T17:00, weekdays=RF , count=10"));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Series Created successfully!"));
  }


  @Test
  public void testInteractiveModeEditEventInvalidDateWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event start \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-1-2T11:00 with 2025-10-28T09:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue("Expected successfully to appear in log,",
        viewLogs.contains("Invalid date-time format: " + System.lineSeparator()
            + "Expected format: YYYY-MM-DDThh:mm (e.g., 2025-11-04T10:30)"));

  }

  @Test
  public void testInteractiveModeEditEventInvalidDateWithActiveCalendarCommandLenTen() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event start meet "
        + "from 2025-10-28T10:00 to 2025-1-2T11:00 with 2025-10-28T09:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue("Expected successfully to appear in log,",
        viewLogs.contains("Invalid date-time format: " + System.lineSeparator()
            + "Expected format: YYYY-MM-DDThh:mm (e.g., 2025-11-04T10:30)"));

  }

  @Test
  public void testInteractiveModeEditEventIllegalDateWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event start \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-54-02T11:00 with 2025-10-28T09:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("invalid date-time"));

  }

  @Test
  public void testInteractiveModeEditEventInvalidSyntaxWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event start \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-10-28T11:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid edit event syntax"));

  }

  @Test
  public void testInteractiveModeEditEventStartCommandWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event start \"Daily Sprint\" "
        + "from 2025-10-28T10:00 to 2025-10-28T11:00 with 2025-10-28T09:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
        + "end=2025-10-28T11:00 , newValue=2025-10-28T09:00"));
    assertTrue("Expected successfully to appear in log,",
        viewLogs.contains("Event Edited successfully!"));

  }

  @Test
  public void testInteractiveModeEditEventEndCommandWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event end \"Daily Sprint\" "
        + "from 2025-10-02T10:00 to 2025-10-28T11:00 with 2025-10-28T12:00"
        + System.lineSeparator()
        + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("property=end , subject=\"Daily Sprint\" , "
        + "start=2025-10-02T10:00 , end=2025-10-28T11:00 , newValue=2025-10-28T12:00"));

  }

  @Test
  public void testInteractiveModeEditEventSubjectCommandWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event subject \"Daily Sprint\" "
        + "from 2025-10-02T10:00 to 2025-10-28T11:00 with \"Master Sprint\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("property=subject , subject=\"Daily Sprint\" , "
        + "start=2025-10-02T10:00 , " + "end=2025-10-28T11:00 , newValue=\"Master Sprint\""));

  }

  @Test
  public void testInteractiveModeEditEventLocationCommandWihActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event location \"Daily Sprint\" "
        + "from 2025-10-02T10:00 to 2025-10-28T11:00 with \"Hyderabad\""
        + System.lineSeparator()
        + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(logs.contains("property=location , subject=\"Daily Sprint\" , "
        + "start=2025-10-02T10:00 , " + "end=2025-10-28T11:00 , newValue=\"Hyderabad\""));
  }

  @Test
  public void testInteractiveModeEditEventDescCommandWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event description \"Daily Sprint\" "
        + "from 2025-10-02T10:00 to 2025-10-28T11:00 with \"Daily meet for project\""
        + System.lineSeparator()
        + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("property=description , subject=\"Daily Sprint\" , "
        + "start=2025-10-02T10:00 , "
        + "end=2025-10-28T11:00 , newValue=\"Daily meet for project\""));

  }

  @Test
  public void testInteractiveModeEditEventStatusPrivateCommandWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event status \"Master Sprint\" "
        + "from 2025-10-02T10:00 to 2025-10-28T11:00 with \"private\""
        + System.lineSeparator()
        + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(
        logs.contains("property=status , subject=\"Master Sprint\" , start=2025-10-02T10:00 , "
            + "end=2025-10-28T11:00 , newValue=\"private\""));
  }

  @Test
  public void testInteractiveModeEditEventStatusPublicCommandWithActiveCalendar() {
    String editEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit event status \"Daily Sprint\" "
        + "from 2025-10-02T10:00 to 2025-10-28T11:00 with \"public\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);

    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=status , subject=\"Daily Sprint\" , start=2025-10-02T10:00 , "
            + "end=2025-10-28T11:00 , newValue=\"public\""));

  }

  @Test
  public void testInteractiveModeEditEventsInvalidDateWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events subject \"Daily Sprint\" "
        + "from 2025-10-8T10:00 with \"Daily Master Sprint\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time format:"));

  }

  @Test
  public void testInteractiveModeEditEventsInvalidDateWithActiveCalendarLenEight() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events subject sprint "
        + "from 2025-10-8T10:00 with meet"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time format:"));

  }

  @Test
  public void testInteractiveModeEditEventsIllegalDateWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events subject \"Daily Sprint\" "
        + "from 2025-54-08T10:00 with \"Daily Master Sprint\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time"));

  }

  @Test
  public void testInteractiveModeEditEventsInvalidSyntaxWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events subject \"Daily Sprint\" "
        + "from 2025-10-8T10:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();
    assertTrue(viewLogs.contains("Invalid edit events syntax"));

  }

  @Test
  public void testInteractiveModeEditEventsSubjectCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events subject \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"Daily Master Sprint\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("property=subject , subject=\"Daily Sprint\" , "
        + "start=2025-10-28T10:00 , newValue=\"Daily Master Sprint\" , editWholeSeries=false"));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Events Edited successfully!"));

  }

  @Test
  public void testInteractiveModeEditEventsStartCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events start \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with 2025-10-28T09:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
        + "newValue=2025-10-28T09:00 , editWholeSeries=false"));
  }

  @Test
  public void testInteractiveModeEditEventsEndCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events end \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with 2025-10-28T13:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(logs.contains("property=end , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
        + "newValue=2025-10-28T13:00 , editWholeSeries=false"));

  }

  @Test
  public void testInteractiveModeEditEventsStatusPrivateCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events status \"Master Sprint\" "
        + "from 2025-10-28T10:00 with \"private\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=status , subject=\"Master Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"private\" , editWholeSeries=false"));

  }

  @Test
  public void testInteractiveModeEditEventsStatusPublicCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events status \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"public\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=status , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"public\" , editWholeSeries=false"));
  }

  @Test
  public void testInteractiveModeEditEventsLocationCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events location \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"Bengal\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=location , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"Bengal\" , editWholeSeries=false"));
  }

  @Test
  public void testInteractiveModeEditEventsDescCommandWithActiveCalendar() {
    String editEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events description \"Master Sprint\" "
        + "from 2025-10-28T10:00 with \"Cumulative project meet\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);

    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(logs.contains("property=description , subject=\"Master Sprint\" , "
        + "start=2025-10-28T10:00 , "
        + "newValue=\"Cumulative project meet\" , editWholeSeries=false"));
  }

  @Test
  public void testInteractiveModeEditSeriesStartCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit events start \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with 2025-10-28T09:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
        + "newValue=2025-10-28T09:00 , editWholeSeries=false"));
  }

  @Test
  public void testInteractiveModeEditSeriesSubjectCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit series subject \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"Casual Meet\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=subject , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"Casual Meet\" , editWholeSeries=true"));
  }

  @Test
  public void testInteractiveModeEditSeriesEndCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit series end \"Daily Sprint\" from 2025-10-28T10:00 "
        + "with 2025-10-28T15:00" + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("property=end , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
        + "newValue=2025-10-28T15:00 , editWholeSeries=true"));
  }

  @Test
  public void testInteractiveModeEditSeriesStatusPublicCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit series status \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"public\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);

    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=status , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"public\" , editWholeSeries=true"));
  }

  @Test
  public void testInteractiveModeEditSeriesStatusPrivateCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit series status \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"private\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(
        logs.contains("property=status , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"private\" , editWholeSeries=true"));
  }

  @Test
  public void testInteractiveModeEditSeriesLocationCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit series location \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"Mumbai\"" + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(
        logs.contains("property=location , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"Mumbai\" , editWholeSeries=true"));
  }

  @Test
  public void testInteractiveModeEditSeriesDescCommandWithActiveCalendar() {
    String editSeries = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "edit series description \"Daily Sprint\" "
        + "from 2025-10-28T10:00 with \"General meet for project discussion\""
        + System.lineSeparator() + "exit";

    inStream = new StringReader(editSeries);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(
        logs.contains("property=description , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
            + "newValue=\"General meet for project discussion\" , editWholeSeries=true"));
  }

  @Test
  public void testInteractiveModePrintSingleDayEventsCommandsWithActiveCalendar() {

    String printOneDayEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "print events on 2025-11-04"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(printOneDayEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();

    assertTrue(logs.contains("start=2025-11-04T00:00 , end=2025-11-05T00:00"));

  }


  @Test
  public void testInteractiveModePrintInRangeEventsCommandsWithActiveCalendar() {
    String printRangeEvents = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "print events from 2025-10-28T10:00 to 2025-10-28T11:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(printRangeEvents);
    calController = new CalControllerImpl(mockModel, mockView, inStream);

    calController.runInteractive();

    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(logs.contains("start=2025-10-28T10:00 , end=2025-10-28T11:00"));
  }

  @Test
  public void testInteractiveModeViewGetEventsCommandDisplayWithActiveCalendar() {
    String input = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "print events on 2025-11-04"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();
    String output = logs.trim();

    assertTrue(
        "Expected output to contain both events, but got:" + System.lineSeparator() + output,
        output.contains("event : "
            + "{subject: Meeting, start: 2025-10-28T10:00, end: 2025-10-28T11:00, seriesId: S1}")
            && output.contains("event : "
            + "{subject: Workshop, start: 2025-10-29T09:00, end: 2025-10-29T12:00, seriesId: S2}")
    );
  }

  @Test
  public void testInteractiveModeViewGetEventsInvalidDateCommandDisplayWithActiveCalendar() {
    String input = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "print events on 2025-28-04"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();
    String output = logs.trim();

    assertTrue(
        output.contains("Invalid date-time")
    );
  }

  @Test
  public void testInteractiveModeViewGetEventInRangeCommandDisplayWithActiveCalendar() {
    String input = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "print events from 2025-10-28T10:00 to 2025-10-28T11:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();
    String output = logs.trim();

    assertTrue(
        "Expected output to contain both events, but got:" + System.lineSeparator() + output,
        output.contains("event : "
            + "{subject: Meeting, start: 2025-10-28T10:00, end: 2025-10-28T11:00, seriesId: S1}")
            && output.contains("event : "
            + "{subject: Workshop, start: 2025-10-29T09:00, end: 2025-10-29T12:00, seriesId: S2}")
    );
  }

  @Test
  public void testInteractiveModeShowStatus() {
    String input = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator()
        + "show status on 2025-58-28T10:00"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue(viewLogs.contains("Invalid date-time"));

  }

  @Test
  public void testQueriesWithExtraSpacesWithActiveCalendar() {
    String input = "   create    calendar --name Birthday     --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "create event        "
        + "\"Daily Sprint\" on 2025-11-04 "
        + "repeats RF         until 2026-01-28"
        + System.lineSeparator() + "exit";

    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockModel) mockModel).getLogs();
    assertTrue(logs.contains("subject=\"Daily Sprint\", "
        + "start=2025-11-04T08:00, end=2025-11-04T17:00, weekdays=RF , endDate=2026-01-28"));
  }


}
