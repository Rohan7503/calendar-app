package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import calendar.model.EventProperty;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * This class represents a GUI for the calendar application using Java Swing. It displays the
 * list of available calendars, a month-view calendar grid, events on a selected day in the grid,
 * and provides the option to create, edit, or modify events, and create/use new calendars.
 */
public class CalGuiImpl extends JFrame implements CalGuiInterface {
  private Features features;

  private final JPanel calendarListPanel;
  private final JPanel monthGridPanel;
  private final JPanel dayEventsPanel;
  private final List<JButton> dayButtons;
  private final JButton createCalendarButton;
  private final JButton createEventButton;
  private final JButton createSeriesCountButton;
  private final JButton createSeriesUntilButton;
  private final JButton editEventsButton;
  private LocalDate selectedDate;
  private YearMonth displayedMonth;

  /**
   * Initialise the swing components.
   */
  public CalGuiImpl() {
    super("Calendar App");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    calendarListPanel = new JPanel();
    monthGridPanel = new JPanel();
    dayEventsPanel = new JPanel();
    dayButtons = new ArrayList<>();

    createCalendarButton = new JButton("Create Calendar");
    createEventButton = new JButton("Create Event");
    createSeriesCountButton = new JButton("Create Series (Count)");
    createSeriesUntilButton = new JButton("Create Series (Until Date)");
    editEventsButton = new JButton("Edit Multiple Events");

    setLayout(new BorderLayout());
    addTopActionButtons(BorderLayout.NORTH);
    createScrollablePanel("Calendars", calendarListPanel, BorderLayout.WEST);
    createScrollablePanel("Day Events", dayEventsPanel, BorderLayout.EAST);
    createMonthGridPanel(BorderLayout.CENTER);
    pack();
  }


  @Override
  public void showGui() {
    setVisible(true);
  }

  @Override
  public void addFeatures(Features features) {
    this.features = features;

    createCalendarButton.addActionListener(e -> openCreateCalendarDialog());
    createEventButton.addActionListener(e -> openCreateEventDialog());
    createSeriesCountButton.addActionListener(e -> openCreateEventSeriesDialog(true));
    createSeriesUntilButton.addActionListener(e -> openCreateEventSeriesDialog(false));
    editEventsButton.addActionListener(e -> openEditEventsDialog());
  }

  @Override
  public void showCalendars(List<String> calendarNames, String activeCalendar) {
    calendarListPanel.removeAll();

    for (String name : calendarNames) {
      JButton btn = new JButton(name);
      btn.setAlignmentX(Component.CENTER_ALIGNMENT);
      if (name.equals(activeCalendar)) {
        btn.setBackground(Color.ORANGE);
      }
      btn.putClientProperty("calendarName", name);
      btn.addActionListener(e -> features.selectCalendar(name));
      calendarListPanel.add(btn);
      calendarListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    calendarListPanel.revalidate();
    calendarListPanel.repaint();
  }


  @Override
  public void showEventsForDay(LocalDate day, List<Event> events) {
    dayEventsPanel.removeAll();

    JLabel header = new JLabel("Events on: " + day);
    header.setAlignmentX(Component.CENTER_ALIGNMENT);
    header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
    dayEventsPanel.add(header);
    dayEventsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    for (Event e : events) {
      JPanel eventPanel = new JPanel();
      eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.X_AXIS));

      String labelText = e.isAllDay()
          ? String.format("%s | All day", e.getSubject())
          : String.format("%s | %s to %s", e.getSubject(),
              e.getStart().format(dateFormatter), e.getEnd().format(dateFormatter));
      JLabel eventLabel = new JLabel(labelText);
      eventLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

      JButton editBtn = new JButton("Edit");
      editBtn.addActionListener(ae -> openEditEventDialog(e));

      eventPanel.add(eventLabel);
      eventPanel.add(editBtn);
      dayEventsPanel.add(eventPanel);
      dayEventsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    dayEventsPanel.revalidate();
    dayEventsPanel.repaint();
  }

