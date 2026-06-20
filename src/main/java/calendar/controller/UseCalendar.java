package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;

/**
 * Represents a command that switches the active calendar in the system.
 */
class UseCalendar {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;
  private SingleCalModelInterface activeCalendar;

  /**
   * Constructs a new {@code UseCalendar} command with the given model, view,
   * parsed command, and currently active calendar.
   *
   * @param calModel the calendar model managing multiple calendars;
   *                must not be {@code null}.
   * @param calView  the view used to display messages to the user;
   *                must not be {@code null}.
   * @param command  the parsed command containing the name of the calendar to activate;
   *                must not be {@code null}.
   * @param activeCalendar the currently active calendar instance before switching;
   *                      may be {@code null}.
   */
  UseCalendar(MultiCalModelInterface calModel,
              CalViewInterface calView,
              ParsedCommand command,
              SingleCalModelInterface activeCalendar) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
    this.activeCalendar = activeCalendar;
  }

  /**
   * Executes the command to switch the active calendar.
   *
   * @return the {@link SingleCalModelInterface} representing the newly active calendar.
   * @throws IllegalArgumentException if the calendar name is invalid or the switch operation fails.
   */
  SingleCalModelInterface execute() throws IllegalArgumentException {
    calModel.useCalendar(command.getArguments().get("calName"));
    activeCalendar = calModel.getActiveCalendar();
    calView.displayMessage("Now using calendar : "
        + command.getArguments().get("calName") + System.lineSeparator());
    return activeCalendar;
  }
}
