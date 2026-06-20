package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Represents a command that checks and displays the user's availability status
 * for a specific date and time within the currently active calendar.
 */
class ShowStatus {

  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a new {@code ShowStatus} command with the specified model,
   * view, and parsed command.
   *
   * @param calModel the calendar model that manages multiple calendars;
   *                 must not be {@code null}.
   * @param calView  the view interface responsible for displaying messages to the user;
   *                 must not be {@code null}.
   * @param command  the parsed command containing the date-time to check;
   *                 must not be {@code null}.
   */
  ShowStatus(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the command to check the user's availability at a specific time.
   *
   * @throws IllegalArgumentException if the {@code start} argument is missing or invalid,
   *                                  or if the model encounters an error
   *                                  while checking availability.
   */
  void execute() throws IllegalArgumentException {
    SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
    String msg = activeCalendar.checkAvailability(
        getDateTimeProp(command));
    calView.displayMessage(msg + System.lineSeparator());
  }

  /**
   * Retrieves and parses a date-time property from the given {@link ParsedCommand}.
   *
   * @param parsedCommand the parsed command containing the property arguments;
   *                      must not be {@code null}.
   * @return the parsed {@link LocalDateTime} value associated with the given property.
   * @throws IllegalArgumentException if the property value is missing or
   *                                  not in the correct date-time format.
   */
  private LocalDateTime getDateTimeProp(ParsedCommand parsedCommand)
      throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(parsedCommand.getArguments().get("start"));
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid date-time");
    }
  }
}
