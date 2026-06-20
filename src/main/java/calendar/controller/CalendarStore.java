package calendar.controller;

import calendar.model.Event;
import calendar.model.EventLocation;
import calendar.model.EventStatus;
import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Saves and loads calendars and their events to and from a simple, dependency-free local file.
 *
 * <p>The format is line-based, one record per line, with tab-separated fields and backslash
 * escaping of tabs, newlines, and backslashes within fields. There are three record kinds:
 * {@code CAL} (name, timezone), {@code EVT} (the event fields, applying to the most recently read
 * {@code CAL}), and {@code ACTIVE} (the active calendar name).</p>
 *
 * <p>Parsing is tolerant of a missing or empty file. This class performs IO and parsing only and
 * uses the model's public API, keeping it out of the model layer.</p>
 */
public class CalendarStore {

  private static final String SEP = "\t";

  /**
   * Serializes all calendars, their events, and the active calendar to the store format.
   *
   * @param model the model to serialize
   * @return the serialized text
   */
  public String serialize(MultiCalModelInterface model) {
    StringBuilder sb = new StringBuilder();
    for (String name : model.listCalendars()) {
      SingleCalModelInterface cal = model.getCalendar(name);
      sb.append("CAL").append(SEP).append(escape(name)).append(SEP)
          .append(cal.getTimezone().getId()).append('\n');
      for (Event event : cal.getAllEvents()) {
        sb.append(serializeEvent(event)).append('\n');
      }
    }
    String active = model.getActiveCalendarName();
    if (active != null) {
      sb.append("ACTIVE").append(SEP).append(escape(active)).append('\n');
    }
    return sb.toString();
  }

  /**
   * Populates the given (empty) model from serialized text.
   *
   * @param content the serialized text
   * @param model   the model to populate
   * @throws IllegalArgumentException if the content is malformed
   */
  public void deserializeInto(String content, MultiCalModelInterface model)
      throws IllegalArgumentException {
    String current = null;
    for (String line : content.split("\n", -1)) {
      if (line.isEmpty()) {
        continue;
      }
      String[] parts = line.split(SEP, -1);
      switch (parts[0]) {
        case "CAL":
          model.createCalendar(unescape(parts[1]), ZoneId.of(parts[2]));
          current = unescape(parts[1]);
          break;
        case "EVT":
          model.getCalendar(current).addEvent(parseEvent(parts));
          break;
        case "ACTIVE":
          model.useCalendar(unescape(parts[1]));
          break;
        default:
          throw new IllegalArgumentException("Unrecognized record: " + parts[0]);
      }
    }
  }

  /**
   * Saves the model to the given file, creating parent directories as needed.
   *
   * @param model the model to save
   * @param path  the destination file
   * @throws UncheckedIOException if writing fails
   */
  public void save(MultiCalModelInterface model, Path path) {
    try {
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.write(path, serialize(model).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Loads saved state into the model if the file exists.
   *
   * @param model the model to populate
   * @param path  the source file
   * @return {@code true} if state was loaded, {@code false} if the file was absent
   * @throws UncheckedIOException if reading fails
   */
  public boolean load(MultiCalModelInterface model, Path path) {
    if (!Files.exists(path)) {
      return false;
    }
    try {
      deserializeInto(new String(Files.readAllBytes(path), StandardCharsets.UTF_8), model);
      return true;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private String serializeEvent(Event event) {
    return String.join(SEP,
        "EVT",
        escape(event.getSubject()),
        event.getStart().toString(),
        event.getEnd().toString(),
        Boolean.toString(event.isAllDay()),
        event.getLocation() == null ? "" : event.getLocation().name(),
        event.getStatus() == null ? "" : event.getStatus().name(),
        event.getSeriesId() == null ? "" : escape(event.getSeriesId()),
        event.getDescription() == null ? "" : escape(event.getDescription()));
  }

  private Event parseEvent(String[] parts) {
    Event.EventBuilder builder = Event.getBuilder()
        .subject(unescape(parts[1]))
        .start(LocalDateTime.parse(parts[2]))
        .end(LocalDateTime.parse(parts[3]))
        .allDay(Boolean.parseBoolean(parts[4]));
    if (!parts[5].isEmpty()) {
      builder.location(EventLocation.fromString(parts[5]));
    }
    if (!parts[6].isEmpty()) {
      builder.status(EventStatus.fromString(parts[6]));
    }
    if (!parts[7].isEmpty()) {
      builder.seriesId(unescape(parts[7]));
    }
    if (parts.length > 8 && !parts[8].isEmpty()) {
      builder.description(unescape(parts[8]));
    }
    return builder.build();
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
  }

  private static String unescape(String value) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\\' && i + 1 < value.length()) {
        char next = value.charAt(++i);
        switch (next) {
          case 't':
            out.append('\t');
            break;
          case 'r':
            out.append('\r');
            break;
          case 'n':
            out.append('\n');
            break;
          default:
            out.append(next);
        }
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }

  /**
   * Returns the default location of the saved-state file under the user's home directory.
   *
   * @return the default store path
   */
  public static Path defaultPath() {
    return Path.of(System.getProperty("user.home"), ".calendar-app", "calendars.dat");
  }
}
