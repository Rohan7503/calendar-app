package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewInterface;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for verifying the behavior of the {@link CalControllerImpl}
 * when operating in headless mode (non-interactive mode that reads commands
 * from an input stream instead of user input).
 */
public class ControllerHeadLessModeTest extends CalControllerImplTest {

  private final MultiCalModelInterface mockModel = new MockModel(new StringBuilder());

  private final CalViewInterface mockView = new MockView(new StringBuilder());

  private CalControllerInterface calController;
  private File inputFile;
  private File outputFile;
  private OutputStream outStream;
  //private InputStream inStream;
  private Readable inStream = new InputStreamReader(System.in);

  @Test
  public void testHeadlessMode() {
    try {
      inputFile = new File("src/test/java/calendar/controller/ValidInputs.txt");
      outputFile = new File("src/test/java/calendar/controller/output.txt");


      //inStream = new FileInputStream(inputFile);
      String fileContents = new String(Files.readAllBytes(Paths.get(inputFile.toURI())));
      inStream = new StringReader(fileContents);
      calController = new CalControllerImpl(mockModel, mockView, inStream);
      calController.runHeadless();

      String logs = ((MockModel) mockModel).getLogs();
      FileWriter fileWriter = new FileWriter(outputFile, true);
      fileWriter.write(System.lineSeparator() + "--- CONTROLLER ARGS ---" + System.lineSeparator());
      fileWriter.write(logs + System.lineSeparator());
      fileWriter.close();

      String actualLogs = Files.readString(outputFile.toPath());
      List<String> expectedLogs = List.of(
          "subject=Meeting, start=2025-10-02T10:00, end=2025-10-02T11:00"
              + System.lineSeparator()
              + "subject=\"Daily Sprint\", start=2025-10-28T10:00, end=2025-10-28T11:00, "
              + "weekdays=WRF , count=20" + System.lineSeparator()
              + "subject=\"Daily Sprint\", start=2025-10-28T10:00, end=2025-10-28T11:00, "
              + "weekdays=WRF , endDate=2026-03-28 " + System.lineSeparator()
              + "subject=\"Daily Sprint\", start=2025-11-04T08:00, end=2025-11-04T17:00"
              + System.lineSeparator()
              + "subject=\"Daily Sprint\", start=2025-11-04T08:00, end=2025-11-04T17:00, "
              + "weekdays=RF , count=10" + System.lineSeparator()
              + "subject=\"Daily Sprint\", start=2025-11-04T08:00, "
              + "end=2025-11-04T17:00, weekdays=RF , "
              + "endDate=2026-01-28 " + System.lineSeparator()
              + "property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "end=2025-10-28T11:00 , newValue=2025-10-28T09:00 " + System.lineSeparator()
              + "property=end , subject=\"Daily Sprint\" , start=2025-10-02T10:00 , "
              + "end=2025-10-28T11:00 , "
              + "newValue=2025-10-28T12:00 " + System.lineSeparator()
              + "property=subject , subject=\"Daily Sprint\" , start=2025-10-02T10:00 , "
              + "end=2025-10-28T11:00 , "
              + "newValue=\"Master Sprint\" " + System.lineSeparator()
              + "property=status , subject=\"Master Sprint\" , start=2025-10-02T10:00 , "
              + "end=2025-10-28T11:00 , newValue=\"private\" " + System.lineSeparator()
              + "property=status , subject=\"Daily Sprint\" , start=2025-10-02T10:00 , "
              + "end=2025-10-28T11:00 , newValue=\"public\" " + System.lineSeparator()
              + "property=location , subject=\"Daily Sprint\" , start=2025-10-02T10:00 , "
              + "end=2025-10-28T11:00 , newValue=\"Hyderabad\" " + System.lineSeparator()
              + "property=description , subject=\"Daily Sprint\" , start=2025-10-02T10:00 , "
              + "end=2025-10-28T11:00 , newValue=\"Daily meet for project\" "
              + System.lineSeparator()
              + "property=location , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"Bengal\" , editWholeSeries=false " + System.lineSeparator()
              + "property=subject , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"Daily Master Sprint\" , editWholeSeries=false " + System.lineSeparator()
              + "property=status , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"public\" , editWholeSeries=false " + System.lineSeparator()
              + "property=status , subject=\"Master Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"private\" , editWholeSeries=false " + System.lineSeparator()
              + "property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=2025-10-28T09:00 , editWholeSeries=false " + System.lineSeparator()
              + "property=end , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=2025-10-28T13:00 , editWholeSeries=false " + System.lineSeparator()
              + "property=description , subject=\"Master Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"Cumulative project meet\" , editWholeSeries=false "
              + System.lineSeparator()
              + "property=end , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=2025-10-28T15:00 , editWholeSeries=true " + System.lineSeparator()
              + "property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=2025-10-28T08:00 , editWholeSeries=true " + System.lineSeparator()
              + "property=status , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"public\" , editWholeSeries=true " + System.lineSeparator()
              + "property=status , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"private\" , editWholeSeries=true " + System.lineSeparator()
              + "property=subject , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"Casual Meet\" , editWholeSeries=true " + System.lineSeparator()
              + "property=location , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"Mumbai\" , editWholeSeries=true " + System.lineSeparator()
              + "property=description , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
              + "newValue=\"General meet for project discussion\" , editWholeSeries=true "
              + System.lineSeparator()
              + "start=2025-11-04T00:00 , end=2025-11-05T00:00 " + System.lineSeparator()
              + "start=2025-10-28T10:00 , end=2025-10-28T11:00 " + System.lineSeparator()
              + "status on date: 2025-10-28T10:00 "
      );

      for (String expected : expectedLogs) {
        assertTrue("Expected output file to contain: " + expected,
            actualLogs.contains(expected));
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }


  @Test
  public void testHeadlessModeExceptions() {
    try {
      inputFile = new File("src/test/java/calendar/controller/InvalidInputs.txt");
      outputFile = new File("src/test/java/calendar/controller/output.txt");
      //inStream = new FileInputStream(inputFile);
      String fileContents = new String(Files.readAllBytes(Paths.get(inputFile.toURI())));
      inStream = new StringReader(fileContents);
      outStream = new FileOutputStream(outputFile);
      calController = new CalControllerImpl(mockModel, mockView, inStream);
      calController.runHeadless();

      String logs = ((MockModel) mockModel).getLogs();
      try {
        FileWriter fileWriter = new FileWriter(outputFile, true);
        fileWriter.write(
            System.lineSeparator() + "--- CONTROLLER ARGS ---" + System.lineSeparator());
        fileWriter.write(logs + System.lineSeparator());
        fileWriter.close();

        String actualLogs = Files.readString(outputFile.toPath());
        List<String> expectedLogs = List.of(
            "subject=Meeting, start=2025-10-26T10:00, end=2025-10-26T11:00" + System.lineSeparator()
                + "subject=\"Daily Sprint\", start=2025-10-28T10:00, "
                + "end=2025-10-28T11:00, weekdays=WRF , count=20" + System.lineSeparator()
                + "subject=\"Daily Sprint\", start=2025-10-28T10:00, "
                + "end=2025-10-28T11:00, weekdays=WRF , endDate=2026-03-28 "
                + System.lineSeparator()
                + "subject=\"Daily Sprint\", start=2025-11-04T08:00, end=2025-11-04T17:00"
                + System.lineSeparator()
                + "subject=\"Daily Sprint\", start=2025-11-04T08:00, "
                + "end=2025-11-04T17:00, weekdays=RF , count=10" + System.lineSeparator()
                + "subject=\"Daily Sprint\", start=2025-11-04T08:00, end=2025-11-04T17:00, "
                + "weekdays=RF , endDate=2026-01-28 " + System.lineSeparator()
                + "property=start , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
                + "end=2025-10-28T11:00 , newValue=2025-10-28T09:00 " + System.lineSeparator()
                + "property=location , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
                + "newValue=\"Bengal\" , editWholeSeries=false " + System.lineSeparator()
                + "property=end , subject=\"Daily Sprint\" , start=2025-10-28T10:00 , "
                + "newValue=2025-10-28T15:00 , editWholeSeries=true " + System.lineSeparator()
                + "start=2025-11-04T08:00 , end=2025-11-04T17:00 " + System.lineSeparator()
                + "start=2025-10-28T10:00 , end=2025-10-28T11:00 " + System.lineSeparator()
                + "status on date: 2025-10-28T10:00 "
        );

        for (String expected : expectedLogs) {
          assertFalse("Expected output file to contain: " + expected,
              actualLogs.contains(expected));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Test
  public void testHeadlessModeViewExitExportCommandDisplay() {
    try {
      inputFile = new File("src/test/java/calendar/controller/ValidInputs.txt");
      //inStream = new FileInputStream(inputFile);
      String fileContents = new String(Files.readAllBytes(Paths.get(inputFile.toURI())));
      inStream = new StringReader(fileContents);
      calController = new CalControllerImpl(mockModel, mockView, inStream);
      calController.runHeadless();

      String logs = ((MockView) mockView).getLogs();
      String output = logs.trim();
      assertTrue("Expected 'Exiting...' message to be displayed, but got: " + output,
          output.contains("Exiting...")
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Test
  public void testHeadlessModeViewErrorCommandDisplay() {
    try {
      inputFile = new File("src/test/java/calendar/controller/InvalidInputs.txt");
      //inStream = new FileInputStream(inputFile);
      String fileContents = new String(Files.readAllBytes(Paths.get(inputFile.toURI())));
      inStream = new StringReader(fileContents);
      calController = new CalControllerImpl(mockModel, mockView, inStream);
      calController.runHeadless();

      String logs = ((MockView) mockView).getLogs();
      String output = logs.trim();
      assertTrue(
          "Expected error message to be displayed",
          output.contains("Unknown command keyword")
      );

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testHeadlessModeNoExitCommand() throws IOException {
    String userInput = String.join(System.lineSeparator(),
        "create event Meeting from 2025-10-02T10:00 to 2025-10-02T11:00",
        "",
        "create event Lunch from 2025-10-03T12:00 to 2025-10-03T13:00",
        ""
    );

    //inStream = new ByteArrayInputStream(userInput.getBytes());
    inStream = new StringReader(userInput);
    outStream = new ByteArrayOutputStream();

    calController = new CalControllerImpl(mockModel, mockView, inStream);
    calController.runHeadless();

    String logs = ((MockView) mockView).getLogs().trim();

    assertTrue(
        "Expected error msg, but got: " + logs,
        logs.contains("Missing exit")
    );
  }
}
