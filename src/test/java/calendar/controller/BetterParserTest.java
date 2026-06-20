package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link BetterParser} class.
 *
 */
public class BetterParserTest {
  BetterParser betterParser;
  ParsedCommand result;

  /**
   * Initializes the test environment before each test case.
   * A new instance of {@link BetterParser} is created to ensure that
   * each test runs in isolation with a fresh parser instance.
   *
   * @throws Exception if an unexpected error occurs during setup
   */
  @Before
  public void setUp() throws Exception {
    betterParser = new BetterParser();
  }

  @Test
  public void testCreateCalendarCommand() {
    result = betterParser.parse(
        "create calendar --name WorkCalendar --timezone America/New_York");
    assertEquals(CommandType.CREATE_CALENDAR, result.getCommandType());
    assertEquals("WorkCalendar", result.getArguments().get("calName"));
    assertEquals("America/New_York", result.getArguments().get("timezone"));
  }

  @Test
  public void testInvalidSyntaxMissingTimezone() {
    try {
      String input = "create calendar --name WorkCalendar";
      betterParser.parse(input);
      fail("invalid syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testInvalidTimezoneFormat() {
    try {
      String input = "create calendar --name WorkCalendar --timezone New_York@America";
      betterParser.parse(input);
      fail("invalid timezone formate exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testDeleteEventCommand() {
    result = betterParser.parse(
        "delete event Gym from 2025-10-27T08:00 to 2025-10-27T09:00");
    assertEquals(CommandType.DELETE_EVENT, result.getCommandType());
    assertEquals("Gym", result.getArguments().get("subject"));
    assertEquals("2025-10-27T08:00", result.getArguments().get("start"));
    assertEquals("2025-10-27T09:00", result.getArguments().get("end"));
  }

  @Test
  public void testDeleteSeriesCommand() {
    result = betterParser.parse("delete series Class from 2025-11-03T09:00");
    assertEquals(CommandType.DELETE_EVENTS, result.getCommandType());
    assertEquals("Class", result.getArguments().get("subject"));
    assertEquals("true", result.getArguments().get("deleteWholeSeries"));
  }

  @Test
  public void testDeleteEventsFromHereCommand() {
    result = betterParser.parse("delete events Class from 2025-11-03T09:00");
    assertEquals(CommandType.DELETE_EVENTS, result.getCommandType());
    assertEquals("false", result.getArguments().get("deleteWholeSeries"));
  }

  @Test
  public void testInvalidDeleteEventSyntax() {
    try {
      betterParser.parse("delete event Gym 2025-10-27T08:00");
      fail("invalid delete syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testUseCalendarCommand() {
    result = betterParser.parse("use calendar --name WorkCalendar");
    assertEquals("WorkCalendar", result.getArguments().get("calName"));
    assertEquals(CommandType.USE_CALENDAR, result.getCommandType());
  }

  @Test
  public void testInvalidSyntaxUseCalendarCommand() {
    try {
      result = betterParser.parse("use calendar");
      fail("invalid syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testEditCalendarCommand() {
    result = betterParser.parse(
        "edit calendar --name WorkCalendar --property timezone America/Boston");
    assertEquals("WorkCalendar", result.getArguments().get("calName"));
    assertEquals("timezone", result.getArguments().get("propertyName"));
    assertEquals("America/Boston", result.getArguments().get("newValue"));
    assertEquals(CommandType.EDIT_CALENDAR, result.getCommandType());
  }

  @Test
  public void testInvalidSyntaxEditCalendarCommand() {
    try {
      String input = "edit calendar --name WorkCalendar";
      result = betterParser.parse(input);
      fail("invalid syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testInvalidTimeZonePropertyEditCommand() {
    try {
      String input = "edit calendar --name WorkCalendar --property timezone America-Boston";
      betterParser.parse(input);
      fail("invalid timezone formate exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testEventCopyCommand() {
    result = betterParser.parse("copy event TeamMeeting "
        + "on 2025-11-04T10:30 --target WorkCalendar to 2025-11-05T09:00");
    assertEquals(CommandType.COPY_EVENT, result.getCommandType());
    assertEquals("TeamMeeting", result.getArguments().get("eventName"));
    assertEquals("2025-11-04T10:30", result.getArguments().get("sourceStartTime"));
    assertEquals("WorkCalendar", result.getArguments().get("targetCalendar"));
    assertEquals("2025-11-05T09:00", result.getArguments().get("targetStartTime"));
  }

  @Test
  public void testInvalidSyntaxEventCopyCommand() {
    try {
      String input = "copy event TeamMeeting"
          + " on 2025-11-04T10:30 --target WorkCalendar";
      result = betterParser.parse(input);
      fail("invalid syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testInvalidDateTimeEventCopyCommand() {
    try {
      String input = "copy event TeamMeeting "
          + "on 2025-11-4T10:30 --target WorkCalendar to 2025-11-5T09:00";
      betterParser.parse(input);
      fail("invalid date-time format exception expected");
    } catch (IllegalArgumentException e) {
      assertTrue(
          "Exception message should indicate invalid date-time format",
          e.getMessage().contains("Invalid date-time format")
      );
    }
  }


  @Test
  public void testEventRangeCopyCommand() {
    result = betterParser.parse("copy events on 2025-11-04 --target WorkCalendar to 2025-11-10");
    assertEquals(CommandType.COPY_EVENTS, result.getCommandType());
    assertEquals("2025-11-04", result.getArguments().get("startDate"));
    assertEquals("2025-11-04", result.getArguments().get("endDate"));
    assertEquals("WorkCalendar", result.getArguments().get("targetCalendar"));
    assertEquals("2025-11-10", result.getArguments().get("targetStartDate"));
  }

  @Test
  public void testInvalidSyntaxEventRangeCopyCommand() {
    try {
      String input = "copy events on 2025-11-04 --target "
          + "WorkCalendar";
      result = betterParser.parse(input);
      fail("invalid syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testInvalidDateEventRangeCopyCommand() {
    try {
      String input = "copy events on 2025-28-4 --target "
          + "WorkCalendar to 2025-11-04";
      betterParser.parse(input);
      fail("invalid date-time format exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testEventBtwRangeCopyCommand() {
    result = betterParser.parse("copy events between 2025-11-01 "
        + "and 2025-11-05 --target Personal to 2025-11-15");
    assertEquals(CommandType.COPY_EVENTS, result.getCommandType());
    assertEquals("2025-11-01", result.getArguments().get("startDate"));
    assertEquals("2025-11-05", result.getArguments().get("endDate"));
    assertEquals("Personal", result.getArguments().get("targetCalendar"));
    assertEquals("2025-11-15", result.getArguments().get("targetStartDate"));
  }

  @Test
  public void testInvalidSyntaxEventBtwRangeCopyCommand() {
    try {
      String input = "copy events between 2025-11-01 "
          + "and 2025-11-05";
      result = betterParser.parse(input);
      fail("invalid syntax exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testInvalidDateEventBtwRangeCopyCommand() {
    try {
      String input = "copy events between 2025-11-1 "
          + "and 2025-2-05 --target Personal to 2025-2-15";
      betterParser.parse(input);
      fail("invalid date-time format exception expected");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }


}
