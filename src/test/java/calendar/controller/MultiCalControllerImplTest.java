package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.Event;
import calendar.model.EventLocation;
import calendar.model.EventStatus;
import calendar.model.MultiCalModelImpl;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the MultiCalendar commands.
 */
public class MultiCalControllerImplTest {
  private MultiCalModelInterface mockModel;
  private CalViewInterface mockView;
  private String input = "";
  private CalControllerInterface controller;
  private File inputFile;
  private Readable inStream = new InputStreamReader(System.in);


  /**
   * Mock implementation of {@link MultiCalModelInterface} used for testing purposes.
   * This mock tracks method calls and interactions through a {@link StringBuilder} log,
   * allowing tests to verify that the controller invokes model operations in the correct
   * sequence with the expected parameters.
   *
   */
  static class MockModel implements MultiCalModelInterface {
    private final StringBuilder log;
    private final Map<String, SingleCalModelInterface> calendars = new HashMap<>();
    private String activeCalendarName;
    private SingleCalModelInterface activeCalendar;

    MockModel(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void createCalendar(String calName, ZoneId timezone) throws IllegalArgumentException {
      if (calName == null || calName.isBlank()) {
        throw new IllegalArgumentException("Calendar name cannot be null or empty."
            + System.lineSeparator());
      }
      if (timezone == null) {
        throw new IllegalArgumentException("Timezone cannot be null."
            + System.lineSeparator());
      }
      if (calendars.containsKey(calName)) {
        throw new IllegalArgumentException("A calendar with the same name already exists."
            + System.lineSeparator());
      }
      log.append("calName: ").append(calName)
          .append(System.lineSeparator())
          .append("timeZone: ").append(timezone)
          .append(System.lineSeparator());
      activeCalendarName = calName;
      SingleCalModelInterface newCalendar = new SingleCalMockModel(log);
      calendars.put(calName, newCalendar);
    }

    @Override
    public void editCalendar(String calName, String propertyName, String newValue)
        throws IllegalArgumentException {
      switch (propertyName.toLowerCase()) {
        case "name":
        case "timezone":
          break;
        default:
          throw new IllegalArgumentException("Invalid property.");
      }
      log.append("calName: ").append(calName)
          .append(System.lineSeparator())
          .append("propertyName: ").append(propertyName)
          .append(System.lineSeparator())
          .append("newValue: ").append(newValue)
          .append(System.lineSeparator());
    }

    @Override
    public void useCalendar(String calName) throws IllegalArgumentException {
      setActiveCalendar(calName);
      activeCalendarName = calName;
      log.append("calToUse: ").append(calName)
          .append(System.lineSeparator())
          .append("activeCalendar: ")
          .append(activeCalendarName)
          .append(System.lineSeparator());

    }

    @Override
    public SingleCalModelInterface getActiveCalendar() {
      return calendars.get(activeCalendarName);
    }

    @Override
    public List<String> listCalendars() {
      return List.of(calendars.keySet().toArray(new String[0]));
    }

    @Override
    public String getActiveCalendarName() {
      return activeCalendarName;
    }

    @Override
    public ZoneId getTimezone(String calName) {
      return ZoneId.of("America/New_York");
    }

    @Override
    public SingleCalModelInterface getCalendar(String calName) {
      return calendars.get(calName);
    }

    @Override
    public void copyEvent(String eventName, LocalDateTime sourceStartTime, String targetCalendar,
                          LocalDateTime targetStartTime)
        throws IllegalArgumentException, IllegalStateException {
      if (activeCalendar == null) {
        throw new IllegalStateException("No active calendar selected.");
      }
      log.append("eventName: ").append(eventName)
          .append(System.lineSeparator())
          .append("sourceStartTime: ").append(sourceStartTime)
          .append(System.lineSeparator())
          .append("targetCalendar: ").append(targetCalendar)
          .append(System.lineSeparator())
          .append("targetStartTime: ")
          .append(targetStartTime)
          .append(System.lineSeparator());

    }

    @Override
    public void copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalendar,
                                  LocalDate targetStartDate)
        throws IllegalArgumentException, IllegalStateException {
      if (activeCalendar == null) {
        throw new IllegalStateException("No active calendar selected.");
      }
      log.append("startDate: ")
          .append(startDate.toString())
          .append(System.lineSeparator())
          .append("endDate: ")
          .append(endDate.toString())
          .append(System.lineSeparator())
          .append("targetCalendar: ")
          .append(targetCalendar)
          .append(System.lineSeparator())
          .append("targetStartDate: ")
          .append(targetStartDate.toString())
          .append(System.lineSeparator());

    }

