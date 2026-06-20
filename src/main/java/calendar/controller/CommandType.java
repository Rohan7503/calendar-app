package calendar.controller;

/**
 * enum class to define types of commands
 * to be used by the controller to call relevant
 * methods of the model.
 */
public enum CommandType {
  CREATE_EVENT,
  CREATE_SERIES_UNTIL,
  CREATE_SERIES_COUNT,
  EDIT_EVENTS,
  EDIT_EVENT,
  DELETE_EVENT,
  DELETE_EVENTS,
  GET_EVENTS_RANGE,
  EXPORT,
  SHOW_STATUS,
  CREATE_CALENDAR,
  EDIT_CALENDAR,
  USE_CALENDAR,
  COPY_EVENT,
  COPY_EVENTS
}
