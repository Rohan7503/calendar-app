package calendar.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses command strings entered by the user.
 * Responsible for interpreting command-line input and breaking it down
 * into actions that the controller can execute.
 */
class CommandParser implements Parser {

  /**
   * takes query string and according to keywords
   * segregate to createParser,editParser,
   * printParser, show_statusParser.
   *
   * @param input takes the query string.
   * @return ParsedCommand class with args and commandType
   *         to give to the controller in order for it to make
   *         relevant calls to the model and provide model
   *         with arguments data.
   * @throws IllegalArgumentException if input string is invalid.
   */
  @Override
  public ParsedCommand parse(String input) throws IllegalArgumentException {
    input = input.replaceAll("\\s+", " ").trim();
    if (input.matches("^create(\\s+|$).*")) {
      if (input.contains("repeats")) {
        if (input.contains("times")) {
          return parseCreateSeriesForCount(input);
        } else {
          return parseCreateSeriesForUntil(input);
        }
      } else {
        return parseCreateEvent(input);
      }
    } else if (input.matches("^edit(\\s+|$).*")) {
      if (input.matches("^edit\\s+events(\\s+|$).*")
          || input.matches("^edit\\s+series(\\s+|$).*")) {
        return parseEditEvents(input);
      } else {
        return parseEditEvent(input);
      }
    } else if (input.matches("^print(\\s+|$).*")) {
      return parsePrintEventRange(input);
    } else if (input.matches("^export(\\s+|$).*")) {
      return parseExport(input);
    } else if (input.matches("^show(\\s+|$).*")) {
      return parseShow(input);
    } else {
      throw new IllegalArgumentException("Unknown command keyword: "
          + input.split("\\s+")[0]);
    }
  }


  //************************ Helper Methods ***************//

  /**
   * parses the input string to get arguments
   * for creating series repeating n times.
   *
   * @param input takes the query string.
   * @return ParsedCommand class with args and commandType
   *        to give to the controller in order for it to make
   *        relevant calls to the model and provide model
   *        with arguments data.
   */

  private ParsedCommand parseCreateSeriesForCount(String input) throws IllegalArgumentException {
    Map<String, String> args;
    String regexTimed = "^create event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "repeats\\s+([MTWRFSU]+)\\s+"
        + "for\\s+(\\d+)\\s+times$";

    String regexAllDay = "^create event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
        + "repeats\\s+([MTWRFSU]+)\\s+"
        + "for\\s+(\\d+)\\s+times$";
    Matcher timedMatcher = createMatcher(regexTimed, input);
    Matcher allDayMatcher = createMatcher(regexAllDay, input);

    if (timedMatcher.find()) {
      args = parseArgs(Arrays.asList("subject", "start", "end", "weekdays", "count"),
          timedMatcher
      );
    } else if (allDayMatcher.find()) {
      args = parseArgs(Arrays.asList("subject", "date", "weekdays", "count"),
          allDayMatcher
      );
    } else {
      throw new IllegalArgumentException("Invalid create series syntax. Expected Format: "
          + System.lineSeparator()
          + "• Timed event: create event <eventSubject> from <YYYY-MM-DDThh:mm> "
          + "to <YYYY-MM-DDThh:mm> repeats <MTWRFSU> for <N> times "
          + System.lineSeparator()
          + "• All-day event: create event <eventSubject> on <YYYY-MM-DD> "
          + "repeats <MTWRFSU> for <N> times");
    }

    return new ParsedCommand(CommandType.CREATE_SERIES_COUNT, args);
  }

