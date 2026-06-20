package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A single day cell in the month grid. It shows the day number and up to a couple of event chips
 * (with a "+N" overflow indicator), and reflects whether the day is today, selected, a weekend, or
 * outside the displayed month. In-month cells are clickable.
 */
class DayCell extends JPanel {

  private static final int MAX_CHIPS = 2;

  private final LocalDate date;
  private final boolean inMonth;
  private final boolean today;
  private final JLabel dayLabel;
  private final JPanel chipArea;
  private boolean selected;

  /**
   * Constructs a day cell.
   *
   * @param date    the date this cell represents
   * @param inMonth whether the date belongs to the currently displayed month
   * @param today   whether the date is the current day
   * @param onClick callback invoked with the date when an in-month cell is clicked
   */
  DayCell(LocalDate date, boolean inMonth, boolean today, Consumer<LocalDate> onClick) {
    this.date = date;
    this.inMonth = inMonth;
    this.today = today;
    setLayout(new BorderLayout(0, 2));
    setPreferredSize(Theme.DAY_CELL);

    dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
    dayLabel.setFont(today ? Theme.BODY_BOLD : Theme.BODY);
    dayLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
    add(dayLabel, BorderLayout.NORTH);

    chipArea = new JPanel();
    chipArea.setOpaque(false);
    chipArea.setLayout(new BoxLayout(chipArea, BoxLayout.Y_AXIS));
    chipArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
    add(chipArea, BorderLayout.CENTER);

    if (inMonth) {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          onClick.accept(date);
        }
      });
    }
    applyStyle();
    getAccessibleContext().setAccessibleName(
        date.getDayOfWeek() + " " + date.getMonth() + " " + date.getDayOfMonth()
            + (today ? ", today" : ""));
  }

  /**
   * Sets the events shown as chips in this cell.
   *
   * @param events the events occurring on this day (may be empty)
   */
  void setEvents(List<Event> events) {
    chipArea.removeAll();
    int shown = Math.min(events.size(), MAX_CHIPS);
    for (int i = 0; i < shown; i++) {
      JLabel chip = EventChip.create(events.get(i));
      chip.setAlignmentX(Component.LEFT_ALIGNMENT);
      chipArea.add(chip);
    }
    if (events.size() > MAX_CHIPS) {
      JLabel more = new JLabel("+" + (events.size() - MAX_CHIPS) + " more");
      more.setFont(Theme.SMALL);
      more.setForeground(Theme.MUTED_TEXT);
      more.setAlignmentX(Component.LEFT_ALIGNMENT);
      chipArea.add(more);
    }
    chipArea.revalidate();
    chipArea.repaint();
  }

  /**
   * Marks this cell as the selected day (or not) and restyles accordingly.
   *
   * @param selected whether this cell is the selected day
   */
  void setSelected(boolean selected) {
    this.selected = selected;
    applyStyle();
  }

  /**
   * Returns the date represented by this cell.
   *
   * @return the cell's date
   */
  LocalDate getDate() {
    return date;
  }

  private void applyStyle() {
    setOpaque(true);
    Color background = Theme.SURFACE;
    if (!inMonth) {
      background = Theme.BACKGROUND;
    } else if (isWeekend()) {
      background = Theme.WEEKEND;
    }
    if (selected) {
      background = Theme.SELECTED;
    }
    setBackground(background);
    dayLabel.setForeground(inMonth ? Theme.TEXT : Theme.OUTSIDE_MONTH_TEXT);

    if (selected) {
      setBorder(BorderFactory.createLineBorder(Theme.SELECTED_BORDER, 2));
    } else if (today) {
      setBorder(BorderFactory.createLineBorder(Theme.TODAY_BORDER, 2));
    } else {
      setBorder(BorderFactory.createLineBorder(Theme.BORDER));
    }
  }

  private boolean isWeekend() {
    DayOfWeek dow = date.getDayOfWeek();
    return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
  }
}
