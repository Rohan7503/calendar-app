package calendar.view;

import calendar.model.Event;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Render-only month grid: a six-week grid of {@link DayCell}s for a given month, showing event
 * chips per day. Day selection is delegated to the supplied callback; navigation and event loading
 * are handled by the surrounding {@link CalendarSurfacePanel}.
 */
class MonthGrid extends JPanel {

  private static final String[] WEEKDAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final int WEEKS = 6;

  private final transient Consumer<LocalDate> onDaySelected;
  private final Map<LocalDate, DayCell> cells = new HashMap<>();
  private YearMonth month = YearMonth.now();
  private LocalDate selected;

  /**
   * Creates a month grid.
   *
   * @param onDaySelected callback invoked with the chosen date when a day is clicked
   */
  MonthGrid(Consumer<LocalDate> onDaySelected) {
    this.onDaySelected = onDaySelected;
    setLayout(new GridLayout(0, 7, 2, 2));
    setBackground(Theme.BACKGROUND);
  }

  /**
   * Returns the first day shown in the grid (which may belong to the previous month).
   *
   * @return the first visible date
   */
  LocalDate firstVisibleDay() {
    LocalDate firstOfMonth = month.atDay(1);
    return firstOfMonth.minusDays(firstOfMonth.getDayOfWeek().getValue() % 7);
  }

  /**
   * Returns the last day shown in the grid (which may belong to the next month).
   *
   * @return the last visible date
   */
  LocalDate lastVisibleDay() {
    return firstVisibleDay().plusDays(WEEKS * 7 - 1L);
  }

  /**
   * Sets the month to display and rebuilds the grid.
   *
   * @param month    the month to show
   * @param selected the day to highlight as selected (may be {@code null})
   */
  void showMonth(YearMonth month, LocalDate selected) {
    this.month = month;
    this.selected = selected;
    rebuild();
  }

  /**
   * Updates which day is highlighted as selected.
   *
   * @param selected the selected day (may be {@code null})
   */
  void setSelected(LocalDate selected) {
    this.selected = selected;
    for (Map.Entry<LocalDate, DayCell> entry : cells.entrySet()) {
      entry.getValue().setSelected(entry.getKey().equals(selected));
    }
  }

  /**
   * Distributes events onto the visible day cells as chips.
   *
   * @param events the events within the visible range
   */
  void setEvents(List<Event> events) {
    Map<LocalDate, List<Event>> byDay = EventGrouping.byStartDate(events);
    for (Map.Entry<LocalDate, DayCell> entry : cells.entrySet()) {
      entry.getValue().setEvents(byDay.getOrDefault(entry.getKey(), List.of()));
    }
  }

  private void rebuild() {
    removeAll();
    cells.clear();

    for (String name : WEEKDAY_NAMES) {
      JLabel label = new JLabel(name, SwingConstants.CENTER);
      label.setFont(Theme.SMALL);
      label.setForeground(Theme.MUTED_TEXT);
      add(label);
    }

    LocalDate today = LocalDate.now();
    LocalDate cursor = firstVisibleDay();
    for (int i = 0; i < WEEKS * 7; i++) {
      boolean inMonth = YearMonth.from(cursor).equals(month);
      DayCell cell = new DayCell(cursor, inMonth, cursor.equals(today), onDaySelected);
      cell.setSelected(cursor.equals(selected));
      cells.put(cursor, cell);
      add(cell);
      cursor = cursor.plusDays(1);
    }
    revalidate();
    repaint();
  }
}
