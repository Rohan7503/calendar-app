package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Represents a command that copies an event from one calendar and start time
 * to another calendar and start time within the multi-calendar system.
 */
public class CopyEvent {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a {@code CopyEvent} command with the given model,
   * view, and parsed command data.
   *
   * @param calModel the multi-calendar model used to perform the copy operation.
   * @param calView  the view interface used to display messages to the user.
   * @param command  the parsed command containing all necessary arguments.
   */
  CopyEvent(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the copy event command by parsing the provided arguments and delegating
   * the event copy operation to the calendar model.
   *
   * @throws DateTimeException        if the provided date-time strings cannot be parsed.
   * @throws IllegalStateException    if the target calendar or source event is in an invalid state.
   * @throws IllegalArgumentException if required arguments are missing or invalid.
   */
  void execute() throws IllegalStateException, IllegalArgumentException {
    try {
      LocalDateTime sourceStartTime = LocalDateTime.parse(
          command.getArguments().get("sourceStartTime"));
      LocalDateTime targetStartTime = LocalDateTime.parse(
          command.getArguments().get("targetStartTime"));
      calModel.copyEvent(command.getArguments().get("eventName"),
          sourceStartTime,
          command.getArguments().get("targetCalendar"),
          targetStartTime);
      calView.displayMessage("Event copied successfully" + System.lineSeparator());
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("invalid date-time");
    }
  }
}
