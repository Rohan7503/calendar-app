package calendar.view;

import calendar.model.Event;
import java.util.List;

/**
 * Represents the view component of the calendar application.
 * The view is responsible for displaying events and messages
 * to the user and exporting event data when required.
 */
public interface CalViewInterface {

  /**
   * Displays a general informational message to the user.
   *
   * @param msg the message to display
   */
  void displayMessage(String msg);

  /**
   * Displays an error message to the user.
   *
   * @param msg the error message to display
   */
  void displayError(String msg);

  /**
   * Displays a list of events.
   *
   * @param events the events to display
   */
  void displayEvents(List<Event> events);
}
