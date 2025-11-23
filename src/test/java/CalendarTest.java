import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class provides tests for the CalendarRunner class.
 */
public class CalendarTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  /**
   * Set the System.out and System.err streams to our custom PrintStreams.
   */
  @Before
  public void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    String input = "Exit" + System.lineSeparator();
    System.setIn(new ByteArrayInputStream(input.getBytes()));
  }

  /**
   * Reset the System.out and System.err streams to defaults.
   */
  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);
  }

  @Test
  public void testMainShowsErrorWhenHeadlessWithoutFile() {
    CalendarRunner.main(new String[] {"--mode", "headless"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    assertEquals("Headless mode requires a commands file.", output);
  }

  @Test
  public void testMainInvalidModeDisplaysErrorMsg() {
    CalendarRunner.main(new String[] {"--mode", "abc"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    assertEquals("Unknown mode: abc", output);
  }

  @Test
  public void testMainNoModeSpecifiedDisplaysErrorMsg() {
    CalendarRunner.main(new String[] {"--mode"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    assertEquals("Usage: --mode interactive or --mode headless <file>", output);
  }

  @Test
  public void testMainInvalidModeArgumentsDisplaysErrorMsg() {
    CalendarRunner.main(new String[] {"abc"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    assertEquals("Usage: --mode interactive or --mode headless <file>", output);
  }

  @Test
  public void testMainHeadlessWithInvalidFile() {
    CalendarRunner.main(new String[] {"--mode", "headless", "abc.txt"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    System.out.println(output);
    assertTrue(output.contains("File not found: "));
  }

  @Test
  public void testMainHeadless() {
    CalendarRunner.main(new String[] {"--mode", "headless", "./src/test/java/testCommands.txt"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    assertEquals("Starting in headless mode...", output);
  }

  @Test
  public void testMainInteractive() {
    CalendarRunner.main(new String[] {"--mode", "interactive"});
    String output = outContent.toString().split(System.lineSeparator())[0];
    assertEquals("Starting in interactive mode...", output);
  }

  @Test
  public void testDummyToImproveCoverage() {
    new CalendarRunner();
  }
}