  /**
   * parses the input string to get arguments
   * for creating series repeating until end-date.
   *
   * @param input takes the query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parseCreateSeriesForUntil(String input) throws IllegalArgumentException {
    Map<String, String> args;
    String regexTimed = "^create event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "repeats\\s+([MTWRFSU]+)\\s+"
        + "until\\s+(\\d{4}-\\d{2}-\\d{2})$";

    String regexAllDay = "^create event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
        + "repeats\\s+([MTWRFSU]+)\\s+"
        + "until\\s+(\\d{4}-\\d{2}-\\d{2})$";

    Matcher timedMatcher = createMatcher(regexTimed, input);
    Matcher allDayMatcher = createMatcher(regexAllDay, input);

    if (timedMatcher.find()) {
      args = parseArgs(Arrays.asList("subject", "start", "end", "weekdays", "until"),
          timedMatcher
      );
    } else if (allDayMatcher.find()) {
      args = parseArgs(Arrays.asList("subject", "date", "weekdays", "until"),
          allDayMatcher
      );
    } else {
      throw new IllegalArgumentException("Invalid create series syntax. Expected Format: "
          + System.lineSeparator()
          + "• Timed event: create event <eventSubject> "
          + "from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm> "
          + "repeats <MTWRFSU> until <YYYY-MM-DD>" + System.lineSeparator()
          + "• All-day event: create event <eventSubject> on <YYYY-MM-DD> repeats <MTWRFSU> "
          + "until <YYYY-MM-DD>");
    }

    return new ParsedCommand(CommandType.CREATE_SERIES_UNTIL, args);
  }

  /**
   * parses the input string to get arguments
   * for creating an event on given date.
   *
   * @param input takes the query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parseCreateEvent(String input) {
    Map<String, String> args;
    String regexTimed = "^create event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s*$";

    String regexAllDay = "^create event\\s+(\"[^\"]+\"|\\S+)\\s+"
        + "on\\s+(\\d{4}-\\d{2}-\\d{2})\\s*$";

    Matcher timedMatcher = createMatcher(regexTimed, input);
    Matcher allDayMatcher = createMatcher(regexAllDay, input);

    if (timedMatcher.find()) {
      args = parseArgs(Arrays.asList("subject", "start", "end"), timedMatcher);
    } else if (allDayMatcher.find()) {
      args = parseArgs(Arrays.asList("subject", "date"), allDayMatcher);
    } else {
      String msg = "Invalid create event syntax. Expected Format: "
          + System.lineSeparator()
          + "• Timed event: create event <eventSubject> "
          + "from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm>"
          + System.lineSeparator()
          + "• All-day event: create event <eventSubject> on <YYYY-MM-DD>";
      throw handleCreateEventSyntaxError(msg, input);
    }
    return new ParsedCommand(CommandType.CREATE_EVENT, args);
  }

  /**
   * parses the input string to get arguments
   * for editing event/(s) with start date-time.
   *
   * @param input takes query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parseEditEvents(String input) {
    Map<String, String> args = new HashMap<>();
    String regex = "^edit\\s+(events|series)\\s+"
        + "(subject|start|end|description|location|status)\\s+"
        + "(\"[^\"]+\"|\\S+)\\s+"
        + "from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "with\\s+(\"[^\"]+\"|\\S+)$";

    Matcher matcher = createMatcher(regex, input);
    if (matcher.find()) {
      args.put("property", matcher.group(2));
      args.put("subject", matcher.group(3));
      args.put("start", matcher.group(4));
      args.put("newValue", matcher.group(5));
      args.put("editWholeSeries",
          String.valueOf(matcher.group(1))
              .equalsIgnoreCase("series")
              ? "true" : "false");
    } else {
      String[] parts = input.split("\\s+");
      if (parts.length >= 8) {
        throw new IllegalArgumentException(
            "Invalid date-time format: " + System.lineSeparator()
                + "Expected format: YYYY-MM-DDThh:mm (e.g., 2025-11-04T10:30)"
        );
      }
      throw new IllegalArgumentException(
          "Invalid edit events syntax. Expected Format: "
              + System.lineSeparator()
              + "edit events/series <property> <eventSubject> "
              + "from <YYYY-MM-DDThh:mm> with <NewPropertyValue>");
    }
    return new ParsedCommand(CommandType.EDIT_EVENTS, args);
  }

  /**
   * parses input string to get arguments
   * for editing event with start and end date-time.
   *
   * @param input takes query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parseEditEvent(String input) {
    Map<String, String> args;
    String regex = "^edit\\s+event\\s+"
        + "(subject|start|end|description|location|status)\\s+"
        + "(\"[^\"]+\"|\\S+)\\s+"
        + "from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "with\\s+(\"[^\"]+\"|\\S+)$";

    Matcher matcher = createMatcher(regex, input);
    if (matcher.find()) {
      args = parseArgs(Arrays.asList("property", "subject", "start", "end", "newValue"),
          matcher
      );
    } else {
      String[] parts = input.split("\\s+");
      if (parts.length >= 10) {
        throw new IllegalArgumentException(
            "Invalid date-time format: " + System.lineSeparator()
                + "Expected format: YYYY-MM-DDThh:mm (e.g., 2025-11-04T10:30)"
        );
      }
      throw new IllegalArgumentException(
          "Invalid edit event syntax. Expected Format: "
              + System.lineSeparator()
              + "edit event <property> <eventSubject> "
              + "from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm> with <NewPropertyValue>");
    }
    return new ParsedCommand(CommandType.EDIT_EVENT, args);
  }

  /**
   * parses input string to get arguments
   * for printing events on a single day or in a range.
   *
   * @param input takes query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parsePrintEventRange(String input) {
    Map<String, String> args = new HashMap<>();
    String regexRange = "^print\\s+events\\s+"
        + "from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
        + "to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s*$";

    String regexOneDay = "^print\\s+events\\s+"
        + "on\\s+(\\d{4}-\\d{2}-\\d{2})\\s*$";

    Matcher matcherRange = createMatcher(regexRange, input);
    Matcher matcherOneDay = createMatcher(regexOneDay, input);

    if (matcherRange.find()) {
      args = parseArgs(Arrays.asList("start", "end"),
          matcherRange
      );
    } else if (matcherOneDay.find()) {
      args.put("date", matcherOneDay.group(1));
    } else {
      throw new IllegalArgumentException("Invalid print events syntax. Expected Format: "
          + System.lineSeparator()
          + "print events from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm>");
    }

    return new ParsedCommand(CommandType.GET_EVENTS_RANGE, args);
  }

  /**
   * parses input string to get arguments
   * for exporting .csv file of events.
   *
   * @param input takes query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parseExport(String input) {
    Map<String, String> args = new HashMap<>();
    String regex = "^export\\s+cal\\s+([A-Za-z0-9._-]+\\.(csv|ics))$";
    Matcher matcher = createMatcher(regex, input);
    if (matcher.find()) {
      args.put("path", matcher.group(1));
    } else {
      throw new IllegalArgumentException(
          "Unsupported file type. Expected Format: " + System.lineSeparator()
              + "fileName.csv || fileName.ics" + System.lineSeparator());
    }

    return new ParsedCommand(CommandType.EXPORT, args);
  }

  /**
   * parses input string to get arguments
   * for showing status as either busy or not
   * on a given day.
   *
   * @param input takes query string.
   * @return ParsedCommand class.
   */
  private ParsedCommand parseShow(String input) {
    Map<String, String> args = new HashMap<>();
    String regex = "^show\\s+status\\s+on\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$";
    Matcher matcher = createMatcher(regex, input);
    if (matcher.find()) {
      args.put("start", matcher.group(1));
    } else {
      throw new IllegalArgumentException(
          "Invalid show status syntax. Expected Format: "
              + System.lineSeparator()
              + "show status on <YYYY-MM-DDThh:mm>");
    }
    return new ParsedCommand(CommandType.SHOW_STATUS, args);
  }

