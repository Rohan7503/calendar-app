package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Left-hand sidebar listing the available calendars as rows with a color marker, name, and
 * timezone subtitle. The active calendar is emphasized. Each row can be selected (to switch
 * calendars) or opened for settings, and a "New calendar" action sits at the bottom. The panel
 * remembers the current calendar names so dialogs can offer them.
 */
class SidebarPanel extends JPanel {

  private final transient Consumer<String> onCalendarSelected;
  private final transient Consumer<String> onEditCalendar;
  private final List<String> calendarNames = new ArrayList<>();
  private final JPanel rows;

  /**
   * Constructs the sidebar.
   *
   * @param onCalendarSelected callback invoked with a calendar name when the user selects it
   * @param onNewCalendar      callback invoked when the user asks to create a calendar
   * @param onEditCalendar     callback invoked with a calendar name to open its settings
   */
  SidebarPanel(Consumer<String> onCalendarSelected, Runnable onNewCalendar,
               Consumer<String> onEditCalendar) {
    this.onCalendarSelected = onCalendarSelected;
    this.onEditCalendar = onEditCalendar;
    setLayout(new BorderLayout());
    setBackground(Theme.BACKGROUND);

    rows = new JPanel();
    rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
    rows.setBackground(Theme.BACKGROUND);
    add(rows, BorderLayout.CENTER);

    JButton newButton = UiFactory.secondaryButton("+ New calendar");
    newButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    newButton.addActionListener(e -> onNewCalendar.run());
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(Theme.BACKGROUND);
    footer.setBorder(BorderFactory.createEmptyBorder(Theme.UNIT, 0, 0, 0));
    footer.add(newButton, BorderLayout.CENTER);
    add(footer, BorderLayout.SOUTH);
  }

  /**
   * Renders the calendar list and highlights the active calendar.
   *
   * @param calendars      summaries of the calendars to display
   * @param activeCalendar the name of the active calendar
   */
  void showCalendars(List<CalendarSummary> calendars, String activeCalendar) {
    rows.removeAll();
    calendarNames.clear();
    for (CalendarSummary summary : calendars) {
      calendarNames.add(summary.name());
      rows.add(buildRow(summary, summary.name().equals(activeCalendar)));
      rows.add(Box.createRigidArea(new Dimension(0, Theme.UNIT / 2)));
    }
    rows.revalidate();
    rows.repaint();
  }

  /**
   * Returns the calendar names currently displayed.
   *
   * @return a snapshot of the calendar names
   */
  List<String> getCalendarNames() {
    return new ArrayList<>(calendarNames);
  }

  private JPanel buildRow(CalendarSummary summary, boolean active) {
    JPanel row = new JPanel(new BorderLayout(Theme.UNIT / 2, 0));
    row.setBackground(active ? Theme.ACCENT_SOFT : Theme.SURFACE);
    Color edge = active ? Theme.ACCENT : Theme.BORDER;
    row.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.calendarColor(summary.name())),
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(edge),
            BorderFactory.createEmptyBorder(Theme.UNIT / 2, Theme.UNIT / 2,
                Theme.UNIT / 2, Theme.UNIT / 2))));

    final JLabel dot = new JLabel(UiFactory.colorDot(Theme.calendarColor(summary.name())));

    JPanel text = new JPanel();
    text.setOpaque(false);
    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
    JLabel name = new JLabel(summary.name());
    name.setFont(active ? Theme.BODY_BOLD : Theme.BODY);
    name.setForeground(Theme.TEXT);
    name.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel zone = new JLabel(summary.timezone());
    zone.setFont(Theme.SMALL);
    zone.setForeground(Theme.MUTED_TEXT);
    zone.setAlignmentX(Component.LEFT_ALIGNMENT);
    text.add(name);
    text.add(zone);

    JButton settings = UiFactory.smallButton("Edit");
    settings.setToolTipText("Calendar settings");
    settings.getAccessibleContext().setAccessibleName("Settings for " + summary.name());
    settings.addActionListener(e -> onEditCalendar.accept(summary.name()));

    row.add(dot, BorderLayout.WEST);
    row.add(text, BorderLayout.CENTER);
    row.add(settings, BorderLayout.EAST);
    row.getAccessibleContext().setAccessibleName(
        "Calendar " + summary.name() + (active ? ", active" : ""));
    row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    row.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        onCalendarSelected.accept(summary.name());
      }
    });
    UiFactory.capHeight(row);
    return row;
  }
}
