package calendar.model;

/**
 * This enum represents the values that the location of an event can take.
 */
public enum EventLocation {
  PHYSICAL,
  ONLINE;


  /**
   * Converts a string to the corresponding Location enum.
   * Case-insensitive.
   *
   * @param s the string to convert
   * @return the corresponding Location
   * @throws IllegalArgumentException if no match is found
   */
  public static EventLocation fromString(String s) {
    for (EventLocation loc : EventLocation.values()) {
      if (loc.name().equalsIgnoreCase(s)) {
        return loc;
      }
    }
    throw new IllegalArgumentException("Invalid event location: " + s);
  }
}
