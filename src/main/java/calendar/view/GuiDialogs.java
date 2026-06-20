package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import calendar.model.EventProperty;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Builds and shows the application's input dialogs and forwards the collected input to the
 * {@link Features} callbacks. Keeping all dialog assembly here keeps the frame and panels focused
 * on layout and rendering.
 */
class GuiDialogs {

  private final Component parent;
  private final transient Features features;
  private final transient Supplier<LocalDate> selectedDate;
  private final transient Supplier<List<String>> calendars;

  /**
   * Constructs the dialog helper.
   *
   * @param parent       the component dialogs are centered over
   * @param features     the controller callbacks to invoke
   * @param selectedDate supplies the currently selected day (may yield {@code null})
   * @param calendars    supplies the names of the available calendars
   */
  GuiDialogs(Component parent, Features features, Supplier<LocalDate> selectedDate,
             Supplier<List<String>> calendars) {
    this.parent = parent;
    this.features = features;
    this.selectedDate = selectedDate;
    this.calendars = calendars;
  }

  /**
   * Prompts for a calendar name and timezone and requests creation.
   */
  void openCreateCalendar() {
    String name = JOptionPane.showInputDialog(parent, "Enter calendar name:");
    if (name == null || name.isBlank()) {
      return;
    }
    String tz = JOptionPane.showInputDialog(parent, "Enter timezone (e.g., America/New_York):");
    if (tz == null || tz.isBlank()) {
      return;
    }
    features.createCalendar(name, tz);
  }

  /**
   * Shows the create-event form, supporting both timed and all-day events.
   */
  void openCreateEvent() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel firstRow = new JPanel(new GridLayout(1, 1));
    JPanel remRows = new JPanel(new GridLayout(0, 2));

    final JTextField subject = labelledField(firstRow, "Subject:", "", 0);
    final JTextField startDate = labelledField(remRows, "Start Date (YYYY-MM-DD):",
        defaultDate(), 0);
    JTextField startTime = labelledField(remRows, "Start Time (HH:MM):", "08:00", 0);
    JTextField endDate = labelledField(remRows, "End Date (YYYY-MM-DD):", defaultDate(), 0);
    JTextField endTime = labelledField(remRows, "End Time (HH:MM):", "17:00", 0);

    JCheckBox allDay = new JCheckBox("All-day event");
    allDay.addItemListener(e -> {
      boolean timed = !allDay.isSelected();
      startTime.setEnabled(timed);
      endDate.setEnabled(timed);
      endTime.setEnabled(timed);
    });

    panel.add(firstRow, BorderLayout.NORTH);
    panel.add(remRows, BorderLayout.CENTER);
    panel.add(allDay, BorderLayout.SOUTH);

