package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Swing implementation of {@link CalGuiInterface}. This class is the application shell: it lays out
 * the toolbar, calendar sidebar, month surface, and day detail panel, and routes the controller's
 * view updates to the appropriate component. The actual rendering and input handling live in the
 * focused panel classes and in {@link GuiDialogs}, keeping this coordinator small.
 */
public class CalGuiImpl extends JFrame implements CalGuiInterface {

  private final SidebarPanel sidebar;
  private final MonthViewPanel monthView;
  private final DayDetailPanel dayDetail;
  private final StatusStrip statusStrip;
  private transient Features features;
  private transient GuiDialogs dialogs;

  /**
   * Builds the frame and its panels. Controller callbacks are attached later via
   * {@link #addFeatures(Features)}.
   */
  public CalGuiImpl() {
    super("Calendar App");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    sidebar = new SidebarPanel(name -> features.selectCalendar(name));
    monthView = new MonthViewPanel(
        date -> features.requestEventsForDay(date.toString()),
        this::loadMonth);
    dayDetail = new DayDetailPanel(
        event -> dialogs.openEditEvent(event),
        event -> dialogs.confirmAndDeleteEvent(event));
    statusStrip = new StatusStrip();

    setLayout(new BorderLayout());
    add(titledScroll("Calendars", sidebar, Theme.SIDE_PANEL), BorderLayout.WEST);
    add(monthView, BorderLayout.CENTER);
    add(titledScroll("Day Events", dayDetail, Theme.DETAIL_PANEL), BorderLayout.EAST);
    add(statusStrip, BorderLayout.SOUTH);
  }

  @Override
  public void showGui() {
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
    monthView.selectToday();
  }

  @Override
  public void addFeatures(Features features) {
    this.features = features;
    this.dialogs = new GuiDialogs(this, features, monthView::getSelectedDate,
        sidebar::getCalendarNames);
    add(new ActionToolbar(dialogs), BorderLayout.NORTH);
    setJMenuBar(new AppMenuBar(dialogs));
    revalidate();
  }

  @Override
  public void showCalendars(List<String> calendarNames, String activeCalendar) {
    sidebar.showCalendars(calendarNames, activeCalendar);
  }

  @Override
  public void showEventsForDay(LocalDate day, List<Event> events) {
    dayDetail.showEventsForDay(day, events);
  }

  @Override
  public void showMonthEvents(List<Event> events) {
    monthView.setMonthEvents(events);
  }

  @Override
  public void showEventsInRange(String title, List<Event> events) {
    dayDetail.showEvents(title, events);
  }

  @Override
  public void refreshEvents() {
    loadMonth();
    LocalDate selected = monthView.getSelectedDate();
    if (selected != null) {
      features.requestEventsForDay(selected.toString());
    }
  }

  /**
   * Requests the events for the currently displayed month so the grid can show event indicators.
   * No-op until the controller callbacks have been attached.
   */
  private void loadMonth() {
    if (features == null) {
      return;
    }
    YearMonth month = monthView.getDisplayedMonth();
    features.requestMonthView(
        month.atDay(1).atStartOfDay().toString(),
        month.atEndOfMonth().atTime(23, 59).toString());
  }

  @Override
  public void showMessage(String message) {
    statusStrip.showMessage(message);
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private JScrollPane titledScroll(String heading, Component content, Dimension size) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBackground(Theme.BACKGROUND);
    wrapper.setBorder(UiFactory.padding(1));
    JLabel title = UiFactory.title(heading);
    title.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.UNIT, 0));
    wrapper.add(title, BorderLayout.NORTH);
    wrapper.add(content, BorderLayout.CENTER);

    JScrollPane scroll = new JScrollPane(wrapper);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setPreferredSize(size);
    return scroll;
  }
}
