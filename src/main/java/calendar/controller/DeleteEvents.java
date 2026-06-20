package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Represents a command that deletes one or more events within the currently active calendar.
 * Depending on the parsed command, this deletes either an entire series or the matched event
 * and all later events in its series.
 */
class DeleteEvents {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a new {@code DeleteEvents} command with the given calendar model,
   * view, and parsed command.
   *
   * @param calModel the calendar model managing multiple calendars; must not be {@code null}.
   * @param calView  the view used to display messages to the user; must not be {@code null}.
   * @param command  the parsed user command containing the event details; must not be {@code null}.
   */
  DeleteEvents(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the delete operation on one or more events within the active calendar.
   *
   * @throws IllegalArgumentException if any argument is missing or invalid, or no event matches.
   */
  void execute() throws IllegalArgumentException {
    SingleCalModelInterface activeCalendar = calModel.getActiveCalendar();
    activeCalendar.deleteEvents(
        command.getArguments().get("subject"),
        getStart(),
        Boolean.parseBoolean(command.getArguments().get("deleteWholeSeries")));
    calView.displayMessage("Events Deleted successfully!" + System.lineSeparator());
  }

  /**
   * Retrieves and parses the start date-time argument from the parsed command.
   *
   * @return the parsed {@link LocalDateTime} value.
   * @throws IllegalArgumentException if the value is missing or not a valid date-time.
   */
  private LocalDateTime getStart() throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(command.getArguments().get("start"));
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid date-time");
    }
  }
}
