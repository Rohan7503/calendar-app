package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Represents a command that copies events from one calendar and start time
 * to another calendar and start time within the multi-calendar system.
 */
public class CopyEvents {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs a {@code CopyEvents} command with the given model,
   * view, and parsed command data.
   *
   * @param calModel the multi-calendar model used to perform the copy operation.
   * @param calView  the view interface used to display messages to the user.
   * @param command  the parsed command containing all necessary arguments.
   */
  CopyEvents(MultiCalModelInterface calModel, CalViewInterface calView, ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the copy events command by parsing the provided arguments and delegating
   * the event copy operation to the calendar model.
   *
   * @throws DateTimeException        if the provided date-time strings cannot be parsed.
   * @throws IllegalStateException    if the target calendar or source event is in an invalid state.
   * @throws IllegalArgumentException if required arguments are missing or invalid.
   */
  void execute() throws IllegalStateException, IllegalArgumentException {
    try {
      LocalDate startDate = LocalDate.parse(
          command.getArguments().get("startDate"));
      LocalDate endDate = LocalDate.parse(
          command.getArguments().get("endDate"));
      LocalDate targetStartDate = LocalDate.parse(
          command.getArguments().get("targetStartDate")
      );
      calModel.copyEventsBetween(startDate, endDate,
          command.getArguments().get("targetCalendar"),
          targetStartDate);
      calView.displayMessage("Events Copied Successfully" + System.lineSeparator());
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("invalid Date");
    } catch (IllegalStateException e) {
      throw new IllegalStateException("invalid state");
    }
  }
}
