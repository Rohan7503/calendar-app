package calendar.controller;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalGuiInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This controller mediates the interaction between the GUI and model. It implements the
 * {@code Features} interface and defines the behaviour for the callback functions that
 * will be called by a view using this controller.
 */
public class GuiControllerImpl implements Features {
  private final MultiCalModelInterface model;
  private final CalGuiInterface view;

  /**
   * Construct the GUI controller with the model and the GUI view.
   *
   * @param model the model of the application
   * @param view the GUI of the application
   */
  public GuiControllerImpl(MultiCalModelInterface model, CalGuiInterface view) {
    this.model = model;
    this.view = view;
  }

  @Override
  public void createCalendar(String name, String timezone) {

  }

  @Override
  public void selectCalendar(String name) {

  }

  @Override
  public void requestEventsForDay(String date) {

  }

  @Override
  public void createEvent(String subject, String start, String end) {

  }

  @Override
  public void createEventSeriesByCount(String subject, String start, String end,
                                       String weekdays, String count) {

  }

  @Override
  public void createEventSeriesUntilDate(String subject, String start, String end,
                                         String weekdays, String untilDate) {

  }

  @Override
  public void editEvent(String property, String subject, String start, String end,
                        String newValue) {

  }

  @Override
  public void editEvents(String property, String subject, String start, String newValue,
                         boolean editWholeSeries) {

  }
}
