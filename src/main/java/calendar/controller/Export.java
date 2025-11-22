package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalViewInterface;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for handling the export command.
 * Calendar data is retrieved from the model and then exported to the given file.
 * The correct export method is called based on the extension of the filepath string passed to it.
 */
public class Export {
  private final MultiCalModelInterface calModel;
  private final CalViewInterface calView;
  private final ParsedCommand command;

  /**
   * Initialize the object with the model, view and command.
   *
   * @param calModel The model to get calendar data from
   * @param calView  The view to pass success/failure messages to
   * @param command  The parsed command
   */
  Export(MultiCalModelInterface calModel,
         CalViewInterface calView,
         ParsedCommand command) {
    this.calModel = calModel;
    this.calView = calView;
    this.command = command;
  }

  /**
   * Retrieve calendar data from the model and export it to the filepath in the
   * command object.
   */
  void execute() {
    SingleCalModelInterface cal = calModel.getActiveCalendar();
    List<Event> events = cal.getAllEvents();
    String filepath = command.getArguments().get("path");
    handleExport(events, filepath);
  }

  /**
   * Helper method to export a list of events to the given filepath.
   *
   * @param events   The events to export
   * @param filename The filepath to export to
   */
  private void handleExport(List<Event> events, String filename) {
    if (events.isEmpty()) {
      calView.displayMessage("No events found.");
      return;
    }
    if (filename.endsWith(".csv")) {
      exportToCsv(events, filename);
    }
    if (filename.endsWith(".ics")) {
      exportToIcal(events, filename);
    }
  }

  /**
   * Helper method to export the given events to a .csv file.
   *
   * @param events   The events to export
   * @param filename The .csv file to export the events to
   */
  private void exportToCsv(List<Event> events, String filename) {
    Path filePath = Paths.get(filename).toAbsolutePath();
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
      writer.println(
          "Subject,Start Date,Start Time,End Date,"
              + "End Time,All Day Event,Description,Location,Private");

      for (Event e : events) {
        String subject = escapeCsv(e.getSubject());
        LocalDateTime start = e.getStart();
        LocalDateTime end = e.getEnd();

        boolean isAllDay = start.toLocalTime().equals(LocalTime.of(8, 0))
            && end.toLocalTime().equals(LocalTime.of(17, 0));

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");
        String startDate = start.toLocalDate().format(dateFmt);
        String endDate = end.toLocalDate().format(dateFmt);
        String startTime = isAllDay ? "" : start.toLocalTime().format(timeFmt);
        String endTime = isAllDay ? "" : end.toLocalTime().format(timeFmt);
        String description = escapeCsv(e.getDescription() == null ? "" : e.getDescription());
        String location = (e.getLocation() == null) ? "" : e.getLocation().toString();
        String allDay = isAllDay ? "True" : "False";
        String isPrivate = (e.getStatus() == EventStatus.PRIVATE) ? "True" : "False";

        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
            subject, startDate, startTime, endDate, endTime,
            allDay, description, location, isPrivate);
      }
      calView.displayMessage("Calendar successfully exported to: " + filePath);
    } catch (IOException ex) {
      calView.displayError("Error writing to file: " + ex.getMessage());
    }
  }

  /**
   * Helper method to export the given events to a .ics file.
   *
   * @param events The events to export.
   * @param filename The .ics file to export the events to.
   */
  private void exportToIcal(List<Event> events, String filename) {
    Path filePath = Paths.get(filename).toAbsolutePath();
    DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd");

    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
      writer.println("BEGIN:VCALENDAR");
      writer.println("VERSION:2.0");
      writer.println("PRODID:-//MyCalendarApp//EN");
      for (Event e : events) {
        writer.println("BEGIN:VEVENT");
        String uid = UUID.randomUUID() + "@calendar";
        writer.println("UID:" + uid);
        writer.println("DTSTAMP:" + LocalDateTime.now().format(dateTimeFmt) + "Z");

        LocalDateTime start = e.getStart();
        LocalDateTime end = e.getEnd();
        boolean isAllDay = start.toLocalTime().equals(LocalTime.of(8, 0))
            && end.toLocalTime().equals(LocalTime.of(17, 0));
        if (isAllDay) {
          writer.println("DTSTART;VALUE=DATE:" + start.toLocalDate().format(dateFmt));
          writer.println("DTEND;VALUE=DATE:" + end.toLocalDate().plusDays(1).format(dateFmt));
        } else {
          writer.println("DTSTART:" + start.format(dateTimeFmt));
          writer.println("DTEND:" + end.format(dateTimeFmt));
        }
        writer.println("SUMMARY:" + escapeIcalText(e.getSubject()));
        if (e.getDescription() != null) {
          writer.println("DESCRIPTION:" + escapeIcalText(e.getDescription()));
        }
        if (e.getLocation() != null) {
          writer.println("LOCATION:" + escapeIcalText(e.getLocation().toString()));
        }
        if (e.getStatus() != null) {
          writer.println("CLASS:" + e.getStatus().name());
        }
        writer.println("END:VEVENT");
      }
      writer.println("END:VCALENDAR");
      calView.displayMessage("Calendar successfully exported to: " + filePath);
    } catch (IOException ex) {
      calView.displayError("Error writing to file: " + ex.getMessage());
    }
  }

  /**
   * Helper method that escapes a string for safe use in a CSV file.
   * If the value contains commas or quotes, wraps it in quotes and doubles any internal quotes.
   *
   * @param value the string to escape.
   * @return the CSV-safe version of the string.
   */
  private String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }

  /**
   * Escapes text fields for iCalendar format.
   * Replaces line breaks with \n and escapes commas and semicolons.
   *
   * @param value the string to escape.
   * @return escaped string suitable for .ics
   */
  private String escapeIcalText(String value) {
    return value
        .replace("\\", "\\\\")
        .replace(System.lineSeparator(), "\\n")
        .replace(",", "\\,")
        .replace(";", "\\;");
  }
}
