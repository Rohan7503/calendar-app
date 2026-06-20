package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import calendar.model.EventProperty;
import java.awt.Component;
import java.awt.FlowLayout;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * Builds and shows the application's input forms and forwards the collected input to the
 * {@link Features} callbacks. Forms use date/time pickers and a consistent two-column layout, and
 * validate input inline (re-prompting with a message) before invoking the controller. Keeping all
 * dialog assembly here keeps the frame and panels focused on layout and rendering.
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
   * Shows the create-calendar form.
   */
  void openCreateCalendar() {
    JPanel form = FormComponents.form();
    JTextField name = new JTextField(16);
    JTextField timezone = new JTextField("America/New_York", 16);
    FormComponents.addRow(form, "Name:", name);
    FormComponents.addRow(form, "Timezone:", timezone);

    if (confirm(form, "Create Calendar",
        () -> name.getText().isBlank() ? "Calendar name is required." : null)) {
      features.createCalendar(name.getText().trim(), timezone.getText().trim());
    }
  }

  /**
   * Shows the create-event form, supporting both timed and all-day events.
   */
  void openCreateEvent() {
    JPanel form = FormComponents.form();
    JTextField subject = new JTextField(18);
    JSpinner startDate = FormComponents.datePicker(defaultDate());
    JSpinner startTime = FormComponents.timePicker(LocalTime.of(9, 0));
    JSpinner endDate = FormComponents.datePicker(defaultDate());
    JSpinner endTime = FormComponents.timePicker(LocalTime.of(10, 0));
    JCheckBox allDay = new JCheckBox("All-day event");
    allDay.addItemListener(e -> {
      boolean timed = !allDay.isSelected();
      startTime.setEnabled(timed);
      endDate.setEnabled(timed);
      endTime.setEnabled(timed);
    });

    FormComponents.addRow(form, "Subject:", subject);
    FormComponents.addRow(form, "Start date:", startDate);
    FormComponents.addRow(form, "Start time:", startTime);
    FormComponents.addRow(form, "End date:", endDate);
    FormComponents.addRow(form, "End time:", endTime);
    FormComponents.addRow(form, "", allDay);

    if (confirm(form, "Create Event",
        () -> validateCreateEvent(subject, startDate, startTime, endDate, endTime, allDay))) {
      if (allDay.isSelected()) {
        features.createAllDayEvent(subject.getText().trim(), FormComponents.dateValue(startDate));
      } else {
        features.createEvent(subject.getText().trim(),
            dateTime(startDate, startTime), dateTime(endDate, endTime));
      }
    }
  }

  /**
   * Shows the create-series form for either a fixed count or an until-date.
   *
   * @param byCount {@code true} for a count-based series, {@code false} for an until-date series
   */
  void openCreateSeries(boolean byCount) {
    JPanel form = FormComponents.form();
    JTextField subject = new JTextField(18);
    JSpinner startDate = FormComponents.datePicker(defaultDate());
    JSpinner startTime = FormComponents.timePicker(LocalTime.of(9, 0));
    JSpinner endTime = FormComponents.timePicker(LocalTime.of(10, 0));
    Map<String, JCheckBox> weekdays = weekdayCheckBoxes();
    JTextField count = new JTextField(6);
    JSpinner until = FormComponents.datePicker(defaultDate());

    FormComponents.addRow(form, "Subject:", subject);
    FormComponents.addRow(form, "Start date:", startDate);
    FormComponents.addRow(form, "Start time:", startTime);
    FormComponents.addRow(form, "End time:", endTime);
    FormComponents.addRow(form, "Weekdays:", weekdayRow(weekdays));
    FormComponents.addRow(form, byCount ? "Occurrences:" : "Until date:",
        byCount ? count : until);

    String title = byCount ? "Create Event Series (Count)" : "Create Event Series (Until Date)";
    if (confirm(form, title, () -> validateSeries(subject, weekdays, byCount, count))) {
      String start = dateTime(startDate, startTime);
      String end = dateTime(startDate, endTime);
      String days = selectedWeekdays(weekdays);
      if (byCount) {
        features.createEventSeriesByCount(subject.getText().trim(), start, end, days,
            count.getText().trim());
      } else {
        features.createEventSeriesUntilDate(subject.getText().trim(), start, end, days,
            FormComponents.dateValue(until));
      }
    }
  }

  /**
   * Shows the edit form for a single event, pre-filling the value of the chosen property.
   *
   * @param event the event to edit
   */
  void openEditEvent(Event event) {
    JPanel form = FormComponents.form();
    JComboBox<String> propertyBox = propertyDropdown();
    JTextField newValue = new JTextField(event.getSubject(), 18);
    propertyBox.addActionListener(e -> {
      EventProperty prop = EventProperty.valueOf((String) propertyBox.getSelectedItem());
      newValue.setText(propertyValue(event, prop));
    });
    FormComponents.addRow(form, "Property:", propertyBox);
    FormComponents.addRow(form, "New value:", newValue);

    if (confirm(form, "Edit Event: " + event.getSubject(),
        () -> newValue.getText().isBlank() ? "A new value is required." : null)) {
      features.editEvent(propertyBox.getSelectedItem().toString(), event.getSubject(),
          event.getStart().toString(), event.getEnd().toString(), newValue.getText().trim());
    }
  }

  /**
   * Shows the form for editing multiple events, with a whole-series / from-here scope choice.
   */
  void openEditEvents() {
    JPanel form = FormComponents.form();
    JTextField subject = new JTextField(18);
    JSpinner startDate = FormComponents.datePicker(defaultDate());
    JSpinner startTime = FormComponents.timePicker(LocalTime.of(9, 0));
    JComboBox<String> propertyBox = propertyDropdown();
    JTextField newValue = new JTextField(18);
    JRadioButton wholeSeries = new JRadioButton("Edit entire series", true);
    JRadioButton fromHere = new JRadioButton("Edit from this event onwards");

    FormComponents.addRow(form, "Subject:", subject);
    FormComponents.addRow(form, "Start date:", startDate);
    FormComponents.addRow(form, "Start time:", startTime);
    FormComponents.addRow(form, "Property:", propertyBox);
    FormComponents.addRow(form, "New value:", newValue);
    FormComponents.addRow(form, "Apply to:", scopeBox(wholeSeries, fromHere));

    if (confirm(form, "Edit Events",
        () -> subject.getText().isBlank() ? "Subject is required." : null)) {
      features.editEvents(propertyBox.getSelectedItem().toString(), subject.getText().trim(),
          dateTime(startDate, startTime), newValue.getText(), wholeSeries.isSelected());
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
    JPanel form = FormComponents.form();
    JTextField subject = new JTextField(18);
    JSpinner startDate = FormComponents.datePicker(defaultDate());
    JSpinner startTime = FormComponents.timePicker(LocalTime.of(9, 0));
    JRadioButton wholeSeries = new JRadioButton("Delete entire series", true);
    JRadioButton fromHere = new JRadioButton("Delete from this event onwards");

    FormComponents.addRow(form, "Subject:", subject);
    FormComponents.addRow(form, "Start date:", startDate);
    FormComponents.addRow(form, "Start time:", startTime);
    FormComponents.addRow(form, "Apply to:", scopeBox(wholeSeries, fromHere));

    if (confirm(form, "Delete Events",
        () -> subject.getText().isBlank() ? "Subject is required." : null)) {
      features.deleteEvents(subject.getText().trim(), dateTime(startDate, startTime),
          wholeSeries.isSelected());
    }
  }

  /**
   * Shows the form for editing a calendar's name or timezone.
   */
  void openEditCalendar() {
    JPanel form = FormComponents.form();
    JComboBox<String> calendarBox = new JComboBox<>(calendars.get().toArray(new String[0]));
    JComboBox<String> propertyBox = new JComboBox<>(new String[] {"name", "timezone"});
    JTextField newValue = new JTextField(16);
    FormComponents.addRow(form, "Calendar:", calendarBox);
    FormComponents.addRow(form, "Property:", propertyBox);
    FormComponents.addRow(form, "New value:", newValue);

    if (confirm(form, "Edit Calendar",
        () -> newValue.getText().isBlank() ? "A new value is required." : null)
        && calendarBox.getSelectedItem() != null) {
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
    JPanel form = FormComponents.form();
    JSpinner fromDate = FormComponents.datePicker(defaultDate());
    JSpinner toDate = FormComponents.datePicker(defaultDate());
    JComboBox<String> targetBox = new JComboBox<>(calendars.get().toArray(new String[0]));
    JSpinner targetStart = FormComponents.datePicker(defaultDate());
    FormComponents.addRow(form, "From date:", fromDate);
    FormComponents.addRow(form, "To date:", toDate);
    FormComponents.addRow(form, "Target calendar:", targetBox);
    FormComponents.addRow(form, "Target start date:", targetStart);

    if (confirm(form, "Copy Events", () -> validateDateOrder(fromDate, toDate))
        && targetBox.getSelectedItem() != null) {
      features.copyEvents(FormComponents.dateValue(fromDate), FormComponents.dateValue(toDate),
          targetBox.getSelectedItem().toString(), FormComponents.dateValue(targetStart));
    }
  }

  /**
   * Shows the form for checking busy/available status at a date and time.
   */
  void openShowStatus() {
    JPanel form = FormComponents.form();
    JSpinner date = FormComponents.datePicker(defaultDate());
    JSpinner time = FormComponents.timePicker(LocalTime.of(9, 0));
    FormComponents.addRow(form, "Date:", date);
    FormComponents.addRow(form, "Time:", time);
    if (confirm(form, "Show Status", () -> null)) {
      features.showStatus(dateTime(date, time));
    }
  }

  /**
   * Shows the form for listing events within a date/time range.
   */
  void openRangeView() {
    JPanel form = FormComponents.form();
    JSpinner startDate = FormComponents.datePicker(defaultDate());
    JSpinner startTime = FormComponents.timePicker(LocalTime.MIDNIGHT);
    JSpinner endDate = FormComponents.datePicker(defaultDate());
    JSpinner endTime = FormComponents.timePicker(LocalTime.of(23, 59));
    FormComponents.addRow(form, "Start date:", startDate);
    FormComponents.addRow(form, "Start time:", startTime);
    FormComponents.addRow(form, "End date:", endDate);
    FormComponents.addRow(form, "End time:", endTime);
    if (confirm(form, "View Events in Range",
        () -> validateRange(startDate, startTime, endDate, endTime))) {
      features.requestEventsInRange(dateTime(startDate, startTime), dateTime(endDate, endTime));
    }
  }

  // ---------------------------------------------------------------- helpers

  private LocalDate defaultDate() {
    LocalDate date = selectedDate.get();
    return date == null ? LocalDate.now() : date;
  }

  private boolean confirm(JPanel form, String title, Supplier<String> validator) {
    while (true) {
      int result = JOptionPane.showConfirmDialog(parent, form, title,
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (result != JOptionPane.OK_OPTION) {
        return false;
      }
      String error = validator.get();
      if (error == null) {
        return true;
      }
      JOptionPane.showMessageDialog(parent, error, "Invalid input",
          JOptionPane.WARNING_MESSAGE);
    }
  }

  private String dateTime(JSpinner date, JSpinner time) {
    return FormComponents.dateValue(date) + "T" + FormComponents.timeValue(time);
  }

  private String validateCreateEvent(JTextField subject, JSpinner startDate, JSpinner startTime,
                                     JSpinner endDate, JSpinner endTime, JCheckBox allDay) {
    if (subject.getText().isBlank()) {
      return "Subject is required.";
    }
    if (!allDay.isSelected() && !isEndAfterStart(
        dateTime(startDate, startTime), dateTime(endDate, endTime))) {
      return "End must be after start.";
    }
    return null;
  }

  private String validateSeries(JTextField subject, Map<String, JCheckBox> weekdays,
                                boolean byCount, JTextField count) {
    if (subject.getText().isBlank()) {
      return "Subject is required.";
    }
    if (selectedWeekdays(weekdays).isEmpty()) {
      return "Select at least one weekday.";
    }
    if (byCount) {
      try {
        if (Integer.parseInt(count.getText().trim()) <= 0) {
          return "Occurrences must be a positive number.";
        }
      } catch (NumberFormatException e) {
        return "Occurrences must be a whole number.";
      }
    }
    return null;
  }

  private String validateDateOrder(JSpinner from, JSpinner to) {
    return FormComponents.dateValue(to).compareTo(FormComponents.dateValue(from)) < 0
        ? "The end date must not be before the start date." : null;
  }

  private String validateRange(JSpinner startDate, JSpinner startTime,
                               JSpinner endDate, JSpinner endTime) {
    return isEndAfterStart(dateTime(startDate, startTime), dateTime(endDate, endTime))
        ? null : "End must be after start.";
  }

  private boolean isEndAfterStart(String start, String end) {
    try {
      return !LocalDateTime.parse(end).isBefore(LocalDateTime.parse(start));
    } catch (DateTimeParseException e) {
      return true;
    }
  }

  private JPanel scopeBox(JRadioButton whole, JRadioButton fromHere) {
    ButtonGroup group = new ButtonGroup();
    group.add(whole);
    group.add(fromHere);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(whole);
    panel.add(fromHere);
    return panel;
  }

  private Map<String, JCheckBox> weekdayCheckBoxes() {
    Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
    for (String key : "MTWRFSU".split("")) {
      checkBoxes.put(key, new JCheckBox(key));
    }
    return checkBoxes;
  }

  private JPanel weekdayRow(Map<String, JCheckBox> checkBoxes) {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    for (JCheckBox box : checkBoxes.values()) {
      row.add(box);
    }
    return row;
  }

  private String selectedWeekdays(Map<String, JCheckBox> checkBoxes) {
    return "MTWRFSU".chars()
        .mapToObj(c -> String.valueOf((char) c))
        .filter(k -> checkBoxes.get(k).isSelected())
        .collect(Collectors.joining());
  }

  private JComboBox<String> propertyDropdown() {
    String[] properties = Arrays.stream(EventProperty.values()).map(Enum::name)
        .toArray(String[]::new);
    return new JComboBox<>(properties);
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
