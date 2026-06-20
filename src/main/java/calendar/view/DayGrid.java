package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * Render-only day view: a single, wider {@link DayColumn} for the anchor day. Navigation and event
 * loading are handled by the surrounding {@link CalendarSurfacePanel}.
 */
class DayGrid extends JPanel {

  private final transient Consumer<LocalDate> onDaySelected;
  private LocalDate day = LocalDate.now();
  private DayColumn column;

  /**
   * Creates a day grid.
   *
   * @param onDaySelected callback invoked with the date when the column is clicked
   */
  DayGrid(Consumer<LocalDate> onDaySelected) {
    this.onDaySelected = onDaySelected;
    setLayout(new BorderLayout());
    setBackground(Theme.BACKGROUND);
    setBorder(UiFactory.padding(1));
  }

  /**
   * Returns the displayed day.
   *
   * @return the day shown
   */
  LocalDate getDay() {
    return day;
  }

  /**
   * Shows the given day.
   *
   * @param day the day to display
   */
  void showDay(LocalDate day) {
    this.day = day;
    rebuild();
  }

  /**
   * Distributes events into the day column.
   *
   * @param events the events within the visible range
   */
  void setEvents(List<Event> events) {
    column.setEvents(EventGrouping.onDay(events, day));
  }

  private void rebuild() {
    removeAll();
    column = new DayColumn(day, day.equals(LocalDate.now()), onDaySelected);
    column.setSelected(true);
    add(column, BorderLayout.CENTER);
    revalidate();
    repaint();
  }
}
