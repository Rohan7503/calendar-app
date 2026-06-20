package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The central calendar surface: a navigable month grid of day cells. Selecting a day highlights it
 * and notifies the supplied callback. This panel owns the currently selected date, which is the
 * single source of truth used by the rest of the GUI for day-scoped actions.
 */
class MonthViewPanel extends JPanel {

  private static final String[] WEEKDAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

  private final transient Consumer<LocalDate> onDaySelected;
  private final JPanel grid;
  private final JLabel monthLabel;
  private final List<JButton> dayButtons;
  private YearMonth displayedMonth;
  private LocalDate selectedDate;

  /**
   * Constructs the month view.
   *
   * @param onDaySelected callback invoked with the chosen date when the user selects a day
   */
  MonthViewPanel(Consumer<LocalDate> onDaySelected) {
    this.onDaySelected = onDaySelected;
    this.dayButtons = new ArrayList<>();
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
   * Returns the currently selected date, or {@code null} if no day is selected.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate() {
    return selectedDate;
  }

  private JPanel buildHeader() {
    JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, Theme.UNIT, Theme.UNIT));
    header.setBackground(Theme.BACKGROUND);

    JButton prev = UiFactory.secondaryButton("<");
    JButton next = UiFactory.secondaryButton(">");
    prev.addActionListener(e -> {
      displayedMonth = displayedMonth.minusMonths(1);
      rebuild();
    });
    next.addActionListener(e -> {
      displayedMonth = displayedMonth.plusMonths(1);
      rebuild();
    });

    header.add(prev);
    header.add(monthLabel);
    header.add(next);
    return header;
  }

  private void rebuild() {
    monthLabel.setText(displayedMonth.getMonth().name() + " " + displayedMonth.getYear());
    grid.removeAll();
    dayButtons.clear();

    for (String name : WEEKDAY_NAMES) {
      JLabel label = new JLabel(name, SwingConstants.CENTER);
      label.setFont(Theme.SMALL);
      label.setForeground(Theme.MUTED_TEXT);
      grid.add(label);
    }

    LocalDate first = displayedMonth.atDay(1);
    int startColumn = first.getDayOfWeek().getValue() % 7;
    for (int i = 0; i < startColumn; i++) {
      grid.add(new JLabel(""));
    }
    for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
      LocalDate date = displayedMonth.atDay(day);
      JButton cell = new JButton(String.valueOf(day));
      cell.setVerticalAlignment(SwingConstants.TOP);
      cell.setHorizontalAlignment(SwingConstants.LEFT);
      cell.putClientProperty("date", date);
      cell.addActionListener(e -> selectDay(date));
      styleDay(cell, date);
      dayButtons.add(cell);
      grid.add(cell);
    }
    grid.revalidate();
    grid.repaint();
  }

  private void selectDay(LocalDate date) {
    selectedDate = date;
    for (JButton button : dayButtons) {
      LocalDate cellDate = (LocalDate) button.getClientProperty("date");
      if (cellDate != null) {
        styleDay(button, cellDate);
      }
    }
    grid.repaint();
    onDaySelected.accept(date);
  }

  private void styleDay(JButton cell, LocalDate date) {
    cell.setMargin(new Insets(2, Theme.UNIT / 2, 2, Theme.UNIT / 2));
    cell.setPreferredSize(Theme.DAY_CELL);
    cell.setOpaque(true);
    cell.setFocusPainted(false);
    cell.setForeground(Theme.TEXT);
    cell.setFont(Theme.BODY);
    cell.setBackground(Theme.SURFACE);

    DayOfWeek dow = date.getDayOfWeek();
    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
      cell.setBackground(Theme.WEEKEND);
    }
    if (date.equals(selectedDate)) {
      cell.setBackground(Theme.SELECTED);
      cell.setBorder(BorderFactory.createLineBorder(Theme.SELECTED_BORDER, 2));
    } else {
      cell.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
    }
  }
}
