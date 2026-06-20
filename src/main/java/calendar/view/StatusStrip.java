package calendar.view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A thin strip along the bottom of the window used for non-intrusive feedback: routine success
 * messages and one-shot results (such as a busy/available check) appear here and fade after a few
 * seconds, instead of interrupting the user with a modal dialog.
 */
class StatusStrip extends JPanel {

  private static final int CLEAR_DELAY_MS = 5000;

  private final JLabel label;
  private final transient Timer clearTimer;

  /**
   * Constructs an empty status strip.
   */
  StatusStrip() {
    setLayout(new BorderLayout());
    setBackground(Theme.SURFACE);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
        BorderFactory.createEmptyBorder(Theme.UNIT / 2, Theme.UNIT, Theme.UNIT / 2, Theme.UNIT)));

    label = new JLabel(" ");
    label.setFont(Theme.BODY);
    add(label, BorderLayout.WEST);

    clearTimer = new Timer(CLEAR_DELAY_MS, e -> label.setText(" "));
    clearTimer.setRepeats(false);
  }

  /**
   * Shows a transient informational message that clears itself after a short delay.
   *
   * @param message the message to display
   */
  void showMessage(String message) {
    label.setForeground(Theme.TEXT);
    label.setText(normalize(message));
    clearTimer.restart();
  }

  private String normalize(String message) {
    String collapsed = message.replace(System.lineSeparator(), " ").trim();
    return collapsed.isEmpty() ? " " : collapsed;
  }
}