    private void setActiveCalendar(String calendarName) {
      if (calendarName == null || calendarName.isBlank()) {
        throw new IllegalArgumentException("Calendar name cannot be null or empty.");
      }
      SingleCalModelInterface calendar = calendars.get(calendarName);
      if (calendar == null) {
        throw new IllegalArgumentException("Calendar with this name does not exist.");
      }
      this.activeCalendar = calendar;
    }

    public String getLogs() {
      return log.toString();
    }
  }

  /**
   * A mock implementation of a single calendar model used specifically for testing.
   */
  static class SingleCalMockModel extends CalControllerImplTest.SingleMockModel {
    public SingleCalMockModel(StringBuilder log) {
      super(log);
    }
  }

  /**
   * A mock implementation of the {@link CalViewInterface} used for testing controller
   * and model interactions without invoking actual view rendering.
   */
  static class MockView implements CalViewInterface {
    private final StringBuilder log;

    MockView(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void displayMessage(String msg) {
      log.append(msg).append(System.lineSeparator());
    }

    @Override
    public void displayError(String msg) {
      log.append(msg).append(System.lineSeparator());
    }

    @Override
    public void displayEvents(List<Event> events) {
      log.append("event : {");
      log.append("subject: Meeting");
      log.append(", start: 2025-10-28T10:00");
      log.append(", end: 2025-10-28T11:00");
      log.append(", seriesId: S1");
      log.append("}").append(System.lineSeparator());
    }

    public String getLogs() {
      return log.toString();
    }
  }


  /**
   * Initializes mock model and view instances before each test execution.
   */
  @Before
  public void setUp() {
    mockModel = new MockModel(new StringBuilder());
    mockView = new MockView(new StringBuilder());
  }