  @Override
  public void refreshEvents() {
    if (selectedDate == null) {
      return;
    }
    features.requestEventsForDay(selectedDate.toString());
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message);
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
        JOptionPane.ERROR_MESSAGE);
  }


  /**
   * Helper method to add buttons at a given position.
   *
   * @param position The position to add the buttons at
   */
  private void addTopActionButtons(Object position) {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
    buttonPanel.add(createCalendarButton);
    buttonPanel.add(createEventButton);
    buttonPanel.add(createSeriesCountButton);
    buttonPanel.add(createSeriesUntilButton);
    buttonPanel.add(editEventsButton);
    this.add(buttonPanel, position);
  }

  /**
   * Helper method to create and add a scrollable panel with a heading to the main component at
   * a given position.
   *
   * @param heading  The text to display in the heading label
   * @param panel    The panel to make scrollable
   * @param position The position to add the panel at
   */
  private void createScrollablePanel(String heading, JPanel panel, Object position) {
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JPanel wrapper = new JPanel();
    wrapper.setLayout(new BorderLayout());

    JLabel title = new JLabel(heading, SwingConstants.CENTER);
    title.setFont(new Font("SansSerif", Font.BOLD, 16));
    title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

    wrapper.add(title, BorderLayout.NORTH);
    wrapper.add(panel, BorderLayout.CENTER);
    JScrollPane scroll = new JScrollPane(wrapper);
    scroll.setPreferredSize(new Dimension(400, 400));
    add(scroll, position);
  }

  /**
   * Helper method to create a month view calendar grid at a given position.
   *
   * @param position The position to create the panel at
   */
  private void createMonthGridPanel(Object position) {
    displayedMonth = YearMonth.now();
    monthGridPanel.setLayout(new GridLayout(0, 7));
    buildInitialMonthGrid();

    JPanel monthWrapper = new JPanel();
    monthWrapper.setLayout(new BoxLayout(monthWrapper, BoxLayout.Y_AXIS));
    JPanel monthHeader = createMonthHeaderPanel();
    monthWrapper.add(monthHeader);
    monthWrapper.add(monthGridPanel);

    JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
    centerWrapper.add(monthWrapper);
    add(centerWrapper, position);
  }

  /**
   * Helper method to create a header panel for the month grid, having the month name and buttons to
   * navigate to preceding and succeeding months.
   *
   * @return The panel consisting of the header
   */
  private JPanel createMonthHeaderPanel() {
    JPanel monthHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));

    JButton prevBtn = new JButton("<");
    JButton nextBtn = new JButton(">");

    JLabel monthLabel =
        new JLabel(displayedMonth.getMonth().name() + " " + displayedMonth.getYear(),
            SwingConstants.CENTER);

    prevBtn.addActionListener(e -> {
      displayedMonth = displayedMonth.minusMonths(1);
      monthLabel.setText(displayedMonth.getMonth().name() + " " + displayedMonth.getYear());
      buildInitialMonthGrid();
    });

    nextBtn.addActionListener(e -> {
      displayedMonth = displayedMonth.plusMonths(1);
      monthLabel.setText(displayedMonth.getMonth().name() + " " + displayedMonth.getYear());
      buildInitialMonthGrid();
    });

    monthHeader.add(prevBtn);
    monthHeader.add(monthLabel);
    monthHeader.add(nextBtn);

    return monthHeader;
  }

  /**
   * Helper method to build the buttons (dates) of the month grid.
   */
  private void buildInitialMonthGrid() {
    monthGridPanel.removeAll();
    dayButtons.clear();

    String[] weekdayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String name : weekdayNames) {
      JLabel lbl = new JLabel(name, SwingConstants.CENTER);
      lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
      monthGridPanel.add(lbl);
    }

    LocalDate first = displayedMonth.atDay(1);
    int startColumn = first.getDayOfWeek().getValue() % 7;
    int daysInMonth = displayedMonth.lengthOfMonth();
    for (int i = 0; i < startColumn; i++) {
      monthGridPanel.add(new JLabel(""));
    }
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = displayedMonth.atDay(day);
      JButton b = new JButton(String.valueOf(day));
      b.addActionListener(evt -> {
        selectedDate = date;
        refreshDayButtonStyles();
        features.requestEventsForDay(date.toString());
      });
      b.putClientProperty("date", date);
      styleDayButton(b, date);
      dayButtons.add(b);
      monthGridPanel.add(b);
    }
    monthGridPanel.revalidate();
    monthGridPanel.repaint();
  }

  /**
   * Helper method to style individual buttons (dates) of the month grid. The selected day is
   * highlighted in a specific color, and weekends are highlighted in a specific color.
   *
   * @param b    The button to style
   * @param date The date value of the button
   */
  private void styleDayButton(JButton b, LocalDate date) {
    b.setMargin(new Insets(1, 1, 1, 1));
    b.setPreferredSize(new Dimension(80, 60));
    b.setOpaque(true);
    b.setBorderPainted(true);
    b.setForeground(Color.BLACK);
    b.setBackground(Color.WHITE);

    DayOfWeek dow = date.getDayOfWeek();
    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
      b.setBackground(new Color(230, 240, 255));
    }
    if (selectedDate != null && selectedDate.equals(date)) {
      b.setBackground(new Color(255, 215, 140));
      b.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
    } else {
      b.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }
  }

  /**
   * Refresh the buttons' styles when they are re-rendered.
   */
  private void refreshDayButtonStyles() {
    for (JButton b : dayButtons) {
      LocalDate date = (LocalDate) b.getClientProperty("date");
      if (date != null) {
        styleDayButton(b, date);
      }
    }
    monthGridPanel.repaint();
  }

  /**
   * Open a dialog box with the necessary fields for creating a new calendar.
   */
  private void openCreateCalendarDialog() {
    String name = JOptionPane.showInputDialog(this, "Enter calendar name:");
    if (name == null || name.isBlank()) {
      return;
    }
    String tz = JOptionPane.showInputDialog(this, "Enter timezone (e.g., America/New_York):");
    if (tz == null || tz.isBlank()) {
      return;
    }
    features.createCalendar(name, tz);
  }

  /**
   * Open a dialog box with the necessary fields for creating an event. If a day is currently
   * selected, certain fields are autopopulated.
   */
  private void openCreateEventDialog() {
    JPanel p = new JPanel(new BorderLayout());
    JPanel firstRow = new JPanel(new GridLayout(1, 1));
    JPanel remRows = new JPanel(new GridLayout(0, 2));

    JTextField subject = createLabelTextField(firstRow, "Subject:", "", 0);
    JTextField startDate = createLabelTextField(remRows, "Start Date (YYYY-MM-DD):",
        (selectedDate == null) ? "2025-01-01" : selectedDate.toString(), 0);
    JTextField startTime = createLabelTextField(remRows, "Start Time (HH:MM):", "08:00", 0);
    JTextField endDate = createLabelTextField(remRows, "End Date (YYYY-MM-DD):",
        (selectedDate == null) ? "2025-01-01" : selectedDate.toString(), 0);
    JTextField endTime = createLabelTextField(remRows, "End Time (HH:MM):", "17:00", 0);

    JCheckBox allDay = new JCheckBox("All-day event");
    allDay.addItemListener(e -> {
      boolean timed = !allDay.isSelected();
      startTime.setEnabled(timed);
      endDate.setEnabled(timed);
      endTime.setEnabled(timed);
    });

    p.add(firstRow, BorderLayout.NORTH);
    p.add(remRows, BorderLayout.CENTER);
    p.add(allDay, BorderLayout.SOUTH);

    int result = JOptionPane.showConfirmDialog(this, p, "Create Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      if (allDay.isSelected()) {
        features.createAllDayEvent(subject.getText(), startDate.getText().trim());
      } else {
        features.createEvent(
            subject.getText(),
            startDate.getText().trim() + "T" + startTime.getText().trim(),
            endDate.getText().trim() + "T" + endTime.getText().trim()
        );
      }
    }
  }

  /**
   * Open a dialog box with the necessary fields for creating an event series. If a day is currently
   * selected, certain fields are autopopulated.
   */
  private void openCreateEventSeriesDialog(boolean isByCount) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JTextField subjectField = createLabelTextField(panel, "Subject:", "", 20);
    JTextField startDateField = createLabelTextField(panel, "Start Date (YYYY-MM-DD):",
        (selectedDate == null) ? "2025-01-01" : selectedDate.toString(), 10);
    JTextField startTimeField = createLabelTextField(panel, "Start Time (HH:MM):", "08:00", 5);
    JTextField endTimeField = createLabelTextField(panel, "End Time (HH:MM):", "17:00", 5);

    panel.add(timePanel);

    Map<String, JCheckBox> checkBoxes = createWeekdayCheckBoxes(panel);
    JTextField countOrUntilField = createLabelTextField(panel,
        isByCount ? "Number of occurrences:" : "Until date (YYYY-MM-DD):", "", 5);

    int result = JOptionPane.showConfirmDialog(this, panel,
        isByCount ? "Create Event Series (Count)" : "Create Event Series (Until Date)",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      handleCreateEventSeries(subjectField, startDateField, startTimeField, endTimeField,
          countOrUntilField, checkBoxes, isByCount);
    }
  }

  /**
   * Open a dialog box with the necessary fields for editing an event. Certain fields are
   * autopopulated based on the chosen event.
   */
  private void openEditEventDialog(Event event) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JComboBox<String> propertyBox = createPropertyDropdown(panel);
    JTextField newValueField = createLabelTextField(panel, "New Value:", event.getSubject(), 20);

    propertyBox.addActionListener(e -> {
      String selected = (String) propertyBox.getSelectedItem();
      EventProperty prop = EventProperty.valueOf(selected);
      String currentValue = getEventPropertyValue(event, prop);
      newValueField.setText(currentValue);
    });

    int result = JOptionPane.showConfirmDialog(this, panel,
        "Edit Event: " + event.getSubject(),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String property = propertyBox.getSelectedItem().toString();
      String newValue = newValueField.getText().trim();

      features.editEvent(
          property,
          event.getSubject(),
          event.getStart().toString(),
          event.getEnd().toString(),
          newValue
      );
    }
  }

  /**
   * Open a dialog box with the necessary fields for editing multiple events. If a day is currently
   * selected, certain fields are autopopulated.
   * A radio button is provided to switch between editing all events belonging to the same series
   * as the one being edited, or to only edit events belonging to the same series as the one
   * being edited, and occurring after the event (inclusive of the event).
   * If the event does not belong to a series, edition just occurs to that event alone.
   */
  private void openEditEventsDialog() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    final JTextField subject = createLabelTextField(panel, "Subject:", "", 20);
    final JTextField startDate = createLabelTextField(panel, "Start Date (YYYY-MM-DD):",
        (selectedDate == null) ? "2025-01-01" : selectedDate.toString(), 15);
    final JTextField startTime = createLabelTextField(panel, "Start Time (HH:MM):", "08:00", 10);

    final JComboBox<String> propertyBox = createPropertyDropdown(panel);
    final JTextField newValueField = createLabelTextField(panel, "New Value:", "", 20);

    JRadioButton wholeSeries = new JRadioButton("Edit entire series");
    JRadioButton fromHere = new JRadioButton("Edit from this event onwards");
    ButtonGroup group = new ButtonGroup();
    group.add(wholeSeries);
    group.add(fromHere);
    wholeSeries.setSelected(true);

    JPanel scopePanel = new JPanel();
    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
    scopePanel.add(new JLabel("Apply change to:"));
    scopePanel.add(wholeSeries);
    scopePanel.add(fromHere);
    panel.add(scopePanel);

    int result = JOptionPane.showConfirmDialog(this, panel, "Edit Events",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      features.editEvents(propertyBox.getSelectedItem().toString(), subject.getText(),
          startDate.getText().trim() + "T" + startTime.getText().trim(),
          newValueField.getText(), wholeSeries.isSelected()
      );
    }
  }

  /**
   * Helper method to create a label and a text field with a default value and size.
   *
   * @param panel      The panel to create the components in
   * @param label      The value of the label
   * @param defaultVal The default value of the text field, if any
   * @param cols       The column span of the text field
   * @return The text field object
   */
  private JTextField createLabelTextField(JPanel panel, String label, String defaultVal, int cols) {
    JTextField field;
    if (cols == 0) {
      field = new JTextField(defaultVal);
    } else {
      field = new JTextField(defaultVal, cols);
    }
    panel.add(new JLabel(label));
    panel.add(field);
    return field;
  }

  /**
   * Helper method to create checkboxes for days of the week.
   *
   * @param panel The panel to create the checkboxes in
   * @return A map of the weekday code (M: Monday, T: Tuesday, W: Wednesday, R: Thursday,
   *         F: Friday, S: Saturday, U: Sunday) and the checkbox object
   */
  private Map<String, JCheckBox> createWeekdayCheckBoxes(JPanel panel) {
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

  /**
   * Helper method to handle creation of an event series once the user has entered input.
   *
   * @param subjectField      The JTextField object containing the subject of the event series
   * @param startDateField    The JTextField object containing the start date of the event series
   * @param startTimeField    The JTextField object containing the start time of the event series
   * @param endTimeField      The JTextField object containing the end time of the event series
   * @param countOrUntilField The JTextField object containing the number of times to repeat the
   *                          event, if {@code isByCount} is true, the date until which to
   *                          repeat the event otherwise
   * @param checkBoxes        A map of weekday codes to it's corresponding JCheckBox objects
   * @param isByCount         True if the event series is to be created by count of recurrences,
   *                          false otherwise
   */
  private void handleCreateEventSeries(JTextField subjectField, JTextField startDateField,
                                       JTextField startTimeField, JTextField endTimeField,
                                       JTextField countOrUntilField,
                                       Map<String, JCheckBox> checkBoxes, boolean isByCount) {
    String subject = subjectField.getText();
    String start = startDateField.getText().trim() + "T" + startTimeField.getText().trim();
    String end = startDateField.getText().trim() + "T" + endTimeField.getText().trim();
    String countOrUntil = countOrUntilField.getText().trim();
    String weekdays = "MTWRFSU"
        .chars()
        .mapToObj(c -> String.valueOf((char) c))
        .filter(k -> checkBoxes.get(k).isSelected())
        .collect(Collectors.joining());
    if (isByCount) {
      features.createEventSeriesByCount(subject, start, end, weekdays, countOrUntil);
    } else {
      features.createEventSeriesUntilDate(subject, start, end, weekdays, countOrUntil);
    }
  }

  /**
   * Helper method to call the appropriate getter method of Event fields, based on the property
   * specified.
   *
   * @param event The event to call the getter methods for
   * @param prop  The property of the event
   * @return The value of the field in the event object
   */
  private String getEventPropertyValue(Event event, EventProperty prop) {
    try {
      String getterName = "get" + prop.name().charAt(0) + prop.name().substring(1).toLowerCase();

      Method m = event.getClass().getMethod(getterName);
      Object value = m.invoke(event);
      return value != null ? value.toString() : "";
    } catch (Exception ex) {
      ex.printStackTrace();
      return "";
    }
  }

  /**
   * Helper method to create a drop-down of the different event properties.
   *
   * @param panel The panel to add the ComboBox to
   * @return The created ComboBox
   */
  private JComboBox<String> createPropertyDropdown(JPanel panel) {
    String[] properties = Arrays.stream(EventProperty.values()).map(Enum::name)
        .toArray(String[]::new);
    JComboBox<String> propertyBox = new JComboBox<>(properties);
    panel.add(new JLabel("Property to edit:"));
    panel.add(propertyBox);

    return propertyBox;
  }

}
