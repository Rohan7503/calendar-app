package calendar.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Centralizes the colors, fonts, and spacing used across the GUI so that the look of the
 * application is defined in one place and stays consistent between panels and dialogs.
 */
final class Theme {

  private Theme() {
  }

  /** Base spacing unit, in pixels. Paddings and gaps are multiples of this. */
  static final int UNIT = 8;

  // Palette
  static final Color BACKGROUND = new Color(0xF5, 0xF6, 0xF8);
  static final Color SURFACE = Color.WHITE;
  static final Color BORDER = new Color(0xD0, 0xD4, 0xDA);
  static final Color TEXT = new Color(0x22, 0x26, 0x2B);
  static final Color MUTED_TEXT = new Color(0x8A, 0x90, 0x99);
  static final Color ACCENT = new Color(0x2F, 0x6F, 0xED);
  static final Color ACCENT_SOFT = new Color(0xE4, 0xED, 0xFD);
  static final Color WEEKEND = new Color(0xEE, 0xF2, 0xF8);
  static final Color SELECTED = new Color(0xFF, 0xD7, 0x8C);
  static final Color SELECTED_BORDER = new Color(0xE6, 0x8A, 0x00);
  static final Color TODAY_BORDER = ACCENT;
  static final Color DANGER = new Color(0xC0, 0x39, 0x2B);
  static final Color OUTSIDE_MONTH_TEXT = new Color(0xBB, 0xBF, 0xC6);

  // Fonts
  static final Font TITLE = new Font("SansSerif", Font.BOLD, 18);
  static final Font HEADER = new Font("SansSerif", Font.BOLD, 15);
  static final Font BODY = new Font("SansSerif", Font.PLAIN, 13);
  static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
  static final Font SMALL = new Font("SansSerif", Font.PLAIN, 11);

  // Sizes
  static final Dimension DAY_CELL = new Dimension(96, 78);
  static final Dimension SIDE_PANEL = new Dimension(240, 520);
  static final Dimension DETAIL_PANEL = new Dimension(320, 520);
}
