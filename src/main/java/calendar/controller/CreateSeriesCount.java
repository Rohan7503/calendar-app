package calendar.controller;

import calendar.model.CalModelInterface;
import calendar.model.Event;
import calendar.model.MultiCalModelImpl;
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
 * Represents a command responsible for creating a recurring series of calendar events
 * based on a specified count.
 */
class CreateSeriesCount {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;
  private final int count;

  /**
   * Constructs a {@code CreateSeriesCount} command with the specified model,
   * view, and parsed command.
   *
   * @param calModel the calendar model used to store and manage event data;
   *                 must not be {@code null}.
   * @param calView  the view interface used to display messages or errors to the user;
   *                 must not be {@code null}.
   * @param command  the parsed command containing user-provided series details such as
   *                 subject, date, count, and weekdays; must not be {@code null}.
   */
  CreateSeriesCount(MultiCalModelInterface calModel,
                    CalViewInterface calView,
                    ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
    this.count = Integer.parseInt(command.getArguments().get("count"));
  }

  /**
   * Executes the event series creation command.
   *
   * @throws IllegalArgumentException if the command arguments are invalid, missing required fields,
   *                                  or contain unparsable date/time values.
   */
  void execute() throws IllegalArgumentException {
    try {
      SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
      activeCalendar.addEventSeriesForCount(
          createEvents(command),
          getNonDateTimeProp(command),
          count);
      calView.displayMessage("Series Created successfully!" + System.lineSeparator());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Creates an {@link Event} object from the given parsed command.
   *
   * @param parsedCommand the parsed command containing event details such as subject and date.
   * @return a fully constructed {@link Event} instance.
   * @throws IllegalArgumentException if required arguments are missing or invalid.
   */
  private Event createEvents(ParsedCommand parsedCommand)
      throws IllegalArgumentException {
    try {
      LocalDateTime start;
      LocalDateTime end;
      if (!parsedCommand.getArguments().containsKey("start")) {
        List<LocalDateTime> dateTimeList = localeAllDayConverter(parsedCommand);
        start = dateTimeList.get(0);
        end = dateTimeList.get(1);
      } else {
        start = LocalDateTime.parse(parsedCommand.getArguments().get("start"));
        end = LocalDateTime.parse(parsedCommand.getArguments().get("end"));
      }
      return Event.getBuilder()
          .start(start)
          .end(end)
          .subject(parsedCommand.getArguments().get("subject"))
          .build();
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid Date Time");
    }
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
  private List<LocalDateTime> localeAllDayConverter(ParsedCommand parsedCommand)
      throws IllegalArgumentException {
    try {
      LocalDate startDate = LocalDate.parse(parsedCommand.getArguments().get("date"));
      LocalTime startTime = LocalTime.parse("08:00");
      LocalTime endTime = LocalTime.parse("17:00");
      List<LocalDateTime> dateTime = new ArrayList<>();
      dateTime.add(LocalDateTime.of(startDate, startTime));
      dateTime.add(LocalDateTime.of(startDate, endTime));
      return dateTime;
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid Date");
    }
  }

  /**
   * Retrieves the non-date-time property (typically recurrence information)
   * from the parsed command.
   *
   * @param parsedCommand the parsed command containing non-date-time arguments.
   * @return the value of the {@code weekdays} argument, or {@code null} if not provided.
   */
  private String getNonDateTimeProp(ParsedCommand parsedCommand) {
    return parsedCommand.getArguments().get("weekdays");
  }


}
