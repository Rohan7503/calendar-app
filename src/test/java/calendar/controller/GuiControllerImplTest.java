package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.MultiCalModelImpl;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalGuiInterface;
import calendar.view.CalViewInterface;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class to validate controller's gui mode operations.
 */
public class GuiControllerImplTest {

  private CalControllerInterface calController;
  private final CalViewInterface calView = new CalControllerImplTest.MockView(new StringBuilder());
  private MultiCalModelInterface model;
  private CalGuiInterface gui;
  private Features guiController;
  String viewLogs;
  String modelLogs;

  /**
   * Nested class MockModel.
   * this class acts as spy model class
   * to help in the testing of controller purely
   * isolated from model and view cohesion.
   * It mimics the original model class, but not
   * implements the methods exactly.
   */
  static class MockModel implements MultiCalModelInterface {
    private final StringBuilder log;
    private final Map<String, SingleMockModel> calendars = new HashMap<>();
    private String activeCalendarName;
    private SingleMockModel activeCalendar;

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
      SingleMockModel newCalendar = new SingleMockModel(log);
      calendars.put(calName, newCalendar);
    }

    @Override
    public void editCalendar(String calName, String propertyName, String newValue)
        throws IllegalArgumentException {
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
    public SingleMockModel getActiveCalendar() {
      return calendars.get(activeCalendarName);
    }

    @Override
    public List<String> listCalendars() {
      return List.of(calendars.keySet().toArray(new String[0]));
    }

    @Override
    public void copyEvent(String eventName, LocalDateTime sourceStartTime, String targetCalendar,
                          LocalDateTime targetStartTime)
        throws IllegalArgumentException, IllegalStateException {
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
      SingleMockModel calendar = calendars.get(calendarName);
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
      List<Event> matches = new ArrayList<>();
      for (Event e : this.events) {
        boolean match = e.getSubject().equals(subject) && e.getStart().equals(start);
        if (end != null) {
          match = match && e.getEnd().equals(end);
        }
        if (match) {
          matches.add(e);
        }
      }
      if (matches.isEmpty()) {
        throw new IllegalArgumentException("No matching event found.");
      }
      if (matches.size() > 1) {
        throw new IllegalArgumentException("Multiple matching events found.");
      }
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
      List<Event> matches = new ArrayList<>();
      for (Event e : this.events) {
        boolean match = e.getSubject().equals(subject) && e.getStart().equals(start);
        if (match) {
          matches.add(e);
        }
      }
      if (matches.isEmpty()) {
        throw new IllegalArgumentException("No matching event found.");
      }
      if (matches.size() > 1) {
        throw new IllegalArgumentException("Multiple matching events found.");
      }
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
   * Mock implementation of gui view for controller testing.
   */
  static class MockView implements CalGuiInterface {
    private final StringBuilder log;

    MockView(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void showGui() {

    }

    @Override
    public void addFeatures(Features features) {

    }

    @Override
    public void showCalendars(List<String> calendarNames, String activeCalendar) {
      log.append("Calendars : ");
      for (String cal : calendarNames) {
        log.append(cal)
            .append(System.lineSeparator());
      }
      log.append("active cal : ")
          .append(activeCalendar);
    }

    @Override
    public void showEventsForDay(LocalDate day, List<Event> events) {
      log.append("Events for day : ")
          .append(day.toString())
          .append(System.lineSeparator());
      for (Event event : events) {
        log.append(event.getSubject())
            .append(System.lineSeparator())
            .append(event.getStart())
            .append(System.lineSeparator())
            .append(event.getEnd())
            .append(System.lineSeparator());
      }
    }

    @Override
    public void refreshEvents() {
      log.append("Event refreshed.");
    }

    @Override
    public void showMessage(String message) {
      log.append(message)
          .append(System.lineSeparator());
    }

    @Override
    public void showError(String message) {
      log.append(message)
          .append(System.lineSeparator());
    }

    private String getLogs() {
      return log.toString();
    }
  }

  /**
   * Initializes fresh mock model, view, and controller objects before each test.
   */
  @Before
  public void setUp() {
    model = new MockModel(new StringBuilder());
    gui = new MockView(new StringBuilder());
    guiController = new GuiControllerImpl(model, gui);
  }


  @Test
  public void testCreateCalendarCallback() {
    String name = "work";
    String timezone = "America/Los_Angeles";
    guiController.createCalendar(name, timezone);
    modelLogs = ((MockModel) model).getLogs();
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Successfully created calendar"));
    assertTrue(viewLogs.contains("Calendars : work" + System.lineSeparator()
        + "active cal : work"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains("calName: work" + System.lineSeparator()
        + "timeZone: America/Los_Angeles" + System.lineSeparator()
        + "calToUse: work" + System.lineSeparator()
        + "activeCalendar: work"));
  }

  @Test
  public void testCreateCalendarInvalidTimeZone() {
    String name = "work";
    String timezone = "Invalid/America";
    guiController.createCalendar(name, timezone);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid time zone"));
  }

  @Test
  public void testCreateCalendarEmptyTimeZone() {
    String name = "work";
    String timezone = "";
    guiController.createCalendar(name, timezone);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid time zone"));
  }

  @Test
  public void testCreateCalendarEmptyName() {
    String name = "";
    String timezone = "America/Los_Angeles";
    guiController.createCalendar(name, timezone);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Calendar name cannot be null or empty."));
  }


  @Test
  public void testCreateCalDuplicateName() {
    ZoneId zoneId = ZoneId.of("America/Los_Angeles");
    model.createCalendar("work", zoneId);
    String name = "work";
    String timezone = "America/Los_Angeles";
    guiController.createCalendar(name, timezone);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("same name already exists."));
  }

  @Test
  public void testSelectCalendar() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.createCalendar("personal", ZoneId.of("America/Los_Angeles"));
    String name = "work";
    guiController.selectCalendar(name);
    modelLogs = ((MockModel) model).getLogs();
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Calendars : work" + System.lineSeparator()
        + "personal" + System.lineSeparator()
        + "active cal : work"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains(name));
  }

  @Test
  public void testSelectCalendarEmptyName() {
    String name = "";
    guiController.selectCalendar(name);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Calendar name cannot be null or empty."));
  }

  @Test
  public void testSelectNonExistingCalendar() {
    String name = "work";
    guiController.selectCalendar(name);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Calendar with this name does not exist"));
  }


  @Test
  public void testRequestEventsForDay() {
    Event e = Event.getBuilder()
        .subject("daily meet")
        .start(LocalDateTime.parse("2025-08-12T10:00"))
        .end(LocalDateTime.parse("2025-08-12T13:00"))
        .build();
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    SingleCalModelInterface activeCalendar = model.getActiveCalendar();
    activeCalendar.addEvent(e);
    String date = "2025-08-12";
    guiController.requestEventsForDay(date);
    modelLogs = ((MockModel) model).getLogs();
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Events for day : 2025-08-12" + System.lineSeparator()
        + "daily meet"));
    assertTrue(modelLogs.contains(date));
  }

  @Test
  public void testRequestEventsForDayInvalidDate() {
    // date-time : YYYY-MM-DDThh::mm
    // date : YYYY-MM-DD
    String date = "2025-08-73";
    guiController.requestEventsForDay(date);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time"));
  }


  @Test
  public void testRequestEventsForDayEmptyDate() {
    guiController.requestEventsForDay("");
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time"));
  }

  @Test
  public void testCreateEvent() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    guiController.createEvent(subject, start, end);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("Event Created successfully!"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains("subject=daily meet, "
        + "start=2025-08-12T08:00, "
        + "end=2025-08-12T13:05"));
  }

  @Test
  public void testCreateEventInvalidDate() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    guiController.createEvent(subject, start, end);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time"));
  }

  @Test
  public void testCreateEventDuplicateEvent() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    SingleCalModelInterface activeCal = model.getActiveCalendar();
    Event newEvent = Event.getBuilder()
        .subject("daily meet")
        .start(LocalDateTime.parse("2025-08-12T08:00"))
        .end(LocalDateTime.parse("2025-08-12T13:05"))
        .build();
    activeCal.addEvent(newEvent);
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    guiController.createEvent(subject, start, end);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Event already exists"));
  }

  @Test
  public void testCreateEventSeriesByCount() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "1";
    guiController.createEventSeriesByCount(subject, start, end, weekdays, count);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("Series Created successfully!"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains("subject=daily meet, "
        + "start=2025-08-12T08:00, "
        + "end=2025-08-12T13:05, "
        + "weekdays=MTW , "
        + "count=1"));
  }

  @Test
  public void testCreateEventSeriesByCountInvalidCount() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "abc";
    guiController.createEventSeriesByCount(subject, start, end, weekdays, count);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid count format"));
  }

  @Test
  public void testCreateEventSeriesByCountNegativeCount() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "-2";
    guiController.createEventSeriesByCount(subject, start, end, weekdays, count);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Count must be positive"));
  }

  @Test
  public void testCreateEventSeriesByCountInvalidStartDate() {
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "abc";
    guiController.createEventSeriesByCount(subject, start, end, weekdays, count);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid date"));
  }

  @Test
  public void testCreateEventSeriesUntilDate() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String untilDate = "2025-08-24";
    guiController.createEventSeriesUntilDate(subject, start, end, weekdays, untilDate);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("Series Created successfully!"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains("subject=daily meet, "
        + "start=2025-08-12T08:00, "
        + "end=2025-08-12T13:05, "
        + "weekdays=MTW , "
        + "endDate=2025-08-24 "));
  }

  @Test
  public void testCreateEventSeriesByUntilDateStartBeforeEndDate() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T13:05";
    String end = "2025-08-12T08:05";
    String weekdays = "MTW";
    String untilDate = "2025-08-24";
    guiController.createEventSeriesUntilDate(subject, start, end, weekdays, untilDate);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("End time cannot be before start time."));
  }

  @Test
  public void testCreateEventSeriesByUntilDateInvalidStartDate() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String untilDate = "2025-08-12";
    guiController.createEventSeriesUntilDate(subject, start, end, weekdays, untilDate);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid Date-time"));
  }

  @Test
  public void testCreateEventSeriesByUntilDateInvalidUntilDate() {
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String untilDate = "2025-45-24";
    guiController.createEventSeriesUntilDate(subject, start, end, weekdays, untilDate);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid Date-time"));

  }

  @Test
  public void testEditEvent() {
    Event e = Event.getBuilder()
        .subject("daily meet")
        .start(LocalDateTime.parse("2025-08-12T10:00"))
        .end(LocalDateTime.parse("2025-08-12T13:00"))
        .build();
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    SingleCalModelInterface activeCal = model.getActiveCalendar();
    activeCal.addEvent(e);
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-12T10:00";
    String end = "2025-08-12T13:00";
    String newVal = "2025-08-12T11:05";
    guiController.editEvent(property, subject, start, end, newVal);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("Event Edited successfully!"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains("property=start , "
        + "subject=daily meet , "
        + "start=2025-08-12T10:00 , "
        + "end=2025-08-12T13:00 , "
        + "newValue=2025-08-12T11:05"));
  }

  @Test
  public void testEditEventNoMatch() {
    Event e = Event.getBuilder()
        .subject("daily meet")
        .start(LocalDateTime.parse("2025-08-12T10:00"))
        .end(LocalDateTime.parse("2025-08-12T13:00"))
        .build();
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    SingleCalModelInterface activeCal = model.getActiveCalendar();
    activeCal.addEvent(e);
    String property = "start";
    String subject = "master meet";
    String start = "2025-08-12T08:00";
    String end = "2025-08-12T13:05";
    String newVal = "2025-08-12T11:05";
    guiController.editEvent(property, subject, start, end, newVal);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("No matching event found."));

  }

  @Test
  public void testEditEventInvalidStart() {
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-45T08:00";
    String end = "2025-08-12T13:05";
    String newVal = "2025-08-12T11:05";
    guiController.editEvent(property, subject, start, end, newVal);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time"));
  }

  @Test
  public void testEditEvents() {
    Event e = Event.getBuilder()
        .subject("master meet")
        .start(LocalDateTime.parse("2025-08-12T08:00"))
        .end(LocalDateTime.parse("2025-08-12T13:00"))
        .build();
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    SingleCalModelInterface activeCal = model.getActiveCalendar();
    activeCal.addEvent(e);
    String property = "start";
    String subject = "master meet";
    String start = "2025-08-12T08:00";
    String newVal = "2025-08-12T11:05";
    boolean editWholeSeries = true;
    guiController.editEvents(property, subject, start, newVal, editWholeSeries);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("Events Edited successfully!"));
    assertTrue(viewLogs.contains("Event refreshed."));
    assertTrue(modelLogs.contains("property=start , "
        + "subject=master meet , "
        + "start=2025-08-12T08:00 , "
        + "newValue=2025-08-12T11:05 , "
        + "editWholeSeries=true "));
  }

  @Test
  public void testEditEventsNoMatch() {
    Event e = Event.getBuilder()
        .subject("daily meet")
        .start(LocalDateTime.parse("2025-08-12T10:00"))
        .end(LocalDateTime.parse("2025-08-12T13:00"))
        .build();
    model.createCalendar("work", ZoneId.of("America/Los_Angeles"));
    model.useCalendar("work");
    SingleCalModelInterface activeCal = model.getActiveCalendar();
    activeCal.addEvent(e);
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-12T08:00";
    String newVal = "2025-08-12T11:05";
    boolean editWholeSeries = true;
    guiController.editEvents(property, subject, start, newVal, editWholeSeries);
    viewLogs = ((MockView) gui).getLogs();
    modelLogs = ((MockModel) model).getLogs();
    assertTrue(viewLogs.contains("No matching event found."));

  }


  @Test
  public void testEditEventsInvalidStart() {
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-45T08:00";
    String newVal = "2025-08-12T11:05";
    boolean editWholeSeries = true;
    guiController.editEvents(property, subject, start, newVal, editWholeSeries);
    viewLogs = ((MockView) gui).getLogs();
    assertTrue(viewLogs.contains("Invalid date-time"));
  }

}
