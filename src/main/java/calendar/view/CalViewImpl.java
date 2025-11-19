package calendar.view;

import calendar.model.Event;
import java.io.PrintStream;
import java.util.List;

/**
 * Implementation of the ICalView interface.
 * Responsible for presenting information to the user, including:
 * - Displaying messages and errors
 * - Displaying lists of events
 * - Exporting calendar events to a file
 */
public class CalViewImpl implements CalViewInterface {
  private final PrintStream out;
  private final PrintStream err;

  /**
   * Construct a view object.
   */
  public CalViewImpl(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  @Override
  public void displayMessage(String msg) {
    out.println(msg);
  }

  @Override
  public void displayError(String msg) {
    err.println("Error: " + msg);
  }

  @Override
  public void displayEvents(List<Event> events) {
    if (!validateEventsList(events)) {
      return;
    }
    displayMessage("Events:");
    for (Event e : events) {
      StringBuilder sb = new StringBuilder();

      sb.append(" - ").append(e.getSubject());
      sb.append(" starting on ")
          .append(e.getStart().toLocalDate().toString())
          .append(" at ")
          .append(e.getStart().toLocalTime().toString());
      sb.append(", ending on ")
          .append(e.getEnd().toLocalDate().toString())
          .append(" at ")
          .append(e.getEnd().toLocalTime().toString());
      if (e.getLocation() != null) {
        sb.append(", Location: ").append(e.getLocation().toString());
      }
      displayMessage(sb.toString());
    }
  }

  /**
   * Helper method to check if a given list of events is valid.
   *
   * @param events The list of events to validate
   * @return True if the list of events are valid, false otherwise
   */
  private boolean validateEventsList(List<Event> events) {
    if (events == null || events.isEmpty()) {
      displayMessage("No events found.");
      return false;
    }
    return true;
  }

}
