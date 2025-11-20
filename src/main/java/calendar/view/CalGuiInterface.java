package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import java.time.LocalDate;
import java.util.List;

/**
 * The GUI view for the calendar application. This interface exposes only
 * methods that the controller needs to update what the user sees.
 */
public interface CalGuiInterface {
  /**
   * Make the GUI visible.
   */
  void showGui();

  /**
   * Register the controller callbacks. The view will call these whenever
   * the user performs an action.
   *
   * @param features an implementation of the Features interface.
   */
  void addFeatures(Features features);

  /**
   * Display the list of calendars and highlight the currently active one.
   *
   * @param calendarNames  list of all calendar names
   * @param activeCalendar name of the currently active calendar
   */
  void showCalendars(List<String> calendarNames, String activeCalendar);

  /**
   * Display all events scheduled on a selected day.
   *
   * @param day    the day to display all events for
   * @param events list of events for that day
   */
  void showEventsForDay(LocalDate day, List<Event> events);

  /**
   * Re-fetch the events to be displayed.
   */
  void refreshEvents();

  /**
   * Display a message to the user.
   *
   * @param message the message to display
   */
  void showMessage(String message);

  /**
   * Display an error message to the user.
   *
   * @param message the error message to display
   */
  void showError(String message);
}
