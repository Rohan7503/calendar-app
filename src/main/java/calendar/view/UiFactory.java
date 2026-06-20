package calendar.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Small factory for consistently styled Swing components. Centralizing widget creation keeps the
 * panels and dialogs visually uniform and free of repeated styling code.
 */
final class UiFactory {

  private UiFactory() {
  }

  /**
   * Creates a primary (accent-colored) action button.
   *
   * @param text the button label
   * @return a styled button
   */
  static JButton primaryButton(String text) {
    JButton button = new JButton(text);
    button.setFocusPainted(false);
    button.setBackground(Theme.ACCENT);
    button.setForeground(Color.WHITE);
    button.setFont(Theme.BODY_BOLD);
    button.setOpaque(true);
    button.setBorder(BorderFactory.createEmptyBorder(Theme.UNIT / 2, Theme.UNIT * 2,
        Theme.UNIT / 2, Theme.UNIT * 2));
    return button;
  }

  /**
   * Creates a secondary (neutral) action button.
   *
   * @param text the button label
   * @return a styled button
   */
  static JButton secondaryButton(String text) {
    JButton button = new JButton(text);
    button.setFocusPainted(false);
    button.setBackground(Theme.SURFACE);
    button.setForeground(Theme.TEXT);
    button.setFont(Theme.BODY);
    button.setOpaque(true);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Theme.BORDER),
        BorderFactory.createEmptyBorder(Theme.UNIT / 2, Theme.UNIT + 2,
            Theme.UNIT / 2, Theme.UNIT + 2)));
    return button;
  }

  /**
   * Creates a small, compact button suitable for inline row actions.
   *
   * @param text the button label
   * @return a styled button
   */
  static JButton smallButton(String text) {
    JButton button = new JButton(text);
    button.setFocusPainted(false);
    button.setFont(Theme.SMALL);
    button.setMargin(new java.awt.Insets(1, Theme.UNIT, 1, Theme.UNIT));
    return button;
  }

  /**
   * Creates a section title label.
   *
   * @param text the title text
   * @return a styled label
   */
  static JLabel title(String text) {
    JLabel label = new JLabel(text);
    label.setFont(Theme.HEADER);
    label.setForeground(Theme.TEXT);
    return label;
  }

  /**
   * Creates a "card" panel with a subtle border and padding, used for event rows and list items.
   *
   * @return an empty, styled card panel
   */
  static JPanel card() {
    JPanel panel = new JPanel();
    panel.setBackground(Theme.SURFACE);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Theme.BORDER),
        BorderFactory.createEmptyBorder(Theme.UNIT, Theme.UNIT, Theme.UNIT, Theme.UNIT)));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    return panel;
  }

  /**
   * Creates an empty-padding border of the given size in base units.
   *
   * @param units the padding size as a multiple of the base spacing unit
   * @return a padding border
   */
  static Border padding(int units) {
    int p = Theme.UNIT * units;
    return BorderFactory.createEmptyBorder(p, p, p, p);
  }

  /**
   * Constrains a component's maximum height to its preferred height so vertically stacked
   * components in a {@code BoxLayout} do not stretch.
   *
   * @param component the component to constrain
   */
  static void capHeight(Component component) {
    Dimension pref = component.getPreferredSize();
    component.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
  }
}
