package calendar.controller;


import calendar.model.MultiCalModelInterface;
import calendar.model.SingleCalModelInterface;
import calendar.view.CalGuiImpl;
import calendar.view.CalGuiInterface;
import calendar.view.CalViewInterface;
import java.time.ZoneId;
import java.util.Scanner;

/**
 * Implementation of the {@link CalControllerInterface}.
 * This class is responsible for coordinating the flow of the calendar application.
 * It receives input from the user (either interactively or headless), interprets commands
 * via the CommandParser, and calls the appropriate methods in the model and view.
 */
public class CalControllerImpl implements CalControllerInterface {

  private final MultiCalModelInterface calModel;
  private final CalViewInterface view;
  private final Parser parser;
  private final Readable readable;
  private SingleCalModelInterface activeCalendar;

  /**
   * Constructs a controller for the calendar application.
   *
   * @param calModel the model implementation to be used by this controller; must not be null
   * @param view     the view implementation to be used by this controller; must not be null
   * @throws IllegalArgumentException if either {@code model} or {@code view} is null
   */
  public CalControllerImpl(MultiCalModelInterface calModel,
                           CalViewInterface view,
                           Readable in)
      throws IllegalArgumentException {
    if (view == null || calModel == null) {
      throw new IllegalArgumentException("Model and view must not be null.");
    }

    this.calModel = calModel;
    this.view = view;
    this.readable = in;
    this.parser = new BetterParser();
  }

  @Override
  public void runInteractive() {
    Scanner scanner = new Scanner(readable);
    handleMenuDisplay();
    while (activeCalendar == null || scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();

      if (command.equalsIgnoreCase("exit")) {
        view.displayMessage("Exiting..." + System.lineSeparator());
        break;
      }

      try {
        if (handleCalendarManagementCommands(command)) {
          continue;
        }
      } catch (IllegalArgumentException e) {
        view.displayError(e.getMessage());
        continue;
      }

      if (command.toLowerCase().startsWith("menu")) {
        handleMenuDisplay();
        continue;
      }
      if (activeCalendar == null) {
        handleStartMessage();
        continue;
      }
      try {
        handleCommand(command);
      } catch (IllegalArgumentException | IllegalStateException e) {
        view.displayError(e.getMessage());
      }
    }
    scanner.close();
  }

  @Override
  public void runHeadless() {
    Scanner scanner = new Scanner(readable);
    boolean foundExit = false;
    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.equalsIgnoreCase("exit")) {
        foundExit = true;
        view.displayMessage("Exiting...");
        break;
      }

      if (command.isEmpty()) {
        continue;
      }

      try {
        if (handleCalendarManagementCommands(command)) {
          continue;
        }
      } catch (IllegalArgumentException e) {
        view.displayError(e.getMessage());
      }

