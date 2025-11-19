package calendar.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * This class takes the type of command and the
 * arguments parsed from the query string,
 * and provides the data to controller.
 */
public class ParsedCommand {
  private final CommandType command;
  private final Map<String, String> argument;

  /**
   * Constructor to initialize
   * command type and arguments.
   *
   * @param command  takes the CommandType
   * @param argument takes the map of arguments.
   */
  public ParsedCommand(CommandType command,
                       Map<String, String> argument) {
    this.command = command;
    this.argument = argument;
  }

  /**
   * getter to get the type of command
   * to be called upon by the controller for a given query.
   *
   * @return CommandType enum.
   */
  public CommandType getCommandType() {
    return command;
  }

  /**
   * getter to get the arguments from the query.
   * these args are then given to the model by
   * the controller.
   *
   * @return the map of args.
   */
  public Map<String, String> getArguments() {
    return argument;
  }
}

