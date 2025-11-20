package calendar.view;

import calendar.controller.Features;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * This class represents a GUI for the calendar application using Java Swing. It displays the
 * list of available calendars, a month-view calendar grid, events on a selected day in the grid,
 * and provides the option to create, edit, or modify events.
 */
public class CalGuiImpl extends JFrame implements CalGuiInterface {
  private Features features;

  private final JPanel calendarListPanel = new JPanel();
  private final JPanel monthGridPanel = new JPanel();
  private final JPanel dayEventsPanel = new JPanel();
  private LocalDate selectedDate = null;
  private YearMonth displayedMonth;
  private final List<JButton> dayButtons = new ArrayList<>();
  private final JButton createCalendarButton =  new JButton("Create Calendar");
  private final JButton createEventButton = new  JButton("Create Event");

  /**
   * Initialise the swing components.
   */
  public CalGuiImpl() {
    super("Calendar App");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1100, 700);

    setLayout(new BorderLayout());

    addTopActionButtons(BorderLayout.NORTH);
    createCalendarListPanel(BorderLayout.WEST);
    createDayEventsPanel(BorderLayout.EAST);
    createMonthGridPanel(BorderLayout.CENTER);
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
    for (JButton b : dayButtons) {
      b.addActionListener(evt -> {
        LocalDate day = (LocalDate) b.getClientProperty("date");
        if (day != null) {
          selectedDate = day;
          refreshDayButtonStyles();
          features.requestEventsForDay(day);
        }
      });
    }
  }

  @Override
  public void showCalendars(List<String> calendarNames, String activeCalendar) {
    calendarListPanel.removeAll();

    for (String name : calendarNames) {
      JButton btn = new JButton(name);
      btn.setAlignmentX(Component.CENTER_ALIGNMENT);
      if (name.equals(activeCalendar)) {
        btn.setBackground(Color.LIGHT_GRAY);
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

      JLabel eventLabel = new JLabel(String.format("%s | %s → %s", e.getSubject(),
          e.getStart().format(dateFormatter), e.getEnd().format(dateFormatter)));
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
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message);
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
        JOptionPane.ERROR_MESSAGE);
  }


  private void addTopActionButtons(Object position) {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

    JButton createSeriesCountBtn = new JButton("Create Series (Count)");
    createSeriesCountBtn.addActionListener(e -> openCreateEventSeriesDialog(true));

    JButton createSeriesUntilBtn = new JButton("Create Series (Until Date)");
    createSeriesUntilBtn.addActionListener(e -> openCreateEventSeriesDialog(false));

    JButton editEventsBtn = new JButton("Edit Multiple Events");
    editEventsBtn.addActionListener(e -> openEditEventsDialog());

    buttonPanel.add(createCalendarButton);
    buttonPanel.add(createEventButton);
    buttonPanel.add(createSeriesCountBtn);
    buttonPanel.add(createSeriesUntilBtn);
    buttonPanel.add(editEventsBtn);

    this.add(buttonPanel, position);
    this.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  private void createCalendarListPanel(Object position) {
    calendarListPanel.setLayout(new BoxLayout(calendarListPanel, BoxLayout.Y_AXIS));
    calendarListPanel.setPreferredSize(new Dimension(200, 400));
    add(new JScrollPane(calendarListPanel), position);
  }

  private void createDayEventsPanel(Object position) {
    dayEventsPanel.setLayout(new BoxLayout(dayEventsPanel, BoxLayout.Y_AXIS));
    dayEventsPanel.setPreferredSize(new Dimension(400, 400));
    add(new JScrollPane(dayEventsPanel), position);
  }

  private void createMonthGridPanel(Object position) {
    displayedMonth = YearMonth.now();
    monthGridPanel.setLayout(new GridLayout(0, 7));
    buildInitialMonthGrid();
    monthGridPanel.setPreferredSize(new Dimension(350, 250));
    JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
    gridWrapper.add(monthGridPanel);
    add(gridWrapper, position);
  }


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
      b.putClientProperty("date", date);
      styleDayButton(b, date);
      dayButtons.add(b);
      monthGridPanel.add(b);
    }
    monthGridPanel.revalidate();
    monthGridPanel.repaint();
  }

  private void styleDayButton(JButton b, LocalDate date) {
    b.setMargin(new Insets(1, 1, 1, 1));
    b.setPreferredSize(new Dimension(40, 30));
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

  private void refreshDayButtonStyles() {
    for (JButton b : dayButtons) {
      LocalDate date = (LocalDate) b.getClientProperty("date");
      if (date != null) {
        styleDayButton(b, date);
      }
    }
    monthGridPanel.repaint();
  }



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

  private void openCreateEventDialog() {
    JTextField subject = new JTextField();
    JTextField startTime = new JTextField("08:00");
    JTextField endTime = new JTextField("17:00");
    JTextField startDate =  new JTextField((selectedDate == null)
        ? "2025-01-01"
        : selectedDate.toString());
    JTextField endDate = new JTextField((selectedDate == null)
        ? "2025-01-01"
        : selectedDate.toString());

    JPanel p = new JPanel(new BorderLayout());
    JPanel firstRow = new JPanel(new GridLayout(1, 1));
    firstRow.add(new JLabel("Subject:"));
    firstRow.add(subject);
    JPanel remRows = new JPanel(new GridLayout(0, 2));
    remRows.add(new JLabel("Start Date (YYYY-MM-DD):"));
    remRows.add(startDate);
    remRows.add(new JLabel("Start Time (HH:MM):"));
    remRows.add(startTime);
    remRows.add(new JLabel("End Date (YYYY-MM-DD):"));
    remRows.add(endDate);
    remRows.add(new JLabel("End Time (HH:MM):"));
    remRows.add(endTime);
    p.add(firstRow, BorderLayout.NORTH);
    p.add(remRows, BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(this, p, "Create Event",
        JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      try {
        features.createEvent(
            subject.getText(),
            LocalDateTime.parse(startDate.getText() + "T" + startTime.getText()),
            LocalDateTime.parse(endDate.getText() + "T" + endTime.getText())
        );
      } catch (DateTimeParseException ex) {
        showError("Invalid date format.");
      }
    }
  }

  private void openCreateEventSeriesDialog(boolean isByCount) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JTextField subjectField = new JTextField(20);
    panel.add(new JLabel("Subject:"));
    panel.add(subjectField);

    JTextField startDateField = new JTextField(
        (selectedDate == null) ? LocalDate.now().toString() : selectedDate.toString(), 10);
    JTextField startTimeField = new JTextField("08:00", 5);
    JTextField endTimeField = new JTextField("17:00", 5);

    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    timePanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    timePanel.add(startDateField);
    timePanel.add(new JLabel("Start Time (HH:MM):"));
    timePanel.add(startTimeField);
    timePanel.add(new JLabel("End Time (HH:MM):"));
    timePanel.add(endTimeField);
    panel.add(timePanel);

    JPanel weekdaysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    weekdaysPanel.add(new JLabel("Weekdays:"));
    Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
    for (String key : "MTWRFSU".split("")) {
      JCheckBox cb = new JCheckBox(key);
      checkBoxes.put(key, cb);
      weekdaysPanel.add(cb);
    }
    panel.add(weekdaysPanel);

    JTextField countOrUntilField = new JTextField(5);
    panel.add(new JLabel(isByCount ? "Number of occurrences:" : "Until date (YYYY-MM-DD):"));
    panel.add(countOrUntilField);

    int result = JOptionPane.showConfirmDialog(this, panel,
        isByCount ? "Create Event Series (Count)" : "Create Event Series (Until Date)",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String subject = subjectField.getText().trim();
        LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
        LocalDateTime start = LocalDateTime.parse(startDate + "T" + startTimeField.getText().trim());
        LocalDateTime end = LocalDateTime.parse(startDate + "T" + endTimeField.getText().trim());

        StringBuilder weekdays = new StringBuilder();
        for (String key : "MTWRFSU".split("")) {
          if (checkBoxes.get(key).isSelected()) {
            weekdays.append(key);
          }
        }
        if (isByCount) {
          int count = Integer.parseInt(countOrUntilField.getText().trim());
          features.createEventSeriesByCount(subject, start, end, weekdays.toString(), count);
        } else {
          LocalDate untilDate = LocalDate.parse(countOrUntilField.getText().trim());
          features.createEventSeriesUntilDate(subject, start, end, weekdays.toString(), untilDate);
        }
      } catch (Exception ex) {
        showError("Error creating event series: " + ex.getMessage());
      }
    }
  }


  private void openEditEventDialog(Event e) {}
  private void openEditEventsDialog(){}

}
