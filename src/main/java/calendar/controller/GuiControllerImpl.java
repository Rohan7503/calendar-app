package calendar.controller;

import calendar.model.Event;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalGuiInterface;
import calendar.view.CalViewInterface;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * This controller mediates the interaction between the GUI and model. It implements the
 * {@code Features} interface and defines the behaviour for the callback functions that
 * will be called by a view using this controller.
 */
public class GuiControllerImpl implements Features {
  private static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime ALL_DAY_END = LocalTime.of(17, 0);

  private final MultiCalModelInterface model;
  private final CalGuiInterface view;
  private SingleCalModelInterface activeCalendar;

  /**
   * Construct the GUI controller with the model and the GUI view.
   *
   * @param model the model of the application
   * @param view  the GUI of the application
   */
  public GuiControllerImpl(MultiCalModelInterface model, CalGuiInterface view) {
    this.model = model;
    this.view = view;
    this.activeCalendar = null;
  }

  @Override
  public void createCalendar(String name, String timezone) {
    try {
      ZoneId zoneId = ZoneId.of(timezone);
      model.createCalendar(name, zoneId);
      model.useCalendar(name);
      List<String> calNames = model.listCalendars();
      view.showMessage("Successfully created calendar - " + name);
      view.showCalendars(calNames, name);
      view.refreshEvents();
    } catch (DateTimeException e) {
      view.showError("Invalid time zone");
    } catch (IllegalArgumentException e) {
      view.showError((e.getMessage()));
    }
  }

