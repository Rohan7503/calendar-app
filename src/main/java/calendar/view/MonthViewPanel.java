package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The central calendar surface: a navigable six-week month grid. Each day is a {@link DayCell}
 * showing event chips. Selecting a day highlights it and notifies the day-selected callback;
 * navigating months notifies the month-changed callback so the surrounding shell can load that
 * month's events. This panel owns the selected date, the single source of truth for day actions.
 */
class MonthViewPanel extends JPanel {

  private static final String[] WEEKDAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final int WEEKS = 6;

  private final transient Consumer<LocalDate> onDaySelected;
  private final transient Runnable onMonthChanged;
  private final JPanel grid;
  private final JLabel monthLabel;
  private final Map<LocalDate, DayCell> cells;
  private YearMonth displayedMonth;
  private LocalDate selectedDate;

  /**
   * Constructs the month view.
   *
   * @param onDaySelected  callback invoked with the chosen date when a day is selected
   * @param onMonthChanged callback invoked after the displayed month is (re)built
   */
  MonthViewPanel(Consumer<LocalDate> onDaySelected, Runnable onMonthChanged) {
    this.onDaySelected = onDaySelected;
    this.onMonthChanged = onMonthChanged;
    this.cells = new HashMap<>();
    this.displayedMonth = YearMonth.now();
    this.grid = new JPanel(new GridLayout(0, 7, 2, 2));
    this.grid.setBackground(Theme.BACKGROUND);
    this.monthLabel = new JLabel("", SwingConstants.CENTER);
    this.monthLabel.setFont(Theme.TITLE);

    setLayout(new BorderLayout());
    setBackground(Theme.BACKGROUND);
    setBorder(UiFactory.padding(1));
    add(buildHeader(), BorderLayout.NORTH);
    add(grid, BorderLayout.CENTER);
    rebuild();
  }

  /**
   * Returns the currently selected date, or {@code null} if none is selected.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Returns the month currently displayed.
   *
   * @return the displayed year-month
   */
  YearMonth getDisplayedMonth() {
    return displayedMonth;
  }

  /**
   * Distributes the given events onto the visible day cells as chips.
   *
   * @param events the events within the displayed month
   */
  void setMonthEvents(List<Event> events) {
    Map<LocalDate, List<Event>> byDay = new HashMap<>();
    for (Event event : events) {
      byDay.computeIfAbsent(event.getStart().toLocalDate(), d -> new ArrayList<>()).add(event);
    }
    for (Map.Entry<LocalDate, DayCell> entry : cells.entrySet()) {
      entry.getValue().setEvents(byDay.getOrDefault(entry.getKey(), List.of()));
    }
  }

  /**
   * Navigates to the current month and selects today.
   */
  void selectToday() {
    displayedMonth = YearMonth.now();
    rebuild();
    selectDay(LocalDate.now());
  }

  private JPanel buildHeader() {
    JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, Theme.UNIT, Theme.UNIT));
    header.setBackground(Theme.BACKGROUND);

    JButton prev = UiFactory.secondaryButton("<");
    JButton next = UiFactory.secondaryButton(">");
    JButton today = UiFactory.secondaryButton("Today");
    prev.addActionListener(e -> {
      displayedMonth = displayedMonth.minusMonths(1);
      rebuild();
    });
    next.addActionListener(e -> {
      displayedMonth = displayedMonth.plusMonths(1);
      rebuild();
    });
    today.addActionListener(e -> selectToday());

    header.add(prev);
    header.add(monthLabel);
    header.add(next);
    header.add(today);
    return header;
  }

  private void rebuild() {
    monthLabel.setText(displayedMonth.getMonth().name() + " " + displayedMonth.getYear());
    grid.removeAll();
    cells.clear();

    for (String name : WEEKDAY_NAMES) {
      JLabel label = new JLabel(name, SwingConstants.CENTER);
      label.setFont(Theme.SMALL);
      label.setForeground(Theme.MUTED_TEXT);
      grid.add(label);
    }

    LocalDate today = LocalDate.now();
    LocalDate cursor = displayedMonth.atDay(1)
        .minusDays(displayedMonth.atDay(1).getDayOfWeek().getValue() % 7);
    for (int i = 0; i < WEEKS * 7; i++) {
      boolean inMonth = YearMonth.from(cursor).equals(displayedMonth);
      DayCell cell = new DayCell(cursor, inMonth, cursor.equals(today), this::selectDay);
      cell.setSelected(cursor.equals(selectedDate));
      cells.put(cursor, cell);
      grid.add(cell);
      cursor = cursor.plusDays(1);
    }
    grid.revalidate();
    grid.repaint();
    onMonthChanged.run();
  }

  private void selectDay(LocalDate date) {
    selectedDate = date;
    for (DayCell cell : cells.values()) {
      cell.setSelected(cell.getDate().equals(date));
    }
    onDaySelected.accept(date);
  }
}
