package calendar.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 * Helpers for building tidy two-column input forms with real date and time pickers. Using
 * {@link JSpinner} date editors means the date/time fields are always well-formed, removing a
 * whole class of input errors, while {@link #form()} and {@link #addRow} give every dialog a
 * consistent, aligned layout.
 */
final class FormComponents {

  private static final String DATE_PATTERN = "yyyy-MM-dd";
  private static final String TIME_PATTERN = "HH:mm";

  private FormComponents() {
  }

  /**
   * Creates an empty, vertically-stacked form panel.
   *
   * @return a panel laid out for {@link #addRow} calls
   */
  static JPanel form() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(UiFactory.padding(1));
    return panel;
  }

  /**
   * Adds a labelled field row to a form created by {@link #form()}.
   *
   * @param form  the form panel
   * @param label the row label
   * @param field the input component
   */
  static void addRow(JPanel form, String label, JComponent field) {
    int row = form.getComponentCount() / 2;

    GridBagConstraints labelConstraints = new GridBagConstraints();
    labelConstraints.gridx = 0;
    labelConstraints.gridy = row;
    labelConstraints.anchor = GridBagConstraints.LINE_END;
    labelConstraints.insets = new Insets(Theme.UNIT / 2, 0, Theme.UNIT / 2, Theme.UNIT);
    JLabel labelComponent = new JLabel(label);
    labelComponent.setFont(Theme.BODY);
    form.add(labelComponent, labelConstraints);

    GridBagConstraints fieldConstraints = new GridBagConstraints();
    fieldConstraints.gridx = 1;
    fieldConstraints.gridy = row;
    fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    fieldConstraints.weightx = 1.0;
    fieldConstraints.insets = new Insets(Theme.UNIT / 2, 0, Theme.UNIT / 2, 0);
    form.add(field, fieldConstraints);
  }

  /**
   * Creates a date picker initialized to the given date.
   *
   * @param initial the initial date
   * @return a spinner that edits a {@code yyyy-MM-dd} date
   */
  static JSpinner datePicker(LocalDate initial) {
    JSpinner spinner = new JSpinner(new SpinnerDateModel());
    spinner.setEditor(new JSpinner.DateEditor(spinner, DATE_PATTERN));
    spinner.setValue(toDate(initial, LocalTime.NOON));
    return spinner;
  }

  /**
   * Creates a time picker initialized to the given time.
   *
   * @param initial the initial time
   * @return a spinner that edits a {@code HH:mm} time
   */
  static JSpinner timePicker(LocalTime initial) {
    JSpinner spinner = new JSpinner(new SpinnerDateModel());
    spinner.setEditor(new JSpinner.DateEditor(spinner, TIME_PATTERN));
    spinner.setValue(toDate(LocalDate.now(), initial));
    return spinner;
  }

  /**
   * Reads the date from a date picker as a {@code yyyy-MM-dd} string.
   *
   * @param spinner the date picker
   * @return the formatted date
   */
  static String dateValue(JSpinner spinner) {
    return new SimpleDateFormat(DATE_PATTERN).format((Date) spinner.getValue());
  }

  /**
   * Reads the time from a time picker as a {@code HH:mm} string.
   *
   * @param spinner the time picker
   * @return the formatted time
   */
  static String timeValue(JSpinner spinner) {
    return new SimpleDateFormat(TIME_PATTERN).format((Date) spinner.getValue());
  }

  private static Date toDate(LocalDate date, LocalTime time) {
    return Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant());
  }
}
