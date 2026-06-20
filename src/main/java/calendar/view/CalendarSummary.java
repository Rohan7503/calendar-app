package calendar.view;

/**
 * A lightweight, immutable view-model describing a calendar for display in the sidebar: its name
 * and a human-readable timezone label. Keeps the view free of model types. Constructed by the
 * controller and consumed by the view through {@link CalGuiInterface#showCalendars}.
 */
public final class CalendarSummary {

  private final String name;
  private final String timezone;

  /**
   * Creates a calendar summary.
   *
   * @param name     the calendar name
   * @param timezone the timezone label to show
   */
  public CalendarSummary(String name, String timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  /**
   * Returns the calendar name.
   *
   * @return the name
   */
  public String name() {
    return name;
  }

  /**
   * Returns the timezone label.
   *
   * @return the timezone label
   */
  public String timezone() {
    return timezone;
  }
}
