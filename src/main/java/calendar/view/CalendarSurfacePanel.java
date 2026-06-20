package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * The central calendar surface. It hosts the month, week, and day views behind a shared header
 * (previous / today / next navigation, the current period label, and a Month/Week/Day switcher)
 * and a {@link CardLayout}. It owns the anchor date that drives the visible period and the selected
 * day. Navigation and view switches notify {@code onRangeChanged} so the shell can load the events
 * for the visible range; selecting a day notifies {@code onDaySelected} so the detail updates.
 */
class CalendarSurfacePanel extends JPanel {

  private static final DateTimeFormatter WEEK_RANGE = DateTimeFormatter.ofPattern("MMM d");
  private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("EEE, MMM d yyyy");

  private final transient Consumer<LocalDate> onDaySelected;
  private final transient Runnable onRangeChanged;
  private final CardLayout cards = new CardLayout();
  private final JPanel deck = new JPanel(cards);
  private final JLabel periodLabel = new JLabel("", SwingConstants.CENTER);
  private final MonthGrid monthGrid;
  private final WeekGrid weekGrid;
  private final DayGrid dayGrid;
  private final JToggleButton monthButton = new JToggleButton("Month");
  private final JToggleButton weekButton = new JToggleButton("Week");
  private final JToggleButton dayButton = new JToggleButton("Day");

  private ViewMode mode = ViewMode.MONTH;
  private LocalDate anchor = LocalDate.now();
  private LocalDate selected;

  /**
   * Creates the calendar surface.
   *
   * @param onDaySelected  callback invoked with a date when the user selects a day
   * @param onRangeChanged callback invoked after the visible period changes (to load its events)
   */
  CalendarSurfacePanel(Consumer<LocalDate> onDaySelected, Runnable onRangeChanged) {
    this.onDaySelected = onDaySelected;
    this.onRangeChanged = onRangeChanged;
    this.monthGrid = new MonthGrid(this::onDayClicked);
    this.weekGrid = new WeekGrid(this::onDayClicked);
    this.dayGrid = new DayGrid(this::onDayClicked);

    setLayout(new BorderLayout());
    setBackground(Theme.BACKGROUND);
    setBorder(UiFactory.padding(1));
    add(buildHeader(), BorderLayout.NORTH);

    deck.setBackground(Theme.BACKGROUND);
    deck.add(monthGrid, ViewMode.MONTH.name());
    deck.add(weekGrid, ViewMode.WEEK.name());
    deck.add(dayGrid, ViewMode.DAY.name());
    add(deck, BorderLayout.CENTER);

    // Render an initial (empty) month so the frame packs to a sensible size before events load.
    monthGrid.showMonth(YearMonth.from(anchor), null);
    periodLabel.setText(periodText());
  }

  /**
   * Returns the start of the visible range.
   *
   * @return the first visible instant
   */
  LocalDateTime getVisibleStart() {
    return visibleFirstDay().atStartOfDay();
  }

  /**
   * Returns the end of the visible range.
   *
   * @return the last visible instant
   */
  LocalDateTime getVisibleEnd() {
    return visibleLastDay().atTime(23, 59);
  }

  /**
   * Returns the currently selected day, or {@code null} if none.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate() {
    return selected;
  }

  /**
   * Distributes events to the active view.
   *
   * @param events the events within the visible range
   */
  void setEvents(List<Event> events) {
    switch (mode) {
      case WEEK:
        weekGrid.setEvents(events);
        break;
      case DAY:
        dayGrid.setEvents(events);
        break;
      default:
        monthGrid.setEvents(events);
    }
  }

  /**
   * Navigates to the current period and selects today.
   */
  void selectToday() {
    anchor = LocalDate.now();
    selected = anchor;
    refresh();
    onDaySelected.accept(selected);
  }

  /**
   * Switches the active view mode.
   *
   * @param newMode the view to switch to
   */
  void switchTo(ViewMode newMode) {
    mode = newMode;
    syncToggles();
    refresh();
  }

  /**
   * Moves to the previous period (month, week, or day depending on the active view).
   */
  void previous() {
    anchor = shift(anchor, -1);
    refresh();
  }

  /**
   * Moves to the next period (month, week, or day depending on the active view).
   */
  void next() {
    anchor = shift(anchor, 1);
    refresh();
  }

