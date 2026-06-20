package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is an implementation of the {@link MultiCalModelInterface} that manages
 * multiple calendars, each represented by a {@link SingleCalModelInterface}. It maintains a
 * mapping from calendar names to calendar instances and tracks the currently active calendar.
 * It supports creation, editing, selection, and event-copying operations between calendars.
 */
public class MultiCalModelImpl implements MultiCalModelInterface {
  private final Map<String, SingleCalModelInterface> calendars;
  private String activeCalendarName;

  /**
   * Constructs an empty MultiCalModelImpl with no calendars initially.
   */
  public MultiCalModelImpl() {
    this.calendars = new HashMap<>();
    this.activeCalendarName = null;
  }

  @Override
  public void createCalendar(String calName, ZoneId timezone) throws IllegalArgumentException {
    if (calName == null || calName.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    if (calendars.containsKey(calName)) {
      throw new IllegalArgumentException("A calendar with the same name already exists.");
    }
    SingleCalModelInterface newCalendar = new SingleCalModelImpl(timezone);
    calendars.put(calName, newCalendar);
  }

  @Override
  public void editCalendar(String calName, String propertyName, String newValue)
      throws IllegalArgumentException {

    SingleCalModelInterface calendar = findUniqueCalendar(calName);
    if (propertyName == null || propertyName.isBlank()) {
      throw new IllegalArgumentException("Property name cannot be null or empty.");
    }
    if (newValue == null || newValue.isBlank()) {
      throw new IllegalArgumentException("New value cannot be null or empty.");
    }
    switch (propertyName.toLowerCase()) {
      case "name":
        handleEditCalendarName(calendar, calName, newValue);
        break;
      case "timezone":
        handleEditTimezone(calendar, newValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid property.");
    }
  }

  @Override
  public void useCalendar(String calName) throws IllegalArgumentException {
    findUniqueCalendar(calName);
    activeCalendarName = calName;
  }

  @Override
  public SingleCalModelInterface getActiveCalendar() {
    if (activeCalendarName == null) {
      return null;
    }
    return calendars.get(activeCalendarName);
  }

  @Override
  public List<String> listCalendars() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public String getActiveCalendarName() {
    return activeCalendarName;
  }

  @Override
  public ZoneId getTimezone(String calName) throws IllegalArgumentException {
    return findUniqueCalendar(calName).getTimezone();
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime sourceStartTime, String targetCalendar,
                        LocalDateTime targetStartTime)
      throws IllegalArgumentException, IllegalStateException {

    SingleCalModelInterface sourceCal = requireActiveCalendar();
    SingleCalModelInterface targetCal = findUniqueCalendar(targetCalendar);
    Event eventToCopy = sourceCal.findEvent(eventName, sourceStartTime, null);

    copyEventToTarget(eventToCopy, targetCal, targetStartTime);
  }

  @Override
  public void copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalendar,
                                LocalDate targetStartDate)
      throws IllegalArgumentException, IllegalStateException {

    SingleCalModelInterface sourceCal = requireActiveCalendar();
    validateCopyDates(startDate, endDate, targetStartDate);
    SingleCalModelInterface targetCal = findUniqueCalendar(targetCalendar);

    List<Event> eventsToCopy = sourceCal.getEventsInRange(startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay().minusSeconds(1));

    copyEventsToTarget(eventsToCopy, targetCal, targetStartDate);
  }


  /**
   * Helper method to retrieve the calendar with the specified name after validating it exists.
   *
   * @param calName the name of the calendar to find
   * @return the {@link SingleCalModelInterface} associated with the given name
   * @throws IllegalArgumentException if the name is null, blank, or not found
   */
  private SingleCalModelInterface findUniqueCalendar(String calName)
      throws IllegalArgumentException {

    if (calName == null || calName.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    SingleCalModelInterface calendar = calendars.get(calName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar with this name does not exist.");
    }
    return calendar;
  }

  /**
   * Helper method to edit the name property of a calendar after checking for duplicates.
   *
   * @param calendar     The calendar to edit
   * @param currentValue The current name of the calendar
   * @param newValue     The new name for the calendar
   * @throws IllegalArgumentException If a calendar with the new name already exists
   */
  private void handleEditCalendarName(SingleCalModelInterface calendar, String currentValue,
                                      String newValue) throws IllegalArgumentException {
    if (calendars.containsKey(newValue)) {
      throw new IllegalArgumentException("A calendar with this name already exists.");
    }
    calendars.remove(currentValue);
    calendars.put(newValue, calendar);
    if (currentValue.equals(activeCalendarName)) {
      activeCalendarName = newValue;
    }
  }

  /**
   * Helper method to edit the timezone property of a calendar.
   *
   * @param calendar The calendar to edit
   * @param newValue The new timezone
   * @throws IllegalArgumentException If the timezone format is invalid or unsupported
   */
  private void handleEditTimezone(SingleCalModelInterface calendar, String newValue)
      throws IllegalArgumentException {
    try {
      ZoneId newZone = ZoneId.of(newValue);
      calendar.setTimezone(newZone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid or unsupported timezone.");
    }
  }

  /**
   * Helper method that returns the currently active calendar if it exists,
   * throws an exception otherwise.
   *
   * @return the active calendar
   * @throws IllegalStateException if no calendar is active
   */
  private SingleCalModelInterface requireActiveCalendar() throws IllegalStateException {
    SingleCalModelInterface active = getActiveCalendar();
    if (active == null) {
      throw new IllegalStateException("No active calendar selected.");
    }
    return active;
  }

  /**
   * Helper method to validate the source and target dates for copy operations.
   *
   * @param startDate       the start date of the source interval
   * @param endDate         the end date of the source interval
   * @param targetStartDate the start date in the target calendar
   * @throws IllegalArgumentException if dates are invalid
   */
  private void validateCopyDates(LocalDate startDate, LocalDate endDate, LocalDate targetStartDate)
      throws IllegalArgumentException {

    if (targetStartDate == null) {
      throw new IllegalArgumentException("Target start date cannot be null.");
    }
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null.");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date.");
    }
  }

  /**
   * Checks if an identical event already exists in the target calendar.
   *
   * @param targetCalendar the calendar to check
   * @param event          the event to check for duplicates
   * @return true if a duplicate exists, false otherwise
   */
  private boolean isDuplicateEvent(SingleCalModelInterface targetCalendar, Event event) {
    for (Event e : targetCalendar.getAllEvents()) {
      if (e.equals(event)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Converts a LocalDateTime from the source timezone to the target timezone.
   *
   * @param ldt        the LocalDateTime in the source timezone
   * @param sourceZone the source timezone
   * @param targetZone the target timezone
   * @return the LocalDateTime in the target timezone
   */
  private LocalDateTime convertToTargetTimezone(LocalDateTime ldt, ZoneId sourceZone,
                                                ZoneId targetZone) {
    ZonedDateTime sourceZdt = ldt.atZone(sourceZone);
    ZonedDateTime targetZdt = sourceZdt.withZoneSameInstant(targetZone);
    return targetZdt.toLocalDateTime();
  }


  /**
   * Copies a single event to a target calendar starting at the specified date/time,
   * preserving its duration.
   *
   * @param originalEvent  the event to copy
   * @param targetCalendar the calendar to copy the event into
   * @param newStart       the start date/time in the target calendar
   * @throws IllegalArgumentException if the event already exists in the target calendar
   */
  private void copyEventToTarget(Event originalEvent, SingleCalModelInterface targetCalendar,
                                 LocalDateTime newStart) throws IllegalArgumentException {

    Duration duration = Duration.between(originalEvent.getStart(), originalEvent.getEnd());
    Event copiedEvent = originalEvent.toBuilder()
        .start(newStart)
        .end(newStart.plus(duration))
        .build();
    targetCalendar.addEvent(copiedEvent);
  }

  /**
   * Copies a list of events to the target calendar starting at the specified start date.
   * If any event is a duplicate, none are copied. The times physically remain the same,
   * except they are converted to the timezone of the target calendar (e.g. an event that
   * starts at 2pm in the source calendar which is in EST would start at 11am in the
   * destination calendar which is in PST).
   *
   * @param eventsToCopy    the source events
   * @param targetCalendar  the target calendar
   * @param targetStartDate the start date in the target calendar
   * @throws IllegalArgumentException if any event already exists in the target calendar
   */
  private void copyEventsToTarget(List<Event> eventsToCopy, SingleCalModelInterface targetCalendar,
                                  LocalDate targetStartDate) throws IllegalArgumentException {

    ZoneId sourceZone = requireActiveCalendar().getTimezone();
    ZoneId targetZone = targetCalendar.getTimezone();
    List<Event> copyCandidates = new ArrayList<>();

    for (Event originalEvent : eventsToCopy) {
      long daysOffset = ChronoUnit.DAYS.between(eventsToCopy.get(0).getStart().toLocalDate(),
          originalEvent.getStart().toLocalDate());

      LocalDateTime baseTargetStart = targetStartDate.atStartOfDay().plusDays(daysOffset);
      LocalDateTime newStart = convertToTargetTimezone(originalEvent.getStart(),
          sourceZone, targetZone)
          .withYear(baseTargetStart.getYear())
          .withMonth(baseTargetStart.getMonthValue())
          .withDayOfMonth(baseTargetStart.getDayOfMonth());

      Duration duration = Duration.between(originalEvent.getStart(), originalEvent.getEnd());
      Event copyCandidate = originalEvent.toBuilder()
          .start(newStart)
          .end(newStart.plus(duration))
          .build();

      if (isDuplicateEvent(targetCalendar, copyCandidate)) {
        throw new IllegalArgumentException("Duplicate event in target calendar. Copy aborted.");
      }
      copyCandidates.add(copyCandidate);
    }
    for (Event copyEvent : copyCandidates) {
      targetCalendar.addEvent(copyEvent);
    }
  }

}
