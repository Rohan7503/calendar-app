package calendar.view;

import calendar.model.Event;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Renders a compact, color-coded chip summarizing a single event. Chips are reused by the month
 * grid and are intended to be reused by future week/day views, so the rendering lives in one place.
 */
final class EventChip {

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
  private static final int MAX_CHARS = 16;

  private EventChip() {
  }

  /**
   * Creates a chip label for the given event, with a tooltip carrying the full description.
   *
   * @param event the event to render
   * @return a styled label representing the event
   */
  static JLabel create(Event event) {
    String text = event.isAllDay()
        ? event.getSubject()
        : TIME.format(event.getStart()) + " " + event.getSubject();

    JLabel chip = new JLabel(truncate(text));
    chip.setOpaque(true);
    chip.setBackground(event.isAllDay() ? Theme.SELECTED : Theme.ACCENT_SOFT);
    chip.setForeground(event.isAllDay() ? Theme.TEXT : Theme.ACCENT);
    chip.setFont(Theme.SMALL);
    chip.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
    chip.setToolTipText(fullText(event));
    return chip;
  }

  private static String truncate(String text) {
    return text.length() <= MAX_CHARS ? text : text.substring(0, MAX_CHARS - 1) + "…";
  }

  private static String fullText(Event event) {
    if (event.isAllDay()) {
      return event.getSubject() + " (all day)";
    }
    return String.format("%s (%s–%s)", event.getSubject(),
        TIME.format(event.getStart()), TIME.format(event.getEnd()));
  }
}
