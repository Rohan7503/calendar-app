package calendar.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the non-window view helpers ({@link FormComponents} and {@link EventChip}). These
 * exercise formatting and truncation logic. They are skipped in a headless environment because
 * they instantiate (non-window) Swing components.
 */
public class ViewHelpersTest {

  /**
   * Skips these tests when no display is available, since they construct Swing components.
   */
  @Before
  public void requireDisplay() {
    org.junit.Assume.assumeFalse("Headless environment", GraphicsEnvironment.isHeadless());
  }

  @Test
  public void testDatePickerRoundTripsDate() {
    JSpinner picker = FormComponents.datePicker(LocalDate.of(2025, 1, 15));
    assertEquals("2025-01-15", FormComponents.dateValue(picker));
  }

  @Test
  public void testTimePickerRoundTripsTime() {
    JSpinner picker = FormComponents.timePicker(LocalTime.of(9, 5));
    assertEquals("09:05", FormComponents.timeValue(picker));
  }

  @Test
  public void testEventChipForTimedEventShowsStartTime() {
    Event event = Event.getBuilder()
        .subject("Gym")
        .start(LocalDateTime.parse("2025-01-15T09:00"))
        .end(LocalDateTime.parse("2025-01-15T10:00"))
        .build();
    JLabel chip = EventChip.create(event);
    assertEquals("09:00 Gym", chip.getText());
  }

  @Test
  public void testEventChipForAllDayEventOmitsTime() {
    Event event = Event.getBuilder()
        .subject("Holiday")
        .start(LocalDateTime.parse("2025-01-15T08:00"))
        .end(LocalDateTime.parse("2025-01-15T17:00"))
        .allDay(true)
        .build();
    JLabel chip = EventChip.create(event);
    assertEquals("Holiday", chip.getText());
    assertTrue(chip.getToolTipText().contains("all day"));
  }

  @Test
  public void testEventChipTruncatesLongSubjects() {
    String subject = "A very long event subject that overflows";
    Event event = Event.getBuilder()
        .subject(subject)
        .start(LocalDateTime.parse("2025-01-15T08:00"))
        .end(LocalDateTime.parse("2025-01-15T17:00"))
        .allDay(true)
        .build();
    JLabel chip = EventChip.create(event);
    assertTrue(chip.getText().endsWith("..."));
    assertTrue(chip.getText().length() <= 16);
    assertTrue(chip.getText().length() < subject.length());
  }
}
