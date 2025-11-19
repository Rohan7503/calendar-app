package calendar.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

class BetterParser extends CommandParser implements Parser {

  private Map<String, String> args;
  private String regex;

  /**
   * Parses the given user input string and returns a {@link ParsedCommand} object
   * representing the corresponding calendar-related command.
   *
   * @param input takes the query string.
   * @return a {@code ParsedCommand} representing the parsed command and its arguments.
   * @throws IllegalArgumentException if the input is invalid
   *                                  or cannot be parsed into a valid command.
   */
  @Override
  public ParsedCommand parse(String input) throws IllegalArgumentException {
    try {
      input = input.replaceAll("\\s+", " ").trim();
      if (input.matches("^create\\s+calendar(\\s+|$).*")) {
        return parseCreateCalendar(input);
      } else if (input.matches("^use\\s+calendar(\\s+|$).*")) {
        return parseUseCalendar(input);
      } else if (input.matches("^edit\\s+calendar(\\s+|$).*")) {
        return parseEditCalendar(input);
      } else if (input.matches("^copy\\s+event(\\s+|$).*")) {
        return parseEventCopy(input);
      } else if (input.matches("^copy\\s+events(\\s+|$).*")) {
        return parseEventRangeCopy(input);
      } else {
        return super.parse(input);
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  //***************************** Helper Methods ***************************/

  /**
   * Parses a {@code create calendar} command string and constructs a corresponding
   * {@link ParsedCommand} object representing the operation and its arguments.
   *
   * @param input the raw user input string representing a create calendar command.
   * @return a {@code ParsedCommand} containing the {@code CREATE_CALENDAR} type
   *          and its parsed arguments (calendar name and timezone).
   * @throws IllegalArgumentException if the command syntax is invalid or the timezone
   *                                  format does not conform to the expected standard.
   */
  private ParsedCommand parseCreateCalendar(String input) throws IllegalArgumentException {
    try {
      regex = "^create\\s+calendar\\s+--name\\s+(\\S+)\\s+--timezone\\s+([^\\s\\\\]+)$";
      Matcher matcher = createMatcher(regex, input);
      if (matcher.find()) {
        String timezone = matcher.group(2);
        validateTimezoneFormat(timezone);
        args = parseArgs(Arrays.asList("calName", "timezone"),
            matcher
        );
      } else {
        throw new IllegalArgumentException(
            "Invalid create calendar syntax. Expected Format: " + System.lineSeparator()
                + "create calendar --name <calName> --timezone area/location");
      }

      return new ParsedCommand(CommandType.CREATE_CALENDAR, args);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Parses an {@code edit calendar} command string and constructs a corresponding
   * {@link ParsedCommand} object representing the edit operation and its arguments.
   *
   * @param input the raw user input string representing a edit calendar command.
   * @return a {@code ParsedCommand} containing the {@code EDIT_CALENDAR} type
   *         and its parsed arguments (calendar name and timezone).
   * @throws IllegalArgumentException if the command syntax is invalid or the timezone
   *                                  format does not conform to the expected standard.
   */
  private ParsedCommand parseEditCalendar(String input) throws IllegalArgumentException {
    try {
      regex = "^edit\\s+calendar\\s+--name\\s+(\\S+)\\s+--property\\s+(\\S+)\\s+(\\S+)$";
      Matcher matcher = createMatcher(regex, input);
      if (matcher.find()) {
        String propertyName = matcher.group(2);
        if (propertyName.equals("timezone")) {
          String timezone = matcher.group(3);
          validateTimezoneFormat(timezone);
        }
        args = parseArgs(Arrays.asList("calName", "propertyName", "newValue"),
            matcher
        );
      } else {
        throw new IllegalArgumentException("Invalid Edit calendar syntax. Expected Format: "
            + System.lineSeparator()
            + "edit calendar --name <name-of-calendar> --property "
            + "<property-name> <new-property-value>");
      }
      return new ParsedCommand(CommandType.EDIT_CALENDAR, args);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Parses an {@code use calendar} command string and constructs a corresponding
   * {@link ParsedCommand} object representing the use calendar operation and its arguments.
   *
   * @param input the raw user input string representing a use calendar command.
   * @return a {@code ParsedCommand} containing the {@code USE_CALENDAR} type
   *         and its parsed arguments (calendar name and timezone).
   * @throws IllegalArgumentException if the command syntax is invalid or the timezone
   *                                  format does not conform to the expected standard.
   */
  private ParsedCommand parseUseCalendar(String input) throws IllegalArgumentException {
    regex = "^use\\s+calendar\\s+--name\\s+(\\S+)$";
    Matcher matcher = createMatcher(regex, input);
    if (matcher.find()) {
      args = parseArgs(List.of("calName"),
          matcher
      );
    } else {
      throw new IllegalArgumentException("Invalid use calendar syntax. Expected Format: "
          + System.lineSeparator() + "use calendar --name <name-of-calendar>");
    }
    return new ParsedCommand(CommandType.USE_CALENDAR, args);
  }

  /**
   * Parses an {@code eventCopy calendar} command string and constructs a corresponding
   * {@link ParsedCommand} object representing the eventCopy calendar
   * operation and its arguments.
   *
   * @param input the raw user input string representing a eventCopy calendar command.
   * @return a {@code ParsedCommand} containing the {@code EVENT_COPY_CALENDAR} type
   *         and its parsed arguments (calendar name and timezone).
   * @throws IllegalArgumentException if the command syntax is invalid or the timezone
   *                                  format does not conform to the expected standard.
   */
  private ParsedCommand parseEventCopy(String input) throws IllegalArgumentException {
    regex = "^copy\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "on\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "--target\\s+(\"[^\"]+\"|\\S+)\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$";
    Matcher matcher = createMatcher(regex, input);
    if (matcher.find()) {
      args = parseArgs(Arrays.asList(
              "eventName", "sourceStartTime", "targetCalendar", "targetStartTime"),
          matcher
      );
    } else {
      String msg = "Invalid Event copy syntax. Expected Format: " + System.lineSeparator()
          + "copy event <eventName> on <dateStringTtimeString> --target <calendarName> "
          + "to <dateStringTtimeString>";
      throw handleCopyEventSyntaxError(input, msg);
    }
    return new ParsedCommand(CommandType.COPY_EVENT, args);
  }

  /**
   * Parses an {@code eventRangeCopy calendar} command string and constructs a corresponding
   * {@link ParsedCommand} object representing the eventRangeCopy calendar
   * operation and its arguments.
   *
   * @param input the raw user input string representing a eventRangeCopy calendar command.
   * @return a {@code ParsedCommand} containing the {@code EVENT_RANGE_COPY_CALENDAR} type
   *         and its parsed arguments (calendar name and timezone).
   * @throws IllegalArgumentException if the command syntax is invalid or the timezone
   *                                  format does not conform to the expected standard.
   */
  private ParsedCommand parseEventRangeCopy(String input) throws IllegalArgumentException {
    regex = "^copy\\s+events\\s+"
        + "on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
        + "--target\\s+(\\S+)\\s+to\\s+(\\d{4}-\\d{2}-\\d{2})$";

    String rangeRegex = "^copy\\s+events\\s+"
        + "between\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
        + "and\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
        + "--target\\s+(\\S+)\\s+to\\s+(\\d{4}-\\d{2}-\\d{2})$";

    Matcher matcher = createMatcher(regex, input);
    Matcher rangeMatcher = createMatcher(rangeRegex, input);

    if (matcher.find()) {
      args = new HashMap<>();
      args.put("startDate", matcher.group(1));
      args.put("endDate", matcher.group(1));
      args.put("targetCalendar", matcher.group(2));
      args.put("targetStartDate", matcher.group(3));
    } else if (rangeMatcher.find()) {
      args = parseArgs(Arrays.asList(
              "startDate", "endDate", "targetCalendar", "targetStartDate"),
          rangeMatcher
      );
    } else {
      String[] parts = input.split("\\s+");
      if (parts.length >= 8) {
        throw new IllegalArgumentException(
            "Invalid date-time format: " + System.lineSeparator()
                + "Expected format: YYYY-MM-DDThh::mm (e.g., 2025-11-04T10:30)"
        );
      }
      throw new IllegalArgumentException("Invalid Event Range Copy syntax. Expected Format: "
          + System.lineSeparator()
          + "copy events on <dateString> --target <calendarName> to <dateString>"
          + System.lineSeparator()
          + "OR" + System.lineSeparator()
          + "copy events between <dateString> and <dateString>"
          + "--target <calendarName> to <dateString>" + System.lineSeparator());
    }

    return new ParsedCommand(CommandType.COPY_EVENTS, args);
  }

  /**
   * Handles syntax-related errors encountered while parsing a {@code copy event} command.
   *
   * @param input the original user input string that caused the parsing error.
   * @param msg   a custom error message describing the parsing issue.
   * @return always throws an {@link IllegalArgumentException}; the return type is provided
   *         for syntactic consistency within parsing logic.
   * @throws IllegalArgumentException always thrown, containing either a specific
   *                                  date-time format error or the provided generic message.
   */
  private IllegalArgumentException handleCopyEventSyntaxError(String input, String msg)
      throws IllegalArgumentException {
    String[] parts = input.split("\\s+");
    if (parts.length >= 9) {
      return new IllegalArgumentException(
          "Invalid date-time format: " + System.lineSeparator()
              + "Expected format: YYYY-MM-DDThh::mm (e.g., 2025-11-04T10:30)"
      );
    }
    return new IllegalArgumentException(msg);
  }


  /**
   * Validates that the given timezone string conforms to the standard
   * {@code Area/Location} format used by the IANA timezone database.
   *
   * @param timezone the timezone string to validate.
   * @throws IllegalArgumentException if the timezone format is invalid or
   *                                  does not follow the {@code Area/Location} pattern.
   */
  private void validateTimezoneFormat(String timezone) throws IllegalArgumentException {
    try {
      if (!timezone.matches("^[A-Za-z_]+/[A-Za-z_]+(?:/[A-Za-z_]+)*$")) {
        throw new IllegalArgumentException(
            "Invalid timezone format. "
                + "Expected format: 'Area/Location' (e.g., 'America/New_York')");
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }


}
