package calendar.view;

import calendar.model.Event;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Right-hand panel showing the events of the currently selected day. Each event is rendered as a
 * row with inline edit and delete actions, which are delegated to the supplied callbacks.
 */
class DayDetailPanel extends JPanel {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final transient Consumer<Event> onEdit;
  private final transient Consumer<Event> onDelete;
  private final JLabel header;
  private final JPanel list;

  /**
   * Constructs the day detail panel.
   *
   * @param onEdit   callback invoked with the event to edit
   * @param onDelete callback invoked with the event to delete
   */
  DayDetailPanel(Consumer<Event> onEdit, Consumer<Event> onDelete) {
    this.onEdit = onEdit;
    this.onDelete = onDelete;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Theme.BACKGROUND);

    header = UiFactory.title("No day selected");
    header.setAlignmentX(Component.LEFT_ALIGNMENT);
    list = new JPanel();
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    list.setBackground(Theme.BACKGROUND);
    list.setAlignmentX(Component.LEFT_ALIGNMENT);

    add(header);
    add(Box.createRigidArea(new Dimension(0, Theme.UNIT)));
    add(list);
  }

  /**
   * Displays the events for the given day.
   *
   * @param day    the day being shown
   * @param events the events on that day
   */
  void showEventsForDay(LocalDate day, List<Event> events) {
    header.setText("Events on " + day);
    list.removeAll();

    if (events.isEmpty()) {
      JLabel empty = new JLabel("No events scheduled.");
      empty.setFont(Theme.BODY);
      empty.setForeground(Theme.MUTED_TEXT);
      empty.setAlignmentX(Component.LEFT_ALIGNMENT);
      list.add(empty);
    } else {
      for (Event event : events) {
        list.add(buildEventRow(event));
        list.add(Box.createRigidArea(new Dimension(0, Theme.UNIT / 2)));
      }
    }
    list.revalidate();
    list.repaint();
  }

  private JPanel buildEventRow(Event event) {
    JPanel row = UiFactory.card();
    row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

    JLabel label = new JLabel(describe(event));
    label.setFont(Theme.BODY);

    JButton edit = UiFactory.smallButton("Edit");
    edit.addActionListener(e -> onEdit.accept(event));
    JButton delete = UiFactory.smallButton("Delete");
    delete.setForeground(Theme.DANGER);
    delete.addActionListener(e -> onDelete.accept(event));

    row.add(label);
    row.add(Box.createHorizontalGlue());
    row.add(edit);
    row.add(Box.createRigidArea(new Dimension(Theme.UNIT / 2, 0)));
    row.add(delete);
    UiFactory.capHeight(row);
    return row;
  }

  private String describe(Event event) {
    if (event.isAllDay()) {
      return event.getSubject() + "  ·  All day";
    }
    return String.format("%s  ·  %s – %s", event.getSubject(),
        event.getStart().format(TIME_FORMAT), event.getEnd().format(TIME_FORMAT));
  }
}
