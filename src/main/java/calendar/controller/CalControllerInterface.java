package calendar.controller;

/**
 * Represents the main controller for the calendar application.
 * The controller is responsible for managing the flow of the program and
 * coordinating interactions between the model and the view.
 * It provides both interactive and headless modes of operation.
 */
public interface CalControllerInterface {

  /**
   * Runs the program in interactive mode.
   * This mode allows the user to interact with the application through a
   * command-line or graphical interface, depending on the implementation.
   */
  void runInteractive();

  /**
   * Runs the program in headless mode using commands from the given input file.
   * This mode executes the program without any user interaction, by providing
   * the commands in a text file.
   */
  void runHeadless();

  /**
   * RUns the program in GUI mode. The controller implements the callback functions
   * that are used query data from the model and pass to the view.
   */
  void runGui();
}
