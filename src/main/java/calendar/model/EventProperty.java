package calendar.model;

/**
 * Enum representing the editable properties of an {@link Event}.
 */
public enum EventProperty {
  SUBJECT,
  START,
  END,
  LOCATION,
  DESCRIPTION,
  STATUS;

  /**
   * Converts a string to the corresponding {@link EventProperty}.
   *
   * @param value the string representation of the property
   * @return the corresponding EventProperty
   * @throws IllegalArgumentException if the string does not match any property
   */
  public static EventProperty fromString(String value) {
    for (EventProperty prop : EventProperty.values()) {
      if (prop.name().equalsIgnoreCase(value)) {
        return prop;
      }
    }
    throw new IllegalArgumentException("Unknown event property: " + value);
  }
}
