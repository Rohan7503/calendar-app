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
 * Represents a command responsible for creating a new calendar event.
 */
class CreateEventCommand {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a {@code CreateEventCommand} with the specified model, view, and parsed command.
   *
   * @param calModel the calendar model used to store and manage event data;
   *                 must not be {@code null}.
   * @param calView  the view interface used to display messages or errors to the user;
   *                 must not be {@code null}.
   * @param command  the parsed command containing user-provided event details such as
   *                 title, date, time, and other attributes; must not be {@code null}.
   */
  CreateEventCommand(MultiCalModelInterface calModel,
                     CalViewInterface calView,
                     ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the event creation command.
   *
   * @throws IllegalArgumentException if the provided event data is invalid,
   *                                  such as an improperly formatted date/time
   *                                  or missing required fields
   *
   */
  void execute() throws IllegalArgumentException {
    SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
    activeCalendar.addEvent(createEvents(command));
    calView.displayMessage("Event Created successfully!" + System.lineSeparator());
  }

  /**
   * Creates an {@link Event} object from the given parsed command.
   *
   * @param parsedCommand the parsed command containing user-specified event details;
   *                      must include at least a {@code subject} and a {@code date}.
   * @return a fully constructed {@link Event} object ready to be added to the calendar.
   * @throws IllegalArgumentException if the command arguments are missing required fields
   *                                  or contain invalid data.
   */
  private Event createEvents(ParsedCommand parsedCommand) throws IllegalArgumentException {
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
  }

  /**
   * Converts a date string from the parsed command into a default all-day
   * event time range.
   *
   * @param parsedCommand the parsed command containing a {@code date} argument.
   * @return a list containing two {@link LocalDateTime} objects:
   *         the start (08:00) and end (17:00) times of the event.
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
}
