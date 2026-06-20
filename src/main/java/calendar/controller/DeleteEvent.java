package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Represents a command that deletes a single event within the currently active calendar.
 */
class DeleteEvent {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a new {@code DeleteEvent} command with the given calendar model,
   * view, and parsed command.
   *
   * @param calModel the calendar model managing multiple calendars; must not be {@code null}.
   * @param calView  the view used to display messages to the user; must not be {@code null}.
   * @param command  the parsed user command containing the event details; must not be {@code null}.
   */
  DeleteEvent(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the delete operation on a single event within the active calendar.
   *
   * @throws IllegalArgumentException if any argument is missing or invalid, or no event matches.
   */
  void execute() throws IllegalArgumentException {
    SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
    activeCalendar.deleteEvent(
        command.getArguments().get("subject"),
        getDateTimeProp("start"),
        getDateTimeProp("end"));
    calView.displayMessage("Event Deleted successfully!" + System.lineSeparator());
  }

  /**
   * Retrieves and parses a date-time argument from the parsed command.
   *
   * @param property the name of the date-time argument to retrieve.
   * @return the parsed {@link LocalDateTime} value.
   * @throws IllegalArgumentException if the value is missing or not a valid date-time.
   */
  private LocalDateTime getDateTimeProp(String property) throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(command.getArguments().get(property));
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid date-time");
    }
  }
}
