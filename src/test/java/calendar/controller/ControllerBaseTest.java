package calendar.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewInterface;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import org.junit.Test;

/**
 * Unit tests for verifying the base command behavior and interactive functionality
 * of the {@link CalControllerImpl} class.
 * This test suite focuses on validating the controller’s initialization,
 * and user interaction flow.
 */
public class ControllerBaseTest extends CalControllerImplTest {
  private final MultiCalModelInterface mockModel = new MockModel(new StringBuilder());

  private final CalViewInterface mockView = new MockView(new StringBuilder());

  private CalControllerInterface calController;
  private File inputFile;
  private File outputFile;
  private OutputStream outStream;
  //private InputStream inStream;
  private Readable inStream = new InputStreamReader(System.in);


  @Test
  public void testNullModelView() {
    try {
      CalControllerInterface controller = new CalControllerImpl(null, null, inStream);
      fail("Expected null model,view exception");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testNullModel() {
    try {
      //CalControllerInterface controller = new CalControllerImpl(null, mockView, System.in);
      CalControllerInterface controller = new CalControllerImpl(null, null, inStream);
      fail("Expected null model,view exception");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testNullView() {
    try {
      //CalControllerInterface controller = new CalControllerImpl(mockModel, null, System.in);
      CalControllerInterface controller = new CalControllerImpl(null, null, inStream);
      fail("Expected null model,view exception");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void testInteractiveModeDisplaysWelcomeMessage() {
    String input = "exit" + System.lineSeparator();
    //InputStream in = new ByteArrayInputStream(input.getBytes());
    //calController = new CalControllerImpl(mockModel, mockView, in);
    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockView) mockView).getLogs();
    assertTrue(logs.contains("Welcome to I-Cal"));
  }

  @Test
  public void testInteractiveModeDisplayStartMessage() {
    String input = "exit" + System.lineSeparator();
    //InputStream in = new ByteArrayInputStream(input.getBytes());
    //calController = new CalControllerImpl(mockModel, mockView, in);
    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockView) mockView).getLogs();
    assertTrue(logs.contains("To start the program, select a calendar "
        + "or create one if not exist."));
  }

  @Test
  public void testInteractiveModeEmptyInput() {
    try {
      String input = "";
      //InputStream in = new ByteArrayInputStream(input.getBytes());
      //calController = new CalControllerImpl(mockModel, mockView, in);
      CalControllerInterface controller = new CalControllerImpl(null, null, inStream);
      calController.runInteractive();
      fail("Expected an no line exception");
    } catch (Exception e) {
      //pass
    }
  }

  @Test
  public void testInteractiveModeAppStart() {
    String createEvent = "exit";
    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();
    String output = logs.trim();
    assertTrue(
        "Expected 'Exiting...' message at end, but got:" + System.lineSeparator() + output,
        output.contains("Exiting...")
    );
  }

  @Test
  public void testInteractiveModeMenu() {
    String input = "menu"
        + System.lineSeparator()
        + "exit";

    //inStream = new ByteArrayInputStream(input.getBytes());
    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String viewLogs = ((MockView) mockView).getLogs();
    assertTrue(viewLogs.contains("For Menu type: Menu"));
  }

  @Test
  public void testInteractiveModeNullCalendar() {
    String input = "use calendar --name Birthday"
        + System.lineSeparator() + "exit";
    //inStream = new ByteArrayInputStream(input.getBytes());
    inStream = new StringReader(input);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();
    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected instruction for use-calendar command!",
        logs.contains("To start the program, select a calendar "
            + "or create one if not exist."));
  }

  @Test
  public void testInteractiveModeUseCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "exit";
    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected invalid syntax message to be displayed",
        logs.contains("To start the program, select a calendar "
            + "or create one if not exist.")
    );
  }

  @Test
  public void testInteractiveModeActiveCalendarScannerNextEmpty() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday";
    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("Now using calendar : Birthday"));
  }

  @Test
  public void testInteractiveModeHelpCommandWithoutActiveCalendar() {
    String createEvent = String.join(System.lineSeparator(),
        "help\\create",
        "help\\edit",
        "exit"
    );

    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected instruction for use-calendar command!",
        logs.contains("To start the program, select a calendar "
            + "or create one if not exist."));
  }


  @Test
  public void testInteractiveModeHelpCommandForStartApp() {
    String createEvent = String.join(System.lineSeparator(),
        "help\\use",
        "help\\createCal",
        "exit"
    );

    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected instruction for use-calendar command!",
        logs.contains("use calendar --name <name-of-calendar>"
            + System.lineSeparator()
            + System.lineSeparator()
            + "create calendar --name <calName> --timezone area/location"));
  }

  @Test
  public void testInteractiveModeHelpCommandWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "help\\create"
        + System.lineSeparator() + "exit";

    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected instruction for help command!",
        logs.contains(
            "- create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>"
                + System.lineSeparator()
                + "- create event <eventSubject> from <dateStringTtimeString> "
                + "to <dateStringTtimeString> repeats <weekdays> for <N> times"
                + System.lineSeparator()
                + "- create event <eventSubject> from <dateStringTtimeString> "
                + "to <dateStringTtimeString> repeats <weekdays> until <dateString>"
                + System.lineSeparator() + "- create event <eventSubject> on <dateString>"
                + System.lineSeparator() + "- create event <eventSubject> on <dateString> "
                + "repeats <weekdays> for <N> times" + System.lineSeparator()
                + "- create event <eventSubject> on <dateString> "
                + "repeats <weekdays> until <dateString>"));
  }

  @Test
  public void testInteractiveModeHelpNonCreateCommandWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "help\\edit"
        + System.lineSeparator() + "help\\print"
        + System.lineSeparator() + "help\\misc"
        + System.lineSeparator() + "exit";

    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue(logs.contains("- edit event <property> <eventSubject> from <dateStringTtimeString> "
        + "to <dateStringTtimeString> with <NewPropertyValue>" + System.lineSeparator()
        + "- edit events <property> <eventSubject> from "
        + "<dateStringTtimeString> with <NewPropertyValue>"
        + System.lineSeparator()
        + "- edit series <property> <eventSubject> from "
        + "<dateStringTtimeString> with <NewPropertyValue>"
        + System.lineSeparator()
        + System.lineSeparator()
        + "- print events on <dateString>" + System.lineSeparator()
        + "- print events from <dateStringTtimeString> to <dateStringTtimeString>"
        + System.lineSeparator()
        + System.lineSeparator()
        + "- export cal fileName.csv" + System.lineSeparator()
        + "- show status on <dateStringTtimeString>"));
  }


  @Test
  public void testInteractiveModeInvalidHelpCommand() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "help/create"
        + System.lineSeparator() + "exit";

    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue("Expected error msg", logs.contains("Invalid command: help/create"));
  }


  @Test
  public void testInteractiveModeMenuCommandWithActiveCalendar() {
    String createEvent = "create calendar --name Birthday --timezone Australia/Sydney"
        + System.lineSeparator() + "use calendar --name Birthday"
        + System.lineSeparator() + "menu"
        + System.lineSeparator() + "exit";
    //inStream = new ByteArrayInputStream(createEvent.getBytes());
    inStream = new StringReader(createEvent);
    outStream = new ByteArrayOutputStream();
    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runInteractive();

    String logs = ((MockView) mockView).getLogs();

    assertTrue(
        "Expected menu instructions",
        logs.contains("Welcome to I-Cal" + System.lineSeparator()
            + "For Exiting the calendar type : Exit" + System.lineSeparator()
            + "For Help type :" + System.lineSeparator())
    );
  }
}
