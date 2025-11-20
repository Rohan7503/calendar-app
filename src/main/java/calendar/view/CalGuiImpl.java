package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JFrame;

/**
 * This class represents a GUI for the calendar application using Java Swing. It displays the
 * list of available calendars, a month-view calendar grid, events on a selected day in the grid,
 * and provides the option to create, edit, or modify events.
 */
public class CalGuiImpl extends JFrame implements CalGuiInterface {
  @Override
  public void showGui() {

  }

  @Override
  public void addFeatures(Features features) {

  }

  @Override
  public void showCalendars(List<String> calendarNames, String activeCalendar) {

  }

  @Override
  public void showEventsForDay(LocalDate day, List<Event> events) {

  }

  @Override
  public void refreshEvents() {

  }

  @Override
  public void showMessage(String message) {

  }

  @Override
  public void showError(String message) {

  }
}
