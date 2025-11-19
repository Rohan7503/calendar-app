package calendar.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This class is an implementation of the {@code SingleCalModelInterface}. It represents a single
 * calendar with timezone functionality added. It inherits all event-related operations from
 * {@code CalModelImpl}.
 * This class is intentionally made package-private since other components do not need to be able
 * to instantiate this class directly. This class can be instantiated through implementations of
 * {@code MultiCalModelInterface}.
 */
class SingleCalModelImpl extends CalModelImpl implements SingleCalModelInterface {

  private ZoneId timezone;

  /**
   * Constructs a new calendar with the given timezone.
   *
   * @param timezone the timezone for this calendar
   * @throws IllegalArgumentException if timezone is null
   */
  public SingleCalModelImpl(ZoneId timezone) {
    super();
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.timezone = timezone;
  }

  @Override
  public Event findEvent(String subject, LocalDateTime start, LocalDateTime end)
      throws IllegalArgumentException {
    return this.findUniqueEvent(subject, start, end);
  }

  @Override
  public ZoneId getTimezone() {
    return this.timezone;
  }

  @Override
  public void setTimezone(ZoneId newTimezone) {
    if (newTimezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    if (this.timezone.equals(newTimezone)) {
      return;
    }
    ZoneId oldZone = this.timezone;
    this.timezone = newTimezone;
    for (Event e : this.getAllEvents()) {
      LocalDateTime newStart = e.getStart().atZone(oldZone).withZoneSameInstant(newTimezone)
          .toLocalDateTime();
      LocalDateTime newEnd = e.getEnd().atZone(oldZone).withZoneSameInstant(newTimezone)
          .toLocalDateTime();
      this.editEvent("start", e.getSubject(), e.getStart(), e.getEnd(), newStart.toString());
      this.editEvent("end", e.getSubject(), newStart, e.getEnd(), newEnd.toString());
    }
  }
}
