package calendar.view;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Left-hand sidebar listing the available calendars. The active calendar is highlighted, and
 * selecting a calendar notifies the supplied callback. The panel also remembers the current
 * calendar names so dialogs can offer them without querying the model directly.
 */
class SidebarPanel extends JPanel {

  private final transient Consumer<String> onCalendarSelected;
  private final List<String> calendarNames;

  /**
   * Constructs the sidebar.
   *
   * @param onCalendarSelected callback invoked with a calendar name when the user selects it
   */
  SidebarPanel(Consumer<String> onCalendarSelected) {
    this.onCalendarSelected = onCalendarSelected;
    this.calendarNames = new ArrayList<>();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Theme.BACKGROUND);
  }

  /**
   * Renders the calendar list and highlights the active calendar.
   *
   * @param names          the calendar names to display
   * @param activeCalendar the name of the active calendar
   */
  void showCalendars(List<String> names, String activeCalendar) {
    removeAll();
    calendarNames.clear();
    calendarNames.addAll(names);

    for (String name : names) {
      JButton button = name.equals(activeCalendar)
          ? UiFactory.primaryButton(name)
          : UiFactory.secondaryButton(name);
      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
      button.addActionListener(e -> onCalendarSelected.accept(name));
      add(button);
      add(Box.createRigidArea(new Dimension(0, Theme.UNIT / 2)));
    }
    revalidate();
    repaint();
  }

  /**
   * Returns the calendar names currently displayed.
   *
   * @return an immutable snapshot of the calendar names
   */
  List<String> getCalendarNames() {
    return new ArrayList<>(calendarNames);
  }
}
