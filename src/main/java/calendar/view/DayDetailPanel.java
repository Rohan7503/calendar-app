package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
 * Right-hand panel that presents a list of events as cards with inline edit and delete actions.
 * It backs both the selected-day view and the date-range view; the actions are delegated to the
 * supplied callbacks.
 */
class DayDetailPanel extends JPanel {

  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  private final transient Consumer<Event> onEdit;
  private final transient Consumer<Event> onDelete;
  private final JLabel header;
  private final JPanel list;

  /**
   * Constructs the detail panel.
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
    showEvents("Events on " + day, events);
  }

  /**
   * Displays an arbitrary list of events under the given heading.
   *
   * @param title  the heading to show
   * @param events the events to display
   */
  void showEvents(String title, List<Event> events) {
    header.setText(title);
    list.removeAll();

    if (events.isEmpty()) {
      JLabel empty = new JLabel("No events to show.");
      empty.setFont(Theme.BODY);
      empty.setForeground(Theme.MUTED_TEXT);
      empty.setAlignmentX(Component.LEFT_ALIGNMENT);
      list.add(empty);
    } else {
      for (Event event : events) {
        list.add(buildCard(event));
        list.add(Box.createRigidArea(new Dimension(0, Theme.UNIT / 2)));
      }
    }
    list.revalidate();
    list.repaint();
  }

  private JPanel buildCard(Event event) {
    JPanel card = UiFactory.card();
    card.setLayout(new BorderLayout(Theme.UNIT, 0));

    JPanel details = new JPanel();
    details.setOpaque(false);
    details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
    details.add(line(event.getSubject(), Theme.BODY_BOLD, Theme.TEXT));
    details.add(line(when(event), Theme.SMALL, Theme.MUTED_TEXT));
    String meta = meta(event);
    if (!meta.isEmpty()) {
      details.add(line(meta, Theme.SMALL, Theme.MUTED_TEXT));
    }

    JPanel actions = new JPanel();
    actions.setOpaque(false);
    actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
    JButton edit = UiFactory.smallButton("Edit");
    edit.setAlignmentX(Component.RIGHT_ALIGNMENT);
    edit.addActionListener(e -> onEdit.accept(event));
    JButton delete = UiFactory.smallButton("Delete");
    delete.setForeground(Theme.DANGER);
    delete.setAlignmentX(Component.RIGHT_ALIGNMENT);
    delete.addActionListener(e -> onDelete.accept(event));
    actions.add(edit);
    actions.add(Box.createRigidArea(new Dimension(0, Theme.UNIT / 2)));
    actions.add(delete);

    card.add(details, BorderLayout.CENTER);
    card.add(actions, BorderLayout.EAST);
    UiFactory.capHeight(card);
    return card;
  }

  private JLabel line(String text, Font font, Color color) {
    JLabel label = new JLabel(text);
    label.setFont(font);
    label.setForeground(color);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }

  private String when(Event event) {
    if (event.isAllDay()) {
      return DATE.format(event.getStart()) + "  -  All day";
    }
    return String.format("%s  -  %s to %s", DATE.format(event.getStart()),
        TIME.format(event.getStart()), TIME.format(event.getEnd()));
  }

  private String meta(Event event) {
    StringBuilder sb = new StringBuilder();
    if (event.getLocation() != null) {
      sb.append(event.getLocation());
    }
    if (event.getStatus() != null) {
      if (sb.length() > 0) {
        sb.append("  -  ");
      }
      sb.append(event.getStatus());
    }
    return sb.toString();
  }
}
