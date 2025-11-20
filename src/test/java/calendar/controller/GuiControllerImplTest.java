package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.MultiCalModelImpl;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalGuiInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class to validate controller's gui mode operations.
 */
public class GuiControllerImplTest {

  private  MultiCalModelInterface model;
  private  CalGuiInterface gui;
  private Features guiController;
  String logs;
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
   * A mock implementation of a single calendar model used specifically for testing.
   */
  static class SingleCalMockModel extends CalControllerImplTest.SingleMockModel {
    public SingleCalMockModel(StringBuilder log) {
      super(log);
    }
  }

  /**
   * Mock implementation of gui view for controller testing.
   */
  static class MockView implements CalGuiInterface{
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
      for(String cal : calendarNames) {
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
        for(Event event : events) {
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


  @Before
  public void setUp() {
    model = new MockModel(new StringBuilder());
    gui = new MockView(new StringBuilder());
    guiController = new GuiControllerImpl(model, gui);
  }

  @Test
  public void testCreateCalendarCallback(){
      String name = "work";
      String timezone = "America/Los_Angeles";
      guiController.createCalendar(name, timezone);
      logs = ((MockView)gui).getLogs();
      assertTrue(logs.contains("Successfully created calendar"));
  }

  @Test
  public void testCreateCalendarInvalidTimeZone(){
    String name = "work";
    String timezone = "Invalid/America";
    guiController.createCalendar(name, timezone);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid time zone"));
  }

  @Test
  public void testCreateCalendarNullTimezone(){
    String name = "work";
    String timezone = "";
    guiController.createCalendar(name, timezone);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Timezone cannot be null."));

  }

  @Test
  public void testCreateCalDuplicateName(){
    ZoneId zoneId = ZoneId.of("America/Los_Angeles");
    model.createCalendar("work",zoneId);
    String name = "work";
    String timezone = "America/Los_Angeles";
    guiController.createCalendar(name, timezone);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("same name already exists."));
  }

  @Test
  public void testSelectCalendar(){
    model.createCalendar("work",ZoneId.of("America/Los_Angeles"));
    model.createCalendar("personal",ZoneId.of("America/Los_Angeles"));
    String name = "work";
    guiController.selectCalendar(name);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("calendars :"));
  }

  @Test
  public void testSelectCalendarEmptyName(){
    String name = "";
    model.useCalendar(name);
    guiController.selectCalendar(name);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Calendar name cannot be null or empty."));
  }

  @Test
  public void testSelectNonExistingCalendar(){
    String name = "work";
    guiController.selectCalendar(name);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Calendar with this name does not exist"));
  }


  @Test
  public void testRequestEventsForDay(){
    String date = "2025-08-12";
    guiController.requestEventsForDay(date);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Event requested"));
  }

  @Test
  public void testRequestEventsForDayInvalidDate(){
    // date-time : YYYY-MM-DDThh::mm
    // date : YYYY-MM-DD
    String date = "2025-08-73";
    guiController.requestEventsForDay(date);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid date-time"));
  }

  @Test
  public void testRequestEventsForDayNullDate(){
    guiController.requestEventsForDay(null);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Start and end date/time cannot be null."));
  }

  @Test
  public void testCreateEvent(){
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    guiController.createEvent(subject, start, end);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Event Created successfully!"));
  }

  @Test
  public void testCreateEventNullEvent(){

  }

  @Test
  public void testCreateEventDuplicateEvent(){
    SingleCalModelInterface activeCal = model.getActiveCalendar();
    Event newEvent = Event.getBuilder()
        .subject("daily meet")
        .start(LocalDateTime.parse("2025-08-12T08::00"))
        .end(LocalDateTime.parse("2025-08-12T13:05"))
        .build();
    activeCal.addEvent(newEvent);
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    guiController.createEvent(subject, start, end);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Event already exists"));
  }

  @Test
  public void testCreateEventSeriesByCount(){
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "1";
    guiController.createEventSeriesByCount(subject,start,end,weekdays,count);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Series Created successfully!"));
  }

  @Test
  public void testCreateEventSeriesByCountInvalidCount(){
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "abc";
    guiController.createEventSeriesByCount(subject,start,end,weekdays,count);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid count"));
  }

  @Test
  public void testCreateEventSeriesByCountInvalidStartDate(){
    String subject = "daily meet";
    String start = "2025-08-73T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String count = "abc";
    guiController.createEventSeriesByCount(subject,start,end,weekdays,count);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid  date"));
  }

  @Test
  public void testCreateEventSeriesByUntilDate(){
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String untilDate = "2025-08-24";
    guiController.createEventSeriesUntilDate(subject,start,end,weekdays,untilDate);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Series Created successfully!"));
  }

  @Test
  public void testCreateEventSeriesByUntilDateInvalidStartDate(){
    String subject = "daily meet";
    String start = "2025-08-75T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String untilDate = "2025-08-12";
    guiController.createEventSeriesUntilDate(subject,start,end,weekdays,untilDate);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid date-time"));
  }

  @Test
  public void testCreateEventSeriesByUntilDateInvalidUntilDate(){
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String weekdays = "MTW";
    String untilDate = "2025-08-24";
    guiController.createEventSeriesUntilDate(subject,start,end,weekdays,untilDate);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid date"));

  }

  @Test
  public void testEditEvent(){
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String end = "2025-08-12T13:05";
    String newVal = "2025-08-12T11:05";
    guiController.editEvent(property,subject,start,end,newVal);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Event Edited successfully!"));
  }

  @Test
  public void testEditEventInvalidStart(){
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-45T08::00";
    String end = "2025-08-12T13:05";
    String newVal = "2025-08-12T11:05";
    guiController.editEvent(property,subject,start,end,newVal);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid date-time"));
  }

  @Test
  public void testEditEvents(){
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-12T08::00";
    String newVal = "2025-08-12T11:05";
    boolean editWholeSeries =  true;
    guiController.editEvents(property,subject,start,newVal,editWholeSeries);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Event Edited successfully!"));
  }

  @Test
  public void testEditEventsInvalidStart(){
    String property = "start";
    String subject = "daily meet";
    String start = "2025-08-45T08::00";
    String newVal = "2025-08-12T11:05";
    boolean editWholeSeries =  true;
    guiController.editEvents(property,subject,start,newVal,editWholeSeries);
    logs = ((MockView)gui).getLogs();
    assertTrue(logs.contains("Invalid date-time"));
  }



}
