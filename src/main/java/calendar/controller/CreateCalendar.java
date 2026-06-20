package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Represents a command that create calendar.
 *
 */
class CreateCalendar {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a {@code createCalendar} command with the given model,
   * view, and parsed command data.
   *
   * @param calModel the multi-calendar model used to perform the copy operation.
   * @param calView  the view interface used to display messages to the user.
   * @param command  the parsed command containing all necessary arguments.
   */
  CreateCalendar(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the create calendar command by parsing the provided arguments and delegating
   * the event copy operation to the calendar model.
   *
   * @throws DateTimeException        if the provided timeZone string cannot be parsed.
   * @throws IllegalArgumentException if required arguments are missing or invalid.
   */
  void execute() throws IllegalArgumentException {
    try {
      ZoneId zoneId = ZoneId.of(command.getArguments().get("timezone"));
      calModel.createCalendar(command.getArguments().get("calName"), zoneId);
      calView.displayMessage("Calendar created successfully" + System.lineSeparator());
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid Zone");
    }
  }
}
