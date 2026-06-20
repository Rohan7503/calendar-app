package calendar.view;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Application menu bar exposing the calendar tools (edit calendar, export, copy, status, and
 * range view) under a single "Tools" menu. Each item delegates to {@link GuiDialogs}.
 */
class AppMenuBar extends JMenuBar {

  /**
   * Constructs the menu bar and wires its items to the given dialogs.
   *
   * @param dialogs the dialog helper the menu items delegate to
   */
  AppMenuBar(GuiDialogs dialogs) {
    JMenu tools = new JMenu("Tools");

    tools.add(item("Edit Calendar...", () -> dialogs.openEditCalendar()));
    tools.addSeparator();
    tools.add(item("Export to CSV...", () -> dialogs.openExport("csv")));
    tools.add(item("Export to ICS...", () -> dialogs.openExport("ics")));
    tools.add(item("Copy Events...", dialogs::openCopyEvents));
    tools.addSeparator();
    tools.add(item("Show Status...", dialogs::openShowStatus));
    tools.add(item("View Date Range...", dialogs::openRangeView));

    add(tools);
  }

  private JMenuItem item(String text, Runnable action) {
    JMenuItem menuItem = new JMenuItem(text);
    menuItem.addActionListener(e -> action.run());
    return menuItem;
  }
}