  /**
   * maps the list of properties with their values.
   *
   * @param props   takes list of properties.
   * @param matcher takes matcher containing values
   *                corresponding to every property.
   * @return a map with key-property, value-propertyValue
   */
  protected Map<String, String> parseArgs(List<String> props,
                                          Matcher matcher) {
    Map<String, String> args = new HashMap<>();
    int start = 1;
    for (String prop : props) {
      args.put(prop, matcher.group(start));
      start++;
    }
    return args;
  }

  /**
   * creates a matcher to match regex with the original input.
   *
   * @param regex takes the regex.
   * @param input takes the query string.
   * @return the Matcher.
   */
  protected Matcher createMatcher(String regex, String input) {
    Pattern pattern = Pattern.compile(regex);
    return pattern.matcher(input);
  }


  private IllegalArgumentException handleCreateEventSyntaxError(String msg, String input) {
    String[] parts = input.split("\\s+");
    if (parts.length >= 7) {
      return new IllegalArgumentException(
          "Invalid date-time format: " + System.lineSeparator()
              + "Expected format: YYYY-MM-DDThh:mm (e.g., 2025-11-04T10:30)"
              + System.lineSeparator()
      );
    }
    if (parts.length >= 6) {
      return new IllegalArgumentException(
          "Invalid date format: " + System.lineSeparator()
              + "Expected format: YYYY-MM-DD (e.g., 2025-11-04)"
              + System.lineSeparator()
      );
    }
    return new IllegalArgumentException(msg);

  }


}
