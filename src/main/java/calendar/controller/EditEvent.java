package calendar.controller;

import calendar.model.CalModelInterface;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Represents a command that edit one event within the currently active calendar.
 */
class EditEvent {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a new {@code EditEvent} command with the given calendar model,
   * view, and parsed command.
   *
   * @param calModel the calendar model managing multiple calendars;
   *                 must not be {@code null}.
   * @param calView  the view used to display messages to the user;
   *                 must not be {@code null}.
   * @param command  the parsed user command containing the event edit details;
   *                 must not be {@code null}.
   */
  EditEvent(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the edit operation on an event within the active calendar.
   *
   * @throws IllegalArgumentException if any argument is missing or invalid.
   *
   */
  void execute() throws IllegalArgumentException {
    SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
    activeCalendar.editEvent(
        getNonDateTimeProp("property", command),
        getNonDateTimeProp("subject", command),
        getDateTimeProp("start", command),
        getDateTimeProp("end", command),
        getNonDateTimeProp("newValue", command));
    calView.displayMessage("Event Edited successfully!" + System.lineSeparator());
  }

  /**
   * Retrieves the non-date-time property (typically recurrence information)
   * from the parsed command.
   *
   * @param parsedCommand the parsed command containing non-date-time arguments.
   * @return the value of the {@code weekdays} argument, or {@code null} if not provided.
   */
  private String getNonDateTimeProp(String property, ParsedCommand parsedCommand) {
    return parsedCommand.getArguments().get(property);
  }


  /**
   * Retrieves and parses a date-time property from the given {@link ParsedCommand}.
   *
   * @param property      the name of the date-time property to retrieve (e.g., "start" or "end").
   * @param parsedCommand the parsed command containing the property arguments;
   *                      must not be {@code null}.
   * @return the parsed {@link LocalDateTime} value associated
   *         with the given property.
   * @throws IllegalArgumentException if the property value is missing
   *                                  or not in the correct date-time format.
   */
  private LocalDateTime getDateTimeProp(String property, ParsedCommand parsedCommand)
      throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(parsedCommand.getArguments().get(property));
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("invalid date-time");
    }
  }
}
