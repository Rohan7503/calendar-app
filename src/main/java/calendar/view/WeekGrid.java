package calendar.view;

import calendar.model.Event;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * Render-only week view: seven {@link DayColumn}s for the week containing the anchor date. Day
 * selection is delegated to the supplied callback; navigation and event loading are handled by the
 * surrounding {@link CalendarSurfacePanel}.
 */
class WeekGrid extends JPanel {

  private final transient Consumer<LocalDate> onDaySelected;
  private final List<DayColumn> columns = new ArrayList<>();
  private LocalDate weekStart = startOfWeek(LocalDate.now());
  private LocalDate selected;

  /**
   * Creates a week grid.
   *
   * @param onDaySelected callback invoked with the chosen date when a day is selected
   */
  WeekGrid(Consumer<LocalDate> onDaySelected) {
    this.onDaySelected = onDaySelected;
    setLayout(new GridLayout(1, 7, 2, 2));
    setBackground(Theme.BACKGROUND);
  }

  /**
   * Returns the first day of the displayed week.
   *
   * @return the Sunday that begins the displayed week
   */
  LocalDate firstVisibleDay() {
    return weekStart;
  }

  /**
   * Returns the last day of the displayed week.
   *
   * @return the Saturday that ends the displayed week
   */
  LocalDate lastVisibleDay() {
    return weekStart.plusDays(6);
  }

  /**
   * Shows the week containing the given anchor date.
   *
   * @param anchor   any date within the week to display
   * @param selected the day to highlight (may be {@code null})
   */
  void showWeek(LocalDate anchor, LocalDate selected) {
    this.weekStart = startOfWeek(anchor);
    this.selected = selected;
    rebuild();
  }

  /**
   * Updates which day column is highlighted.
   *
   * @param selected the selected day (may be {@code null})
   */
  void setSelected(LocalDate selected) {
    this.selected = selected;
    for (DayColumn column : columns) {
      column.setSelected(column.getDate().equals(selected));
    }
  }

  /**
   * Distributes events across the week's day columns.
   *
   * @param events the events within the visible range
   */
  void setEvents(List<Event> events) {
    for (DayColumn column : columns) {
      column.setEvents(EventGrouping.onDay(events, column.getDate()));
    }
  }

  private void rebuild() {
    removeAll();
    columns.clear();
    LocalDate today = LocalDate.now();
    for (int i = 0; i < 7; i++) {
      LocalDate day = weekStart.plusDays(i);
      DayColumn column = new DayColumn(day, day.equals(today), onDaySelected);
      column.setSelected(day.equals(selected));
      columns.add(column);
      add(column);
    }
    revalidate();
    repaint();
  }

  private static LocalDate startOfWeek(LocalDate date) {
    return date.minusDays(date.getDayOfWeek().getValue() % 7);
  }
}
