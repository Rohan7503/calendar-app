package calendar.controller;

import calendar.model.CalModelInterface;
import calendar.model.Event;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command that retrieves and displays events from the currently active calendar
 * within a specified date-time range.
 */
class GetEvents {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;
  private List<Event> events;

  /**
   * Constructs a new {@code GetEvents} command with the given calendar model,
   * view, and parsed command.
   *
   * @param calModel the calendar model managing multiple calendars;
   *                 must not be {@code null}
   * @param calView  the view responsible for displaying results to the user;
   *                 must not be {@code null}
   * @param command  the parsed command containing event query parameters;
   *                 must not be {@code null}
   */
  GetEvents(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the command to retrieve and display events from the active calendar
   * within the specified date-time range.
   *
   * @throws IllegalArgumentException if invalid arguments are provided or
   *                                  if the date-time format cannot be parsed
   */
  void execute() throws IllegalArgumentException {
    try {
      SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
      if (!command.getArguments().containsKey("start")) {
        List<LocalDateTime> dateTimes = localeDateTimeConverter(command);
        events = activeCalendar.getEventsInRange(dateTimes.get(0), dateTimes.get(1));
      } else {
        events = activeCalendar.getEventsInRange(
            getDateTimeProp("start", command),
            getDateTimeProp("end", command));
      }
      calView.displayEvents(events);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Retrieves and parses a date-time property from the given {@link ParsedCommand}.
   *
   * @param property      the name of the date-time property to retrieve (e.g., "start" or "end").
   * @param parsedCommand the parsed command containing the property arguments;
   *                      must not be {@code null}.
   * @return the parsed {@link LocalDateTime} value associated with the given property.
   * @throws IllegalArgumentException if the property value is missing or
   *                                  not in the correct date-time format.
   */
  private LocalDateTime getDateTimeProp(String property, ParsedCommand parsedCommand) {
    return LocalDateTime.parse(parsedCommand.getArguments().get(property));
  }

  /**
   * Converts a date string from the parsed command into a default all-day
   * event time range.
   *
   * @param parsedCommand the parsed command containing a {@code date} argument.
   * @return a list containing two {@link LocalDateTime} objects:
   *        the start (08:00) and end (17:00) times of the event.
   * @throws IllegalArgumentException if the provided date string is invalid
   *                                  or cannot be parsed into a {@link LocalDate}.
   */
  private List<LocalDateTime> localeDateTimeConverter(ParsedCommand parsedCommand)
      throws IllegalArgumentException {
    try {
      LocalDate startDate = LocalDate.parse(parsedCommand.getArguments().get("date"));
      LocalTime startTime = LocalTime.parse("00:00");
      LocalTime endTime = LocalTime.parse("00:00");
      List<LocalDateTime> dateTime = new ArrayList<>();
      dateTime.add(LocalDateTime.of(startDate, startTime));
      dateTime.add(LocalDateTime.of(startDate.plusDays(1), endTime));
      return dateTime;
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid date-time");
    }
  }
}