  @Test
  public void testMultiCalHeadlessMode() {
    inputFile = new File("src/test/java/calendar/controller/MultiCalCommands.txt");
    try {
      String fileContents = new String(Files.readAllBytes(Paths.get(inputFile.toURI())));
      inStream = new StringReader(fileContents);
      controller = new CalControllerImpl(mockModel, mockView, inStream);
      controller.runHeadless();
      String output = ((MockModel) mockModel).getLogs().trim();
      assertTrue(output.contains("calName: Work"));
      assertTrue(output.contains("timeZone: America/New_York"));

      assertTrue(output.contains("calName: Work"));
      assertTrue(output.contains("propertyName: timezone"));
      assertTrue(output.contains("newValue: America/Chicago"));
      assertTrue(output.contains("calToUse: Work"));
      assertTrue(output.contains("activeCalendar: Work"));


      assertTrue(output.contains("eventName: \"Team Meeting\""));
      assertTrue(output.contains("sourceStartTime: 2024-03-15T10:00"));
      assertTrue(output.contains("targetCalendar: Work"));
      assertTrue(output.contains("targetStartTime: 2024-03-20T14:00"));

      assertTrue(output.contains("eventName: Meeting"));

      assertTrue(output.contains("sourceStartTime: 2024-03-15T10:00"));
      assertTrue(output.contains("targetCalendar: Work"));
      assertTrue(output.contains("targetStartDate: 2024-03-22"));


      assertTrue(output.contains("startDate: 2024-03-01"));
      assertTrue(output.contains("endDate: 2024-03-07"));
      assertTrue(output.contains("targetCalendar: Work"));
      assertTrue(output.contains("targetStartDate: 2024-03-15"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testMultiCalHeadlessErrors() {
    inputFile = new File("src/test/java/calendar/controller/MultiCalInvalidCommands.txt");
    try {
      String fileContents = new String(Files.readAllBytes(Paths.get(inputFile.toURI())));
      inStream = new StringReader(fileContents);
      controller = new CalControllerImpl(mockModel, mockView, inStream);
      controller.runHeadless();
      String output = ((MockView) mockView).getLogs().trim();

      assertTrue(output.contains(
          "Invalid create calendar syntax. Expected Format: " + System.lineSeparator()
              + "create calendar --name <calName> --timezone area/location"));
      assertTrue(output.contains("Invalid timezone format. Expected format: "
          + "'Area/Location' (e.g., 'America/New_York')"));
      assertTrue(output.contains(
          "Invalid Edit calendar syntax. Expected Format: " + System.lineSeparator()));
      assertTrue(
          output.contains("Invalid use calendar syntax. Expected Format: " + System.lineSeparator()
              + "use calendar --name <name-of-calendar>"));
      assertTrue(output.contains("Invalid Event copy syntax"));
      assertTrue(output.contains("Invalid Event Range Copy syntax."));
      assertTrue(output.contains("Invalid date-time format: " + System.lineSeparator()
          + "Expected format: YYYY-MM-DDThh::mm (e.g., 2025-11-04T10:30)"));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testCreateCalendar() throws IOException {
    input = "create calendar --name Work --timezone America/New_York"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockModel) mockModel).getLogs().trim();
    assertEquals("calName: Work" + System.lineSeparator()
        + "timeZone: America/New_York", output);
  }

  @Test
  public void testCreateCalendarDuplicateException() throws IOException {
    mockModel.createCalendar("Work", ZoneId.of("America/New_York"));
    input = "create calendar --name Work --timezone America/New_York"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("A calendar with the same name already exists."));
  }

  @Test
  public void testCreateCalendarIllegalArgs() throws IOException {
    input = "create calendar --name Work"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid create calendar syntax. Expected Format: " + System.lineSeparator()
        + "create calendar --name <calName> --timezone area/location";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCreateCalendarInvalidZone() throws IOException {
    input = "create calendar --name Personal --timezone Europe@London"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid timezone format. "
        + "Expected format: 'Area/Location' (e.g., 'America/New_York')";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCreateCalendarZoneException() throws IOException {
    input = "create calendar --name Personal --timezone Invalid/London"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("Invalid Zone"));
  }

  @Test
  public void testEditCalendarTimezone() throws IOException {
    input = "edit calendar --name Work --property timezone America/Chicago"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockModel) mockModel).getLogs().trim();
    String viewLogs = ((MockView) mockView).getLogs();
    assertEquals("calName: Work" + System.lineSeparator()
        + "propertyName: timezone" + System.lineSeparator()
        + "newValue: America/Chicago", output);

    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Calendar Edited Successfully"));

  }

  @Test
  public void testEditCalendarIllegalArgs() throws IOException {
    input = "edit calendar  --property timezone America/Chicago"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid Edit calendar syntax. Expected Format: " + System.lineSeparator()
        + "edit calendar --name <name-of-calendar> "
        + "--property <property-name> <new-property-value>";
    assertTrue(output.contains(expected));

  }

  @Test
  public void testEditCalendarInvalidZone() throws IOException {
    input = "edit calendar --name Work --property timezone America@Chicago"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid timezone format. "
        + "Expected format: 'Area/Location' (e.g., 'America/New_York')";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testEditCalendarNullProp() throws IOException {
    input = "edit calendar --name Work --property location India/Delhi "
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid property.";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testUseCalendarTimezone() throws IOException {
    input = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockModel) mockModel).getLogs().trim();
    String viewLogs = ((MockView) mockView).getLogs().trim();
    assertEquals("calName: Birthday" + System.lineSeparator()
        + "timeZone: Australia/Sydney" + System.lineSeparator()
        + "calToUse: Birthday" + System.lineSeparator()
        + "activeCalendar: Birthday", output);

    assertTrue(viewLogs.contains("Now using calendar : Birthday"));

  }

  @Test
  public void testUseCalendarInvalidZone() throws IOException {
    input = "create calendar --name Birthday --timezone Australia@Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid timezone format. "
        + "Expected format: 'Area/Location' (e.g., 'America/New_York')";
    assertTrue(output.contains(expected));
  }


  @Test
  public void testCopyEvent() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy event \"Doctor Appointment\" "
        + "on 2024-04-10T09:30 --target Personal to 2024-04-17T09:30"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockModel) mockModel).getLogs().trim();
    String viewLogs = ((MockView) mockView).getLogs();
    String expected = "eventName: \"Doctor Appointment\"" + System.lineSeparator()
        + "sourceStartTime: 2024-04-10T09:30" + System.lineSeparator()
        + "targetCalendar: Personal" + System.lineSeparator()
        + "targetStartTime: 2024-04-17T09:30";
    assertTrue(output.contains(expected));

    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Event copied successfully"));
  }

  @Test
  public void testCopyEventIllegalState() throws IOException {
    input = "copy event \"Doctor Appointment\" "
        + "on 2024-04-10T09:30 --target Personal to 2024-04-17T09:30"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();

    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("No active calendar selected."));
  }


  @Test
  public void testCopyEventInvalidDateTime() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy event \"Doctor Appointment\" "
        + "on 2024-04-18T09:30 --target Personal to 2024-4-17T09:30"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid date-time format: " + System.lineSeparator()
        + "Expected format: YYYY-MM-DDThh::mm (e.g., 2025-11-04T10:30)";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCopyEventIllegalDateTime() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy event \"Doctor Appointment\" "
        + "on 2024-04-18T09:30 --target Personal to 2024-24-17T09:30"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "invalid date-time";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCopyEvents() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy events on 2024-03-15 --target Work to 2024-03-22"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockModel) mockModel).getLogs().trim();
    String viewLogs = ((MockView) mockView).getLogs();
    String expected = "startDate: 2024-03-15" + System.lineSeparator()
        + "endDate: 2024-03-15" + System.lineSeparator()
        + "targetCalendar: Work" + System.lineSeparator()
        + "targetStartDate: 2024-03-22";
    assertTrue(output.contains(expected));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Events Copied Successfully"));
  }

  @Test
  public void testCopyEventsIllegalState() throws IOException {
    input = "copy events on 2024-03-15 --target Work to 2024-03-22"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("No active calendar selected."));
  }

  @Test
  public void testCopyEventsInvalidDateTime() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy events on 2024-3-21 --target Work to 2024-03-22"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid date-time format: " + System.lineSeparator()
        + "Expected format: YYYY-MM-DDThh::mm (e.g., 2025-11-04T10:30)";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCopyEventsIllegalDateTime() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy events on 2024-22-21 --target Work to 2024-03-22"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "invalid Date";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCopyEventsBetween() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy events between 2024-03-01 and 2024-03-07 --target Work to 2024-03-15"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String viewLogs = ((MockView) mockView).getLogs();
    String output = ((MockModel) mockModel).getLogs().trim();
    String expected = "startDate: 2024-03-01" + System.lineSeparator()
        + "endDate: 2024-03-07" + System.lineSeparator()
        + "targetCalendar: Work" + System.lineSeparator()
        + "targetStartDate: 2024-03-15";
    assertTrue(output.contains(expected));
    assertTrue("Expected successfully to appear in log",
        viewLogs.contains("Events Copied Successfully"));

  }

  @Test
  public void testCopyEventsBetweenInvalidDateTime() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy events between 2024-3-01 and 2024-03-07 --target Work to 2024-03-15"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "Invalid date-time format: " + System.lineSeparator()
        + "Expected format: YYYY-MM-DDThh::mm (e.g., 2025-11-04T10:30)";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testCopyEventsBetweenIllegalDateTime() throws IOException {
    input = "create calendar --name Work --timezone Australia/Sydney"
        + System.lineSeparator()
        + "use calendar --name Work"
        + System.lineSeparator()
        + "copy events between 2024-20-01 and 2024-03-07 --target Work to 2024-03-15"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(mockModel, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    String expected = "invalid Date";
    assertTrue(output.contains(expected));
  }

  @Test
  public void testExportToCsv() throws IOException {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();
    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T08:00"))
        .end(LocalDateTime.parse("2025-10-27T09:00"))
        .location(EventLocation.ONLINE)
        .description("Gym, for an hour.")
        .status(EventStatus.PRIVATE)
        .build();
    cal.addEvent(event1);
    Event event2 = Event.getBuilder()
        .subject("Play")
        .start(LocalDateTime.parse("2025-10-26T08:00"))
        .end(LocalDateTime.parse("2025-10-26T17:00"))
        .location(EventLocation.PHYSICAL)
        .description("\"Play\"")
        .status(EventStatus.PUBLIC)
        .build();
    cal.addEvent(event2);
    Event event3 = Event.getBuilder()
        .subject("Tennis")
        .start(LocalDateTime.parse("2025-10-28T15:00"))
        .end(LocalDateTime.parse("2025-10-28T17:00"))
        .build();
    cal.addEvent(event3);
    Event prototype = Event.getBuilder()
        .subject("Class")
        .start(LocalDateTime.parse("2025-10-26T11:00"))
        .end(LocalDateTime.parse("2025-10-26T13:00"))
        .location(EventLocation.PHYSICAL)
        .build();
    cal.addEventSeriesForCount(prototype, "MTWRF", 5);

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal data_export_sample.csv"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    cleanupFiles();
  }

  @Test
  public void testDeleteSingleEventViaCli() {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "create event Gym from 2025-10-27T08:00 to 2025-10-27T09:00"
        + System.lineSeparator()
        + "delete event Gym from 2025-10-27T08:00 to 2025-10-27T09:00"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();

    String output = ((MockView) mockView).getLogs();
    assertTrue(output.contains("Event Deleted successfully!"));
    model.useCalendar("C1");
    assertTrue(model.getActiveCalendar().getAllEvents().isEmpty());
  }

  @Test
  public void testDeleteSeriesViaCli() {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "create event Class from 2025-11-03T09:00 to 2025-11-03T10:00 repeats MWF for 5 times"
        + System.lineSeparator()
        + "delete series Class from 2025-11-03T09:00"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();

    String output = ((MockView) mockView).getLogs();
    assertTrue(output.contains("Events Deleted successfully!"));
    model.useCalendar("C1");
    assertTrue(model.getActiveCalendar().getAllEvents().isEmpty());
  }

  @Test
  public void testExportToCsvAndValidate() throws IOException {
    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T08:00"))
        .end(LocalDateTime.parse("2025-10-27T17:00"))
        .location(EventLocation.ONLINE)
        .description("\"Gym\" for an hour.")
        .status(EventStatus.PRIVATE)
        .allDay(true)
        .build();

    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();
    cal.addEvent(event1);
    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal data_export_sample.csv"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("Calendar successfully exported to: "));

    String line;
    try (BufferedReader br = new BufferedReader(new FileReader("data_export_sample.csv"))) {
      line = br.readLine();
      assertTrue(line.contains("Subject,Start Date,Start Time,End Date,"
          + "End Time,All Day Event,Description,Location,Private"));
      line = br.readLine();
      String[] fields = line.split(",");
      assertEquals("Gym", fields[0]);
      assertEquals("", fields[2]);
      assertEquals("", fields[4]);
      assertEquals("True", fields[5]);
      assertEquals("\"\"\"Gym\"\" for an hour.\"", fields[6]);
      assertEquals("True", fields[8]);
    } catch (IOException e) {
      fail();
    } finally {
      cleanupFiles();
    }
  }

  @Test
  public void testCliAllDayEventExportsAsAllDayButTimedEventDoesNot() {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "create event Holiday on 2025-10-27"
        + System.lineSeparator()
        + "create event Meeting from 2025-10-28T08:00 to 2025-10-28T17:00"
        + System.lineSeparator()
        + "export cal data_export_sample.csv"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();

    try (BufferedReader br = new BufferedReader(new FileReader("data_export_sample.csv"))) {
      br.readLine();
      String[] allDayRow = br.readLine().split(",");
      assertEquals("Holiday", allDayRow[0]);
      assertEquals("", allDayRow[2]);
      assertEquals("True", allDayRow[5]);

      String[] timedRow = br.readLine().split(",");
      assertEquals("Meeting", timedRow[0]);
      assertEquals("08:00 AM", timedRow[2]);
      assertEquals("False", timedRow[5]);
    } catch (IOException e) {
      fail();
    } finally {
      cleanupFiles();
    }
  }

  @Test
  public void testExportInvalidEmptyEvents() throws IOException {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal data_export_sample.csv"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("No events found."));
    cleanupFiles();
  }

  @Test
  public void testExportInvalidFilenameNotEndingInCsvOrIcs() throws IOException {
    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T00:00"))
        .end(LocalDateTime.parse("2025-10-27T01:00"))
        .location(EventLocation.ONLINE)
        .build();

    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();
    cal.addEvent(event1);

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal abc.pdf"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("Unsupported file type."));
    cleanupFiles();
  }

  @Test
  public void testExportToCsvInvalidIoException() {
    try {
      Event event1 = Event.getBuilder()
          .subject("Gym")
          .start(LocalDateTime.parse("2025-10-27T00:00"))
          .end(LocalDateTime.parse("2025-10-27T01:00"))
          .location(EventLocation.ONLINE)
          .build();

      MultiCalModelInterface model = new MultiCalModelImpl();
      model.createCalendar("C1", ZoneId.of("America/New_York"));
      model.useCalendar("C1");
      SingleCalModelInterface cal = model.getActiveCalendar();
      cal.addEvent(event1);

      Path dir = Paths.get("invalid.csv");
      Path invalidCsvFile = Files.createDirectory(dir);

      input = "use calendar --name C1"
          + System.lineSeparator()
          + "export cal " + invalidCsvFile
          + System.lineSeparator()
          + "exit";
      inStream = new StringReader(input);
      controller = new CalControllerImpl(model, mockView, inStream);
      controller.runInteractive();
      String output = ((MockView) mockView).getLogs().trim();
      assertTrue(output.contains("Error writing to file:"));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      cleanupFiles();
    }
  }

  @Test
  public void testExportToIcal() throws IOException {
    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();

    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T08:00"))
        .end(LocalDateTime.parse("2025-10-27T09:00"))
        .location(EventLocation.ONLINE)
        .description("Gym, for an hour.")
        .status(EventStatus.PRIVATE)
        .build();
    Event event2 = Event.getBuilder()
        .subject("Play")
        .start(LocalDateTime.parse("2025-10-26T08:00"))
        .end(LocalDateTime.parse("2025-10-26T17:00"))
        .location(EventLocation.PHYSICAL)
        .description("\"Play\"")
        .status(EventStatus.PUBLIC)
        .build();
    Event event3 = Event.getBuilder()
        .subject("Tennis")
        .start(LocalDateTime.parse("2025-10-28T15:00"))
        .end(LocalDateTime.parse("2025-10-28T17:00"))
        .build();
    Event prototype = Event.getBuilder()
        .subject("Class")
        .start(LocalDateTime.parse("2025-10-26T11:00"))
        .end(LocalDateTime.parse("2025-10-26T13:00"))
        .location(EventLocation.PHYSICAL)
        .build();

    cal.addEvent(event1);
    cal.addEvent(event2);
    cal.addEvent(event3);
    cal.addEventSeriesForCount(prototype, "MTWRF", 5);

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal data_export_sample.ics"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    cleanupFiles();
  }

  @Test
  public void testExportToIcalAndValidate() {
    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T08:00"))
        .end(LocalDateTime.parse("2025-10-27T17:00"))
        .location(EventLocation.ONLINE)
        .description("\"Gym\" for an hour.")
        .status(EventStatus.PRIVATE)
        .allDay(true)
        .build();

    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();
    cal.addEvent(event1);

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal data_export_sample.ics"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("Calendar successfully exported to: "));

    try (BufferedReader br = new BufferedReader(new FileReader("data_export_sample.ics"))) {
      String line = br.readLine();
      assertEquals("BEGIN:VCALENDAR", line);
      line = br.readLine();
      assertTrue(line.startsWith("VERSION:2.0"));
      line = br.readLine();
      assertTrue(line.startsWith("PRODID:-//MyCalendarApp//EN"));
      line = br.readLine();
      assertEquals("BEGIN:VEVENT", line);
      StringBuilder eventBlock = new StringBuilder();
      while ((line = br.readLine()) != null && !line.equals("END:VEVENT")) {
        eventBlock.append(line).append(System.lineSeparator());
      }
      String eventData = eventBlock.toString();
      assertTrue(eventData.contains("UID"));
      assertTrue(eventData.contains("DTSTAMP"));
      assertTrue(eventData.contains("SUMMARY:Gym"));
      assertTrue(eventData.contains("DESCRIPTION:\"Gym\" for an hour."));
      assertTrue(eventData.contains("DTSTART;VALUE=DATE:20251027"));
      assertTrue(eventData.contains("DTEND;VALUE=DATE:20251028"));
      assertTrue(eventData.contains("LOCATION:ONLINE"));
      assertTrue(eventData.contains("CLASS:PRIVATE"));
      assertEquals("END:VEVENT", line);
      line = br.readLine();
      assertEquals("END:VCALENDAR", line);
    } catch (IOException e) {
      fail("IOException while reading .ics file: " + e.getMessage());
    } finally {
      cleanupFiles();
    }
  }

  @Test
  public void testExportToIcalAndValidateWithNonAllDayEvent() {
    Event event1 = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-10-27T09:00"))
        .end(LocalDateTime.parse("2025-10-27T10:00"))
        .location(EventLocation.ONLINE)
        .description("\"Gym\" for an hour.")
        .status(EventStatus.PRIVATE)
        .build();

    MultiCalModelInterface model = new MultiCalModelImpl();
    model.createCalendar("C1", ZoneId.of("America/New_York"));
    model.useCalendar("C1");
    SingleCalModelInterface cal = model.getActiveCalendar();
    cal.addEvent(event1);

    input = "use calendar --name C1"
        + System.lineSeparator()
        + "export cal data_export_sample.ics"
        + System.lineSeparator()
        + "exit";
    inStream = new StringReader(input);
    controller = new CalControllerImpl(model, mockView, inStream);
    controller.runInteractive();
    String output = ((MockView) mockView).getLogs().trim();
    assertTrue(output.contains("Calendar successfully exported to: "));

    try (BufferedReader br = new BufferedReader(new FileReader("data_export_sample.ics"))) {
      String line = br.readLine();
      assertEquals("BEGIN:VCALENDAR", line);
      line = br.readLine();
      assertTrue(line.startsWith("VERSION:2.0"));
      line = br.readLine();
      assertTrue(line.startsWith("PRODID:-//MyCalendarApp//EN"));
      line = br.readLine();
      assertEquals("BEGIN:VEVENT", line);
      StringBuilder eventBlock = new StringBuilder();
      while ((line = br.readLine()) != null && !line.equals("END:VEVENT")) {
        eventBlock.append(line).append(System.lineSeparator());
      }
      String eventData = eventBlock.toString();
      assertTrue(eventData.contains("UID"));
      assertTrue(eventData.contains("DTSTAMP"));
      assertTrue(eventData.contains("SUMMARY:Gym"));
      assertTrue(eventData.contains("DESCRIPTION:\"Gym\" for an hour."));
      assertTrue(eventData.contains("DTSTART:20251027T090000"));
      assertTrue(eventData.contains("DTEND:20251027T100000"));
      assertTrue(eventData.contains("LOCATION:ONLINE"));
      assertTrue(eventData.contains("CLASS:PRIVATE"));
      assertEquals("END:VEVENT", line);
      line = br.readLine();
      assertEquals("END:VCALENDAR", line);
    } catch (IOException e) {
      fail("IOException while reading .ics file: " + e.getMessage());
    } finally {
      cleanupFiles();
    }
  }

  @Test
  public void testExportToIcalInvalidIoException() {
    try {
      Event event1 = Event.getBuilder()
          .subject("Gym")
          .start(LocalDateTime.parse("2025-10-27T00:00"))
          .end(LocalDateTime.parse("2025-10-27T01:00"))
          .location(EventLocation.ONLINE)
          .build();

      MultiCalModelInterface model = new MultiCalModelImpl();
      model.createCalendar("C1", ZoneId.of("America/New_York"));
      model.useCalendar("C1");
      SingleCalModelInterface cal = model.getActiveCalendar();
      cal.addEvent(event1);

      Path dir = Paths.get("invalid.ics");
      Path invalidIcsFile = Files.createDirectory(dir);

      input = "use calendar --name C1"
          + System.lineSeparator()
          + "export cal " + invalidIcsFile
          + System.lineSeparator()
          + "exit";
      inStream = new StringReader(input);
      controller = new CalControllerImpl(model, mockView, inStream);
      controller.runInteractive();
      String output = ((MockView) mockView).getLogs().trim();
      assertTrue(output.contains("Error writing to file:"));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      cleanupFiles();
    }
  }

  private void cleanupFiles() {
    try {
      Files.deleteIfExists(Paths.get("invalid.csv"));
      Files.deleteIfExists(Paths.get("invalid.ics"));
      Files.deleteIfExists(Paths.get("xyz.csv"));
      Files.deleteIfExists(Paths.get("abc.pdf"));
      Files.deleteIfExists(Paths.get("data_export_sample.csv"));
      Files.deleteIfExists(Paths.get("data_export_sample.ics"));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