      if (activeCalendar != null) {
        try {
          handleCommand(command);
        } catch (IllegalArgumentException e) {
          view.displayError(e.getMessage());
        }
      }
    }
    if (!foundExit) {
      view.displayError("Missing exit command at the end of the input file!");
    }
  }

  @Override
  public void runGui() {
    CalGuiInterface guiView = new CalGuiImpl();
    Features guiController = new GuiControllerImpl(calModel, guiView);
    guiView.addFeatures(guiController);

    ZoneId systemZone = ZoneId.systemDefault();
    calModel.createCalendar("Default", systemZone);
    calModel.useCalendar("Default");

    guiView.showCalendars(calModel.listCalendars(), "Default");
    guiView.showGui();
  }

  //****************** Helper Methods ****************************

  /**
   * Process the input queries given by user
   * and pass the arguments to model.
   * segregate query based on create,edit,print,export,show_status,
   * and accordingly call the model with relevant function.
   *
   * @param command takes the user query in form of string.
   * @throws IllegalArgumentException if either query keywords are invalid
   *                                  or property formats are not correct
   *                                  like date-time, weekdays etc.
   */
  private void handleCommand(String command) throws IllegalArgumentException {
    try {
      ParsedCommand parsedCommand = parser.parse(command);

      switch (parsedCommand.getCommandType()) {
        case CREATE_EVENT:
          CreateEventCommand createEvent = new CreateEventCommand(calModel, view, parsedCommand);
          createEvent.execute();
          break;

        case CREATE_SERIES_COUNT:
          CreateSeriesCount createSeries = new CreateSeriesCount(calModel, view, parsedCommand);
          createSeries.execute();
          break;

        case CREATE_SERIES_UNTIL:
          CreateSeriesUntil createSeriesUntil =
              new CreateSeriesUntil(calModel, view, parsedCommand);
          createSeriesUntil.execute();
          break;

        case EDIT_EVENTS:
          EditEvents editEvents = new EditEvents(calModel, view, parsedCommand);
          editEvents.execute();
          break;

        case EDIT_EVENT:
          EditEvent editEvent = new EditEvent(
              calModel, view, parsedCommand);
          editEvent.execute();
          break;

        case DELETE_EVENT:
          DeleteEvent deleteEvent = new DeleteEvent(
              calModel, view, parsedCommand);
          deleteEvent.execute();
          break;

        case DELETE_EVENTS:
          DeleteEvents deleteEvents = new DeleteEvents(
              calModel, view, parsedCommand);
          deleteEvents.execute();
          break;

        case GET_EVENTS_RANGE:
          GetEvents getEvents = new GetEvents(
              calModel, view, parsedCommand);
          getEvents.execute();
          break;

        case SHOW_STATUS:
          ShowStatus showStatus = new ShowStatus(
              calModel, view, parsedCommand);
          showStatus.execute();
          break;

        case USE_CALENDAR:
          UseCalendar useCalendar = new UseCalendar(
              calModel, view, parsedCommand, activeCalendar);
          activeCalendar = useCalendar.execute();
          break;

        case CREATE_CALENDAR:
          CreateCalendar createCalendar = new CreateCalendar(
              calModel, view, parsedCommand);
          createCalendar.execute();
          break;

        case EDIT_CALENDAR:
          EditCalendar editCalendar = new EditCalendar(
              calModel, view, parsedCommand);
          editCalendar.execute();
          break;

        case COPY_EVENT:
          CopyEvent copyEvent = new CopyEvent(
              calModel, view, parsedCommand);
          copyEvent.execute();
          break;

        case COPY_EVENTS:
          CopyEvents copyEvents = new CopyEvents(
              calModel, view, parsedCommand);
          copyEvents.execute();
          break;

        case EXPORT:
          Export exportEvents = new Export(
              calModel, view, parsedCommand);
          exportEvents.execute();
          break;

        default:
          throw new IllegalArgumentException();
      }
    } catch (IllegalStateException | IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Handles commands that can be executed before a calendar is selected.
   * This includes creating calendars, editing calendars, and selecting a calendar.
   *
   * @param command the command string to process
   * @return true if the command was a calendar setup command, false otherwise
   */
  private boolean handleCalendarManagementCommands(String command) throws IllegalArgumentException {
    if (command.toLowerCase().startsWith("help")) {
      try {
        handleHelpMenu(command);
      } catch (IllegalArgumentException e) {
        view.displayError(e.getMessage());
      }
      return true;
    }
    String normalizedCommand = command.trim().replaceAll("\\s+", " ").toLowerCase();
    boolean isCalendarOperations = normalizedCommand.startsWith("create calendar")
        || normalizedCommand.startsWith("edit calendar")
        || normalizedCommand.startsWith("copy event")
        || normalizedCommand.startsWith("copy events");
    boolean isUseCommand = normalizedCommand.startsWith("use");
    if (isCalendarOperations || isUseCommand) {
      try {
        handleCommand(command);
      } catch (IllegalArgumentException | IllegalStateException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      return true;
    }

    return false;
  }

  /**
   * Provides query syntax strings to view according to the command
   * passed by user.
   *
   * @param command takes the command string passed by user.
   */
  private void handleHelpMenu(String command) throws IllegalArgumentException {
    try {
      switch (command) {
        case "help\\use":
          String useCommand =
              System.lineSeparator() + "use calendar --name <name-of-calendar>"
                  + System.lineSeparator();
          view.displayMessage(useCommand);
          break;
        case "help\\createCal":
          String createCalCommand =
              System.lineSeparator() + "create calendar --name <calName> --timezone area/location"
                  + System.lineSeparator();
          view.displayMessage(createCalCommand);
          break;
        case "help\\create":
          String createCommands =
              System.lineSeparator()
                  + "- create event <eventSubject> "
                  + "from <dateStringTtimeString> to <dateStringTtimeString>"
                  + System.lineSeparator()
                  + "- create event <eventSubject> "
                  + "from <dateStringTtimeString> to <dateStringTtimeString> "
                  + "repeats <weekdays> for <N> times" + System.lineSeparator()
                  + "- create event <eventSubject> "
                  + "from <dateStringTtimeString> to <dateStringTtimeString> "
                  + "repeats <weekdays> until <dateString>" + System.lineSeparator()
                  + "- create event <eventSubject> on <dateString>" + System.lineSeparator()
                  + "- create event <eventSubject> on <dateString> repeats <weekdays> for <N> times"
                  + System.lineSeparator()
                  + "- create event <eventSubject> on <dateString> "
                  + "repeats <weekdays> until <dateString>"
                  + System.lineSeparator();

          view.displayMessage(createCommands);
          break;

        case "help\\edit":
          String editCommands = System.lineSeparator()
              + "- edit event <property> <eventSubject> "
              + "from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>"
              + System.lineSeparator()
              + "- edit events <property> <eventSubject> from <dateStringTtimeString> "
              + "with <NewPropertyValue>"
              + System.lineSeparator()
              + "- edit series <property> <eventSubject> from <dateStringTtimeString> "
              + "with <NewPropertyValue>"
              + System.lineSeparator();

          view.displayMessage(editCommands);
          break;
        case "help\\print":
          String printCommands = System.lineSeparator()
              + "- print events on <dateString>"
              + System.lineSeparator()
              + "- print events from <dateStringTtimeString> to <dateStringTtimeString>"
              + System.lineSeparator();
          view.displayMessage(printCommands);
          break;

        case "help\\misc":
          String printMisc = System.lineSeparator()
              + "- export cal fileName.csv"
              + System.lineSeparator()
              + "- show status on <dateStringTtimeString>"
              + System.lineSeparator();
          view.displayMessage(printMisc);
          break;

        default:
          throw new IllegalArgumentException(
              "Invalid command: " + command + System.lineSeparator());

      }
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }


  /**
   * Displays the menu for interactive mode of the calendar.
   * provides set of instructions to the user for exit and
   * query syntax help.
   */
  private void handleMenuDisplay() {
    view.displayMessage("Welcome to I-Cal" + System.lineSeparator()
        + "For Exiting the calendar type : Exit" + System.lineSeparator()
        + "For Help type :" + System.lineSeparator()
        + "help\\use --> for selecting a calendar syntax" + System.lineSeparator()
        + "help\\createCal --> for creating a calendar syntax" + System.lineSeparator()
        + "help\\create --> for create event command syntax" + System.lineSeparator()
        + "help\\edit --> for edit event command syntax" + System.lineSeparator()
        + "help\\print --> for print event command syntax" + System.lineSeparator()
        + "help\\misc --> for miscellaneous commands syntax" + System.lineSeparator()
        + "For Menu type: Menu" + System.lineSeparator()
        + "To start the program, select a calendar or create one if not exist."
        + System.lineSeparator()
        + "Enter Command here" + System.lineSeparator());
  }

  private void handleStartMessage() {
    view.displayMessage("Command not valid"
        + System.lineSeparator()
        + "To start the program, select a calendar"
        + " or create one if not exist."
        + System.lineSeparator()
        + "For selecting a calendar type : help\\use"
        + System.lineSeparator()
        + "For creating a calendar type : help\\createCal"
        + System.lineSeparator()
    );
  }
}
