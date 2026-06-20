package calendar.view;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Top toolbar holding the primary calendar and event actions. Each button delegates to the
 * relevant dialog in {@link GuiDialogs}.
 */
class ActionToolbar extends JPanel {

  /**
   * Constructs the toolbar and wires its buttons to the given dialogs.
   *
   * @param dialogs the dialog helper the buttons delegate to
   */
  ActionToolbar(GuiDialogs dialogs) {
    super(new FlowLayout(FlowLayout.LEFT, Theme.UNIT, Theme.UNIT));
    setBackground(Theme.SURFACE);

    JButton createEvent = UiFactory.primaryButton("Create Event");
    createEvent.setToolTipText("Create a new event (Ctrl+N)");
    createEvent.addActionListener(e -> dialogs.openCreateEvent());
    JButton createSeriesCount = UiFactory.secondaryButton("Create Series (Count)");
    createSeriesCount.addActionListener(e -> dialogs.openCreateSeries(true));
    JButton createSeriesUntil = UiFactory.secondaryButton("Create Series (Until Date)");
    createSeriesUntil.addActionListener(e -> dialogs.openCreateSeries(false));
    JButton editEvents = UiFactory.secondaryButton("Edit Multiple Events");
    editEvents.addActionListener(e -> dialogs.openEditEvents());
    JButton deleteEvents = UiFactory.secondaryButton("Delete Events");
    deleteEvents.addActionListener(e -> dialogs.openDeleteEvents());

    add(createEvent);
    add(createSeriesCount);
    add(createSeriesUntil);
    add(editEvents);
    add(deleteEvents);
  }
}
