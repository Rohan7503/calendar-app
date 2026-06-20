package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A single day's column used by the week and day views. It has a clickable date header, a band of
 * all-day event chips, and a time-ordered list of timed event chips. Clicking anywhere in the
 * column selects its day.
 */
class DayColumn extends JPanel {

  private static final DateTimeFormatter DOW = DateTimeFormatter.ofPattern("EEE");
  private static final DateTimeFormatter DAY_NUM = DateTimeFormatter.ofPattern("MMM d");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  private final LocalDate date;
  private final JPanel body;
  private final JPanel headerPanel;
  private boolean selected;

  /**
   * Creates a day column.
   *
   * @param date       the day this column represents
   * @param today      whether this is the current day
   * @param onSelected callback invoked with the date when the column is clicked
   */
  DayColumn(LocalDate date, boolean today, Consumer<LocalDate> onSelected) {
    this.date = date;
    setLayout(new BorderLayout());
    setBackground(Theme.SURFACE);

    headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(today ? Theme.ACCENT_SOFT : Theme.BACKGROUND);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(2, Theme.UNIT / 2, 2, Theme.UNIT / 2));
    JLabel dow = new JLabel(DOW.format(date), SwingConstants.LEFT);
    dow.setFont(Theme.SMALL);
    dow.setForeground(Theme.MUTED_TEXT);
    JLabel num = new JLabel(DAY_NUM.format(date), SwingConstants.RIGHT);
    num.setFont(today ? Theme.BODY_BOLD : Theme.BODY);
    headerPanel.add(dow, BorderLayout.WEST);
    headerPanel.add(num, BorderLayout.EAST);

    body = new JPanel();
    body.setBackground(Theme.SURFACE);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setBorder(BorderFactory.createEmptyBorder(Theme.UNIT / 2, Theme.UNIT / 2,
        Theme.UNIT / 2, Theme.UNIT / 2));

    add(headerPanel, BorderLayout.NORTH);
    add(body, BorderLayout.CENTER);
    applyBorder();

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    MouseAdapter click = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        onSelected.accept(date);
      }
    };
    addMouseListener(click);
    headerPanel.addMouseListener(click);
    body.addMouseListener(click);
  }

  /**
   * Sets the events for this day, rendering all-day chips first and then timed chips in order.
   *
   * @param events the events that start on this day
   */
  void setEvents(List<Event> events) {
    body.removeAll();
    for (Event event : EventGrouping.allDay(events)) {
      body.add(chipRow(event));
    }
    for (Event event : EventGrouping.timed(events)) {
      body.add(chipRow(event));
    }
    if (events.isEmpty()) {
      JLabel empty = new JLabel(" ");
      empty.setAlignmentX(Component.LEFT_ALIGNMENT);
      body.add(empty);
    }
    body.revalidate();
    body.repaint();
  }

  /**
   * Marks this column as the selected day and restyles its border.
   *
   * @param selected whether this column is selected
   */
  void setSelected(boolean selected) {
    this.selected = selected;
    applyBorder();
  }

  /**
   * Returns the day this column represents.
   *
   * @return the column's date
   */
  LocalDate getDate() {
    return date;
  }

  private JPanel chipRow(Event event) {
    JPanel row = new JPanel(new BorderLayout());
    row.setOpaque(false);
    row.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    JLabel chip = EventChip.create(event);
    if (!event.isAllDay()) {
      chip.setText(TIME.format(event.getStart()) + "  " + chip.getText());
    }
    chip.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.add(chip, BorderLayout.CENTER);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, chip.getPreferredSize().height + 2));
    return row;
  }

  private void applyBorder() {
    Color line = selected ? Theme.SELECTED_BORDER : Theme.BORDER;
    int thickness = selected ? 2 : 1;
    setBorder(BorderFactory.createLineBorder(line, thickness));
  }
}
