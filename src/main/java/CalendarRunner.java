import calendar.controller.CalControllerImpl;
import calendar.controller.CalControllerInterface;
import calendar.model.MultiCalModelImpl;
import calendar.model.MultiCalModelInterface;
import calendar.view.CalViewImpl;
import calendar.view.CalViewInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * Main entry point for the Calendar application.
 * Supports --mode interactive and --mode headless.
 */
public class CalendarRunner {

  /**
   * Main function to start the application.
   *
   * @param args Command line arguments. --mode interactive or --mode headless &lt;file&gt;
   */
  public static void main(String[] args) {
    MultiCalModelInterface model = new MultiCalModelImpl();
    CalViewInterface view = new CalViewImpl(System.out, System.err);
    InputStream in = System.in;
    Readable rd = new InputStreamReader(System.in);
    CalControllerInterface controller = new CalControllerImpl(model, view, rd);

    if (args.length == 0) {
      System.out.println("Starting the GUI...");
      controller.runGui();
      return;
    }
    String modeArg = args[0].toLowerCase();
    if (!modeArg.equals("--mode")) {
      System.out.println("Usage: --mode interactive or --mode headless <file>");
      System.out.println("Exiting the program...");
      return;
    }
    if (args.length < 2) {
      System.out.println("Usage: --mode interactive or --mode headless <file>");
      System.out.println("Exiting the program...");
      return;
    }
    String mode = args[1].toLowerCase();
    switch (mode) {
      case "headless":
        if (args.length < 3) {
          System.out.println("Headless mode requires a commands file.");
          return;
        }
        File inputFile = Paths.get(System.getProperty("user.dir"), args[2]).normalize().toFile();
        try {
          in = new FileInputStream(inputFile);
          rd = new InputStreamReader(in);
          System.out.println("Starting in headless mode...");
          controller = new CalControllerImpl(model, view, rd);
          controller.runHeadless();
        } catch (FileNotFoundException e) {
          System.out.println("File not found: " + inputFile.getAbsolutePath());
        }
        break;
      case "interactive":
        System.out.println("Starting in interactive mode...");
        controller.runInteractive();
        break;
      default:
        System.out.println("Unknown mode: " + args[1]);
        System.out.println("Exiting the program...");
    }
  }
}
