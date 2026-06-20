package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewInterface;

/**
 * Represents a command responsible for editing properties of an existing calendar.
 */
public class EditCalendar {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Constructs an {@code EditCalendar} command with the specified model, view, and parsed command.
   *
   * @param calModel the calendar model interface that manages calendar data;
   *                 must not be {@code null}
   * @param calView  the view interface used to display messages or errors to the user;
   *                 must not be {@code null}
   * @param command  the parsed command containing details such as the calendar name,
   *                 property to edit, and new value; must not be {@code null}
   */
  EditCalendar(MultiCalModelInterface calModel,
               CalViewInterface calView,
               ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Executes the calendar editing command.
   *
   * @throws IllegalArgumentException if the specified calendar name, property name,
   *                                  or new value is invalid or if the operation fails
   */
  void execute() throws IllegalArgumentException {
    calModel.editCalendar(command.getArguments().get("calName"),
        command.getArguments().get("propertyName"),
        command.getArguments().get("newValue"));
    calView.displayMessage("Calendar Edited Successfully" + System.lineSeparator());
  }
}
