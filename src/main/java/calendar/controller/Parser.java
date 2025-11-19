package calendar.controller;

/**
 * Represents a generic parser interface responsible for interpreting user input
 * and converting it into a structured {@link ParsedCommand} object.
 */
interface Parser {


  /**
   * Parses a user-provided input string into a {@link ParsedCommand} object.
   *
   * @param input the raw command string entered by the user; must not be {@code null} or empty
   * @return a {@link ParsedCommand} representing the structured form of the input command
   * @throws IllegalArgumentException if the input is malformed, incomplete, or cannot be parsed
   */
  ParsedCommand parse(String input) throws IllegalArgumentException;
}