  private JPanel buildHeader() {
    JButton prev = UiFactory.secondaryButton("<");
    prev.setToolTipText("Previous");
    prev.addActionListener(e -> previous());
    JButton today = UiFactory.secondaryButton("Today");
    today.addActionListener(e -> selectToday());
    JButton next = UiFactory.secondaryButton(">");
    next.setToolTipText("Next");
    next.addActionListener(e -> next());

    JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.UNIT / 2, 0));
    nav.setBackground(Theme.BACKGROUND);
    nav.add(prev);
    nav.add(today);
    nav.add(next);

    periodLabel.setFont(Theme.TITLE);

    final ButtonGroup group = new ButtonGroup();
    JPanel switcher = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    switcher.setBackground(Theme.BACKGROUND);
    monthButton.addActionListener(e -> switchTo(ViewMode.MONTH));
    weekButton.addActionListener(e -> switchTo(ViewMode.WEEK));
    dayButton.addActionListener(e -> switchTo(ViewMode.DAY));
    for (JToggleButton button : new JToggleButton[] {monthButton, weekButton, dayButton}) {
      button.setFocusPainted(false);
      button.setFont(Theme.BODY);
      group.add(button);
      switcher.add(button);
    }
    monthButton.setSelected(true);

    JPanel header = new JPanel(new BorderLayout(Theme.UNIT, 0));
    header.setBackground(Theme.BACKGROUND);
    header.setBorder(UiFactory.padding(1));
    header.add(nav, BorderLayout.WEST);
    header.add(periodLabel, BorderLayout.CENTER);
    header.add(switcher, BorderLayout.EAST);
    return header;
  }

  private void onDayClicked(LocalDate date) {
    selected = date;
    anchor = date;
    activeSetSelected(date);
    onDaySelected.accept(date);
  }

  private void refresh() {
    switch (mode) {
      case WEEK:
        weekGrid.showWeek(anchor, selected);
        break;
      case DAY:
        dayGrid.showDay(anchor);
        break;
      default:
        monthGrid.showMonth(YearMonth.from(anchor), selected);
    }
    cards.show(deck, mode.name());
    periodLabel.setText(periodText());
    onRangeChanged.run();
  }

  private void activeSetSelected(LocalDate date) {
    if (mode == ViewMode.WEEK) {
      weekGrid.setSelected(date);
    } else if (mode == ViewMode.MONTH) {
      monthGrid.setSelected(date);
    }
  }

  private void syncToggles() {
    monthButton.setSelected(mode == ViewMode.MONTH);
    weekButton.setSelected(mode == ViewMode.WEEK);
    dayButton.setSelected(mode == ViewMode.DAY);
  }

  private LocalDate shift(LocalDate date, int amount) {
    switch (mode) {
      case WEEK:
        return date.plusWeeks(amount);
      case DAY:
        return date.plusDays(amount);
      default:
        return date.plusMonths(amount);
    }
  }

  private LocalDate visibleFirstDay() {
    if (mode == ViewMode.DAY) {
      return anchor;
    }
    if (mode == ViewMode.WEEK) {
      return startOfWeek(anchor);
    }
    LocalDate firstOfMonth = YearMonth.from(anchor).atDay(1);
    return startOfWeek(firstOfMonth);
  }

  private LocalDate visibleLastDay() {
    if (mode == ViewMode.DAY) {
      return anchor;
    }
    if (mode == ViewMode.WEEK) {
      return startOfWeek(anchor).plusDays(6);
    }
    return visibleFirstDay().plusDays(41);
  }

  private String periodText() {
    if (mode == ViewMode.DAY) {
      return DAY_LABEL.format(anchor);
    }
    if (mode == ViewMode.WEEK) {
      LocalDate start = startOfWeek(anchor);
      LocalDate end = start.plusDays(6);
      return WEEK_RANGE.format(start) + " - " + WEEK_RANGE.format(end) + ", " + end.getYear();
    }
    YearMonth month = YearMonth.from(anchor);
    return month.getMonth().name() + " " + month.getYear();
  }

  private static LocalDate startOfWeek(LocalDate date) {
    return date.minusDays(date.getDayOfWeek().getValue() % 7);
  }
}