  @Override
  public void selectCalendar(String name) {
    try {
      model.useCalendar(name);
      activeCalendar = model.getActiveCalendar();
      List<String> calNames = model.listCalendars();
      view.showCalendars(calNames, name);
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void requestEventsForDay(String date) {
    try {
      activeCalendar = model.getActiveCalendar();
      LocalDate startDate = LocalDate.parse(date);
      LocalTime startTime = LocalTime.parse("00:00");
      LocalTime endTime = LocalTime.parse("00:00");
      LocalDateTime start = LocalDateTime.of(startDate, startTime);
      LocalDateTime end = LocalDateTime.of(startDate.plusDays(1), endTime);
      List<Event> events = activeCalendar.getEventsInRange(start, end);
      view.showEventsForDay(startDate, events);
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void createEvent(String subject, String start, String end) {
    try {
      activeCalendar = model.getActiveCalendar();
      activeCalendar.addEvent(createEvents(subject, start, end));
      view.showMessage("Event Created successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void createAllDayEvent(String subject, String date) {
    try {
      activeCalendar = model.getActiveCalendar();
      LocalDate day = LocalDate.parse(date);
      Event event = Event.getBuilder()
          .subject(subject)
          .start(day.atTime(ALL_DAY_START))
          .end(day.atTime(ALL_DAY_END))
          .allDay(true)
          .build();
      activeCalendar.addEvent(event);
      view.showMessage("Event Created successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date");
    }
  }

  @Override
  public void createEventSeriesByCount(String subject, String start, String end,
                                       String weekdays, String count) {
    try {
      SingleCalModelInterface activeCalendar = model.getActiveCalendar();
      activeCalendar.addEventSeriesForCount(
          createEvents(subject, start, end),
          weekdays,
          Integer.parseInt(count));
      view.showMessage("Series Created successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      if (e instanceof NumberFormatException) {
        view.showError("Invalid count format");
      } else {
        view.showError(e.getMessage());
      }
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void createEventSeriesUntilDate(String subject, String start, String end,
                                         String weekdays, String untilDate) {
    try {
      LocalDate until = LocalDate.parse(untilDate);
      SingleCalModelInterface activeCalendar = model.getActiveCalendar();
      activeCalendar.addEventSeriesUntilDate(
          createEvents(subject, start, end),
          weekdays,
          until);
      view.showMessage("Series Created successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid Date-time");
    }
  }

  @Override
  public void editEvent(String property, String subject, String start, String end,
                        String newValue) {
    try {
      SingleCalModelInterface activeCalendar = model.getActiveCalendar();
      activeCalendar.editEvent(
          property,
          subject,
          LocalDateTime.parse(start),
          LocalDateTime.parse(end),
          newValue);
      view.showMessage("Event Edited successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void editEvents(String property, String subject, String start, String newValue,
                         boolean editWholeSeries) {
    try {
      SingleCalModelInterface activeCalendar = model.getActiveCalendar();
      activeCalendar.editEvents(
          property,
          subject,
          LocalDateTime.parse(start),
          newValue,
          editWholeSeries);
      view.showMessage("Events Edited successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }

  }


  @Override
  public void deleteEvent(String subject, String start, String end) {
    try {
      activeCalendar = model.getActiveCalendar();
      activeCalendar.deleteEvent(
          subject,
          LocalDateTime.parse(start),
          LocalDateTime.parse(end));
      view.showMessage("Event Deleted successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void deleteEvents(String subject, String start, boolean deleteWholeSeries) {
    try {
      activeCalendar = model.getActiveCalendar();
      activeCalendar.deleteEvents(
          subject,
          LocalDateTime.parse(start),
          deleteWholeSeries);
      view.showMessage("Events Deleted successfully!" + System.lineSeparator());
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void editCalendar(String name, String property, String newValue) {
    try {
      model.editCalendar(name, property, newValue);
      String highlight = property.equalsIgnoreCase("name") ? newValue : name;
      view.showMessage("Calendar updated successfully");
      view.showCalendars(model.listCalendars(), highlight);
      view.refreshEvents();
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void exportCalendar(String path) {
    ParsedCommand command = new ParsedCommand(CommandType.EXPORT,
        Collections.singletonMap("path", path));
    new Export(model, new GuiViewAdapter(), command).execute();
  }

  @Override
  public void copyEvents(String startDate, String endDate, String targetCalendar,
                         String targetStartDate) {
    try {
      model.copyEventsBetween(LocalDate.parse(startDate), LocalDate.parse(endDate),
          targetCalendar, LocalDate.parse(targetStartDate));
      view.showMessage("Events copied successfully");
      view.refreshEvents();
    } catch (IllegalArgumentException | IllegalStateException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date");
    }
  }

  @Override
  public void showStatus(String dateTime) {
    try {
      activeCalendar = model.getActiveCalendar();
      String status = activeCalendar.checkAvailability(LocalDateTime.parse(dateTime));
      view.showMessage("Status: " + status);
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void requestEventsInRange(String start, String end) {
    try {
      activeCalendar = model.getActiveCalendar();
      List<Event> events = activeCalendar.getEventsInRange(
          LocalDateTime.parse(start), LocalDateTime.parse(end));
      view.showEventsInRange("Events from " + start + " to " + end, events);
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  @Override
  public void requestMonthView(String startDate, String endDate) {
    try {
      activeCalendar = model.getActiveCalendar();
      List<Event> events = activeCalendar.getEventsInRange(
          LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
      view.showMonthEvents(events);
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    } catch (DateTimeException e) {
      view.showError("Invalid date-time");
    }
  }

  //********************** Helper methods *******************************//

  /**
   * Adapts the GUI view to the {@link CalViewInterface} expected by the shared {@link Export}
   * command, forwarding messages and errors to the GUI's feedback. {@code displayEvents} is unused
   * by the export path.
   */
  private final class GuiViewAdapter implements CalViewInterface {
    @Override
    public void displayMessage(String msg) {
      view.showMessage(msg);
    }

    @Override
    public void displayError(String msg) {
      view.showError(msg);
    }

    @Override
    public void displayEvents(List<Event> events) {
      // Not used by export; range results are rendered via showEventsInRange.
    }
  }


  /**
   * Creates an {@link Event} object from the given parsed command.
   *
   * @param subject the subject of the event.
   * @param start   the start date-time of the event.
   * @param end     the end date-time of the event.
   * @return the event object.
   * @throws IllegalArgumentException if the command arguments are missing required fields
   *                                  or contain invalid data.
   */
  private Event createEvents(String subject, String start, String end)
      throws IllegalArgumentException {
    LocalDateTime startTime = LocalDateTime.parse(start);
    LocalDateTime endTime = LocalDateTime.parse(end);
    return Event.getBuilder()
        .start(startTime)
        .end(endTime)
        .subject(subject)
        .build();
  }

}