    if (confirm(panel, "Create Event")) {
      if (allDay.isSelected()) {
        features.createAllDayEvent(subject.getText(), startDate.getText().trim());
      } else {
        features.createEvent(subject.getText(),
            startDate.getText().trim() + "T" + startTime.getText().trim(),
            endDate.getText().trim() + "T" + endTime.getText().trim());
      }
    }
  }

  /**
   * Shows the create-series form for either a fixed count or an until-date.
   *
   * @param byCount {@code true} for a count-based series, {@code false} for an until-date series
   */
  void openCreateSeries(boolean byCount) {
    JPanel panel = verticalPanel();
    JTextField subject = labelledField(panel, "Subject:", "", 20);
    JTextField startDate = labelledField(panel, "Start Date (YYYY-MM-DD):", defaultDate(), 10);
    JTextField startTime = labelledField(panel, "Start Time (HH:MM):", "08:00", 5);
    JTextField endTime = labelledField(panel, "End Time (HH:MM):", "17:00", 5);
    Map<String, JCheckBox> weekdays = weekdayCheckBoxes(panel);
    JTextField countOrUntil = labelledField(panel,
        byCount ? "Number of occurrences:" : "Until date (YYYY-MM-DD):", "", 5);

    String title = byCount ? "Create Event Series (Count)" : "Create Event Series (Until Date)";
    if (confirm(panel, title)) {
      String start = startDate.getText().trim() + "T" + startTime.getText().trim();
      String end = startDate.getText().trim() + "T" + endTime.getText().trim();
      String days = selectedWeekdays(weekdays);
      if (byCount) {
        features.createEventSeriesByCount(subject.getText(), start, end, days,
            countOrUntil.getText().trim());
      } else {
        features.createEventSeriesUntilDate(subject.getText(), start, end, days,
            countOrUntil.getText().trim());
      }
    }
  }

  /**
   * Shows the edit form for a single event, pre-filling the value of the chosen property.
   *
   * @param event the event to edit
   */
  void openEditEvent(Event event) {
    JPanel panel = verticalPanel();
    JComboBox<String> propertyBox = propertyDropdown(panel);
    JTextField newValue = labelledField(panel, "New Value:", event.getSubject(), 20);
    propertyBox.addActionListener(e -> {
      EventProperty prop = EventProperty.valueOf((String) propertyBox.getSelectedItem());
      newValue.setText(propertyValue(event, prop));
    });

    if (confirm(panel, "Edit Event: " + event.getSubject())) {
      features.editEvent(propertyBox.getSelectedItem().toString(), event.getSubject(),
          event.getStart().toString(), event.getEnd().toString(), newValue.getText().trim());
    }
  }

  /**
   * Shows the form for editing multiple events, with a whole-series / from-here scope choice.
   */
  void openEditEvents() {
    JPanel panel = verticalPanel();
    JTextField subject = labelledField(panel, "Subject:", "", 20);
    JTextField startDate = labelledField(panel, "Start Date (YYYY-MM-DD):", defaultDate(), 15);
    JTextField startTime = labelledField(panel, "Start Time (HH:MM):", "08:00", 10);
    JComboBox<String> propertyBox = propertyDropdown(panel);
    JTextField newValue = labelledField(panel, "New Value:", "", 20);
    JRadioButton wholeSeries = scopePanel(panel, "Apply change to:",
        "Edit entire series", "Edit from this event onwards");

    if (confirm(panel, "Edit Events")) {
      features.editEvents(propertyBox.getSelectedItem().toString(), subject.getText(),
          startDate.getText().trim() + "T" + startTime.getText().trim(),
          newValue.getText(), wholeSeries.isSelected());
    }
  }

  /**
   * Confirms and requests deletion of a single event.
   *
   * @param event the event to delete
   */
  void confirmAndDeleteEvent(Event event) {
    int choice = JOptionPane.showConfirmDialog(parent,
        "Delete \"" + event.getSubject() + "\"?", "Delete Event",
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (choice == JOptionPane.YES_OPTION) {
      features.deleteEvent(event.getSubject(), event.getStart().toString(),
          event.getEnd().toString());
    }
  }

  /**
   * Shows the form for deleting multiple events, with a whole-series / from-here scope choice.
   */
  void openDeleteEvents() {
    JPanel panel = verticalPanel();
    JTextField subject = labelledField(panel, "Subject:", "", 20);
    JTextField startDate = labelledField(panel, "Start Date (YYYY-MM-DD):", defaultDate(), 15);
    JTextField startTime = labelledField(panel, "Start Time (HH:MM):", "08:00", 10);
    JRadioButton wholeSeries = scopePanel(panel, "Apply to:",
        "Delete entire series", "Delete from this event onwards");

    if (confirm(panel, "Delete Events")) {
      features.deleteEvents(subject.getText(),
          startDate.getText().trim() + "T" + startTime.getText().trim(),
          wholeSeries.isSelected());
    }
  }

  /**
   * Shows the form for editing a calendar's name or timezone.
   */
  void openEditCalendar() {
    JPanel panel = verticalPanel();
    JComboBox<String> calendarBox = new JComboBox<>(calendars.get().toArray(new String[0]));
    panel.add(new JLabel("Calendar:"));
    panel.add(calendarBox);
    JComboBox<String> propertyBox = new JComboBox<>(new String[] {"name", "timezone"});
    panel.add(new JLabel("Property:"));
    panel.add(propertyBox);
    JTextField newValue = labelledField(panel, "New value:", "", 15);

    if (confirm(panel, "Edit Calendar") && calendarBox.getSelectedItem() != null) {
      features.editCalendar(calendarBox.getSelectedItem().toString(),
          propertyBox.getSelectedItem().toString(), newValue.getText().trim());
    }
  }

  /**
   * Prompts for a file name and requests an export with the given extension.
   *
   * @param extension the file extension ("csv" or "ics")
   */
  void openExport(String extension) {
    String name = JOptionPane.showInputDialog(parent,
        "Enter file name (the ." + extension + " extension is added automatically):");
    if (name == null || name.isBlank()) {
      return;
    }
    features.exportCalendar(name.trim() + "." + extension);
  }

  /**
   * Shows the form for copying a range of events to another calendar.
   */
  void openCopyEvents() {
    JPanel panel = verticalPanel();
    JTextField fromDate = labelledField(panel, "From Date (YYYY-MM-DD):", defaultDate(), 12);
    JTextField toDate = labelledField(panel, "To Date (YYYY-MM-DD):", defaultDate(), 12);
    JComboBox<String> targetBox = new JComboBox<>(calendars.get().toArray(new String[0]));
    panel.add(new JLabel("Target calendar:"));
    panel.add(targetBox);
    JTextField targetStart = labelledField(panel, "Target Start Date (YYYY-MM-DD):",
        defaultDate(), 12);

    if (confirm(panel, "Copy Events") && targetBox.getSelectedItem() != null) {
      features.copyEvents(fromDate.getText().trim(), toDate.getText().trim(),
          targetBox.getSelectedItem().toString(), targetStart.getText().trim());
    }
  }

  /**
   * Shows the form for checking busy/available status at a date and time.
   */
  void openShowStatus() {
    JPanel panel = verticalPanel();
    JTextField date = labelledField(panel, "Date (YYYY-MM-DD):", defaultDate(), 12);
    JTextField time = labelledField(panel, "Time (HH:MM):", "09:00", 8);
    if (confirm(panel, "Show Status")) {
      features.showStatus(date.getText().trim() + "T" + time.getText().trim());
    }
  }

  /**
   * Shows the form for listing events within a date/time range.
   */
  void openRangeView() {
    JPanel panel = verticalPanel();
    JTextField startDate = labelledField(panel, "Start Date (YYYY-MM-DD):", defaultDate(), 12);
    JTextField startTime = labelledField(panel, "Start Time (HH:MM):", "00:00", 8);
    JTextField endDate = labelledField(panel, "End Date (YYYY-MM-DD):", defaultDate(), 12);
    JTextField endTime = labelledField(panel, "End Time (HH:MM):", "23:59", 8);
    if (confirm(panel, "View Events in Range")) {
      features.requestEventsInRange(
          startDate.getText().trim() + "T" + startTime.getText().trim(),
          endDate.getText().trim() + "T" + endTime.getText().trim());
    }
  }

  // ---------------------------------------------------------------- helpers

  private String defaultDate() {
    LocalDate date = selectedDate.get();
    return date == null ? LocalDate.now().toString() : date.toString();
  }

  private boolean confirm(JPanel panel, String title) {
    return JOptionPane.showConfirmDialog(parent, panel, title,
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
  }

  private JPanel verticalPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    return panel;
  }

  private JTextField labelledField(JPanel panel, String label, String defaultVal, int cols) {
    JTextField field = (cols == 0) ? new JTextField(defaultVal) : new JTextField(defaultVal, cols);
    panel.add(new JLabel(label));
    panel.add(field);
    return field;
  }

  private JRadioButton scopePanel(JPanel panel, String heading, String wholeText, String fromText) {
    JRadioButton whole = new JRadioButton(wholeText);
    JRadioButton fromHere = new JRadioButton(fromText);
    ButtonGroup group = new ButtonGroup();
    group.add(whole);
    group.add(fromHere);
    whole.setSelected(true);

    JPanel scope = new JPanel();
    scope.setLayout(new BoxLayout(scope, BoxLayout.Y_AXIS));
    scope.add(new JLabel(heading));
    scope.add(whole);
    scope.add(fromHere);
    panel.add(scope);
    return whole;
  }

  private Map<String, JCheckBox> weekdayCheckBoxes(JPanel panel) {
    JPanel weekdaysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    weekdaysPanel.add(new JLabel("Weekdays:"));
    Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
    for (String key : "MTWRFSU".split("")) {
      JCheckBox cb = new JCheckBox(key);
      checkBoxes.put(key, cb);
      weekdaysPanel.add(cb);
    }
    panel.add(weekdaysPanel);
    return checkBoxes;
  }

  private String selectedWeekdays(Map<String, JCheckBox> checkBoxes) {
    return "MTWRFSU".chars()
        .mapToObj(c -> String.valueOf((char) c))
        .filter(k -> checkBoxes.get(k).isSelected())
        .collect(Collectors.joining());
  }

  private JComboBox<String> propertyDropdown(JPanel panel) {
    String[] properties = Arrays.stream(EventProperty.values()).map(Enum::name)
        .toArray(String[]::new);
    JComboBox<String> box = new JComboBox<>(properties);
    panel.add(new JLabel("Property to edit:"));
    panel.add(box);
    return box;
  }

  private String propertyValue(Event event, EventProperty prop) {
    try {
      String getter = "get" + prop.name().charAt(0) + prop.name().substring(1).toLowerCase();
      Method method = event.getClass().getMethod(getter);
      Object value = method.invoke(event);
      return value != null ? value.toString() : "";
    } catch (ReflectiveOperationException ex) {
      return "";
    }
  }
}
