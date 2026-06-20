# Calendar App — UI/UX Redesign Plan

Status: proposal for review. This document plans a UI/UX overhaul of the desktop calendar
app. No UI rewrite has been started; implementation begins only after this plan is approved.

It is grounded in the current Swing implementation in
[CalGuiImpl.java](../src/main/java/calendar/view/CalGuiImpl.java),
[CalGuiInterface.java](../src/main/java/calendar/view/CalGuiInterface.java),
[GuiControllerImpl.java](../src/main/java/calendar/controller/GuiControllerImpl.java), and the
[Features.java](../src/main/java/calendar/controller/Features.java) callback interface.

---

## 1. Current UI — how it works today

The GUI is a single `JFrame` (`CalGuiImpl`) laid out with `BorderLayout` and four regions:

- **NORTH** — a `FlowLayout` row of action buttons: Create Calendar, Create Event,
  Create Series (Count), Create Series (Until Date), Edit Multiple Events, Delete Events.
- **WEST** — "Calendars" pane: one `JButton` per calendar; the active one is painted orange.
- **CENTER** — a month grid (`GridLayout(0,7)`) of day buttons with `< Month Year >` navigation;
  weekends tinted, the selected day highlighted.
- **EAST** — "Day Events" pane: a vertical list of the selected day's events, each row showing
  `subject | start to end` (or `subject | All day`) with inline **Edit** and **Delete** buttons.
- A **Tools** menu (added in the parity pass) exposing Edit Calendar, Export CSV/ICS,
  Copy Events, Show Status, and View Date Range.

All data entry happens through `JOptionPane` dialogs assembled on the fly from bare `JTextField`s
(dates/times typed as `YYYY-MM-DD` / `HH:MM` strings), `JCheckBox`es for weekdays, and a property
`JComboBox`. Every result — success or failure — is reported through a **modal** `JOptionPane`.

User flows today:
- **Create calendar:** button → two chained input dialogs (name, then timezone) → orange highlight moves.
- **Create event / series:** button → one dialog with typed date/time fields (and weekday checkboxes
  for series) → modal success/error.
- **View a day:** click a day button → EAST pane re-renders that day's events.
- **Edit / delete:** per-event buttons in the EAST pane, or the "Edit/Delete Multiple Events"
  dialogs with a whole-series / from-here radio.
- **Switch calendars:** click a calendar button in the WEST pane.

---

## 2. Current UI problems

1. **No visual hierarchy or modern styling.** Default metal/system L&F, ad-hoc spacing, inconsistent
   fonts. It reads as a class project, not a product.
2. **Modal dialogs for everything**, including routine success messages — every create/edit/delete
   interrupts the user with an OK box. There is no quiet, inline confirmation.
3. **String-typed dates and times.** Users must hand-type `YYYY-MM-DD` and `HH:MM`. Mistakes are
   common and only surface as a modal error after submit. No pickers, no inline validation.
4. **The month grid carries no information.** Days show only a number; you cannot tell which days
   have events without clicking each one. No event previews, counts, or color.
5. **Only a month view.** No week or day view, despite the model supporting arbitrary ranges.
6. **Discoverability is uneven.** Core actions are buttons; the parity features hide in a menu.
   The two groupings have different visual weight and no obvious relationship.
7. **One growing God-class.** `CalGuiImpl` is ~750 lines mixing layout, dialogs, styling, event
   wiring, and formatting, with 0 automated test coverage. Each feature makes it harder to change.
8. **Calendar list is just buttons.** No rename/delete affordance in place, no color per calendar,
   no indication of timezone.
9. **Weak feedback for long lists / empty states.** The EAST pane and range view dump text with no
   empty-state design or scmasing for many events.

---

## 3. Target user experience

A clean, modern, single-window desktop calendar that feels closer to Google/Apple Calendar at a
small scale:

- A persistent **left sidebar** for calendars (color dot, name, active state, per-calendar actions).
- A **main calendar surface** that defaults to a polished month view with **visible event chips**,
  switchable to **week** and **day** views.
- A **right detail panel** that shows the selected day/event with rich, glanceable cards.
- **Form-based create/edit** with real date/time pickers and inline validation, shown in a docked
  panel or a clean modal — never a stack of chained input boxes.
- **Non-intrusive feedback**: a transient status/"toast" strip for successes; inline field errors
  for validation; modal confirms reserved for destructive actions only.
- First-class access to **export, copy, status, and range queries** from a consistent toolbar.

---

## 4. Recommended direction: improve Swing (do not migrate yet)

**Recommendation: stay on Swing and do a structured Swing redesign.** Reasons, weighed against a
JavaFX migration:

| Factor | Improve Swing | Migrate to JavaFX |
|---|---|---|
| **Effort** | Moderate — reuse model/controller/`Features` untouched; rebuild the view in stages. | High — rewrite the entire view, re-wire callbacks, learn FXML/CSS, restructure the build. |
| **Dependencies / build** | None. Stays JDK-only, honoring the project's hard constraint. | JavaFX is no longer bundled with the JDK: adds `org.openjfx` artifacts, the `javafx` Gradle plugin, and module-path config. Directly violates the "JDK-only at runtime" rule. |
| **MVC compatibility** | Excellent — `CalGuiInterface` + `Features` already isolate the view; only the view package changes. | The same boundary helps, but every `Features`/view interaction is re-implemented against new controls. |
| **Testing impact** | Low — controller/model tests unchanged; can add view tests via extracted seams + `AssertJ-Swing`-style headless checks (optional dep) or plain logic extraction. | Higher — needs TestFX and a headless toolkit; existing patterns don't transfer. |
| **UI quality ceiling** | Good with effort — custom painting, a shared theme/spacing system, and component factories get us "clean and modern" but not CSS-grade. | Higher ceiling — CSS theming, animation, and richer controls out of the box. |
| **Long-term maintainability** | Good if we break up the God-class into panels/components and add a theme layer. | Good, but the migration cost and new dependency surface are real ongoing burdens. |

**Conclusion.** The decisive factor is the project's explicit JDK-only-at-runtime constraint
(`README`/working rules): JavaFX requires adding dependencies and module configuration, which is
out of scope for this app. Swing can reach a genuinely polished result for an app of this size, and
the clean MVC boundary means a Swing redesign is contained to the view package. **If** a future
product bar demands CSS-grade theming/animation, revisit JavaFX (or adopt a Look-and-Feel library
such as FlatLaf) as an explicit, separately-approved decision with its dependency tradeoff
documented. Until then, improve Swing.

What "polished Swing" concretely means here:
- A small **theme module** (centralized colors, fonts, paddings, corner radius) applied consistently.
- **Component factories** (primary/secondary buttons, card panels, form rows, date/time pickers) so
  every screen looks the same and `CalGuiImpl` stops hand-styling each widget.
- **Custom-painted** day cells and event chips for the calendar surface.
- Optional: ship a tasteful system Look-and-Feel (`Nimbus`, already in the JDK) as the base.

---

## 5. Proposed app layout

```
┌───────────────────────────────────────────────────────────────────────────┐
│  Toolbar:  [＋ Event] [＋ Series] [Today] [‹ ›]  Month▾   ⌕   [Export][Copy] │
├───────────┬───────────────────────────────────────────────┬─────────────────┤
│ SIDEBAR   │                MAIN CALENDAR SURFACE           │  DETAIL PANEL   │
│           │                                                │                 │
│ Calendars │   Month / Week / Day view with event chips     │  Selected day   │
│  ● Work   │                                                │  or event card  │
│  ● Home   │   ┌────┬────┬────┬────┬────┬────┬────┐          │                 │
│  ＋ New    │   │ ...│ ...│[evt]│ ...│ ...│ ...│ ...│          │  • event cards  │
│           │   └────┴────┴────┴────┴────┴────┴────┘          │  • quick edit   │
│  [⚙ gear] │                                                │  • status line  │
├───────────┴───────────────────────────────────────────────┴─────────────────┤
│  Status strip (transient): "Event created"          [busy/available result]  │
└───────────────────────────────────────────────────────────────────────────┘
```

- **Top toolbar:** primary actions (create event/series), today/navigation, a **view switcher**
  (Month/Week/Day), search, and the tools (export/copy). Replaces today's mixed button row + menu.
- **Left sidebar:** calendar list with a color dot per calendar, active selection, and a gear/⋯ for
  rename, edit timezone, and (future) delete. A "＋ New calendar" affordance at the bottom.
- **Center surface:** the active view.
- **Right detail panel:** context for the selected day or event.
- **Bottom status strip:** transient success messages and one-off results (e.g. status check).

---

## 6. Main screens / views needed

1. **Month view** (default) — 6×7 grid; each cell shows the date and up to N event chips with a
   "+k more" overflow; today and selected day visually distinct.
2. **Week view** — 7 day columns with an hour grid; timed events drawn as blocks, all-day events in
   a top band.
3. **Day view** — single-day hour grid; densest detail.
4. **Event detail / editor** — docked right-panel form (or modal) for create and edit, shared by
   single events and series.
5. **Calendar settings** — sidebar gear → form for name and timezone (and future: color, delete).
6. **Tools surfaces** — export, copy, status, and range-view as consistent forms (status and range
   results render in the detail panel, not a modal).

The view switcher maps cleanly onto the existing model: all three views are just
`getEventsInRange(start, end)` calls with different windows.

---

## 7. Calendar sidebar behavior

- Each calendar is a row: **color dot + name** (+ small timezone subtitle).
- The active calendar is highlighted (replacing today's orange button) and drives the main surface.
- Selecting a calendar calls the existing `Features.selectCalendar`.
- A per-row ⋯/gear opens **Edit Calendar** (`Features.editCalendar` — already implemented) for
  rename/timezone; a future **delete calendar** action would need a new model method.
- "＋ New calendar" uses the existing create flow but with a proper form (name + timezone picker)
  instead of two chained input dialogs.
- Color is a **view-only** concern (a name→color map in the view); it does not touch the model and
  keeps MVC intact.

---

## 8. Month / Week / Day view plan

- **Month:** custom-painted cells; render event chips from the day's `getEventsInRange`. All-day
  events get a full-width chip; timed events a labeled chip with start time. Click a day → detail
  panel; double-click → day view. Keep `< >` and add **Today**.
- **Week:** columns Mon–Sun (or locale start), an hour gutter, timed events positioned by start/end,
  an all-day band on top. Navigation by week.
- **Day:** one column, larger hit targets, ideal for dense days.
- Shared rendering: one `EventChip` component and one `getEventsInRange`-backed data load feed all
  three, so behavior stays consistent.

---

## 9. Event creation and editing flow

- Replace ad-hoc `JOptionPane` field stacks with a **single shared event form** used for create and
  edit, with:
  - subject field with inline "required" validation,
  - a **date picker** and **time spinner** (custom Swing components; no new deps),
  - an **All-day** toggle that hides the time fields (the model already supports `allDay`),
  - for series: weekday toggles and a count/until selector,
  - for series edits/deletes: the existing **whole series / from here** scope control.
- Submitting calls the existing `Features` methods (`createEvent`, `createAllDayEvent`,
  `createEventSeriesByCount/UntilDate`, `editEvent`, `editEvents`, `deleteEvent`, `deleteEvents`).
- Errors render **inline** on the offending field; success shows a transient status message and
  refreshes the surface — no modal OK.

---

## 10. Event cards and indicators

- **Event chip** (in grids): color bar, subject, start time (or "All day"), truncation with tooltip.
- **Event card** (detail panel): subject, full time range or "All day", location/status/description
  if present, series indicator, and **Edit/Delete** actions.
- **Day indicators** in month view: chips plus a subtle count badge / dot when events exist, so a
  populated day is obvious at a glance (today's biggest gap).

---

## 11. Calendar settings flow

- Entry from the sidebar gear/⋯ or a Tools entry.
- Form fields: **name** (text), **timezone** (searchable combo of IANA zones), and future **color**.
- Calls `Features.editCalendar`. On rename, the active selection follows the renamed calendar; the
  current implementation re-highlights the edited calendar — the redesign should track the active
  calendar name explicitly (small `MultiCalModelInterface` addition or view-side tracking) so the
  highlight is always correct.

---

## 12. Export / copy / status / range integration

- **Export:** a toolbar "Export" with CSV/ICS choice and a real file chooser (`JFileChooser`)
  instead of a name-only input; still routes through the shared `Export` command via the existing
  `Features.exportCalendar` adapter.
- **Copy events:** a form with date pickers and a target-calendar combo (`Features.copyEvents`);
  consider also exposing single-event copy (needs a `Features.copyEvent` + model call).
- **Status:** result shows in the detail panel / status strip, not a modal (`Features.showStatus`).
- **Range view:** results render as event cards in the detail panel (`Features.requestEventsInRange`),
  reusing the same card component as the day view.

---

## 13. Validation and error-message strategy

- **Validate before submit**: pickers constrain dates/times so most format errors become impossible.
- **Inline field errors** (red helper text) for the rest; keep the submit button disabled until the
  form is valid where practical.
- **Modal confirmation only for destructive actions** (delete event/series, future delete calendar).
- **Transient status strip** for successes and one-shot results.
- Keep model/controller error messages as the source of truth; the view maps them to the right
  field or the status strip rather than always popping a dialog.

---

## 14. Visual design principles

- **Spacing:** an 8px base unit; consistent paddings/margins via a shared constants class.
- **Typography:** one sans-serif family, a small type scale (e.g. 12/14/16/20), bold only for
  headers and the active day.
- **Hierarchy:** clear primary vs secondary buttons; the active calendar and today's date are the
  strongest accents.
- **Color:** a restrained neutral base with one accent; per-calendar colors for chips; reserve red
  strictly for destructive/error states.
- **Consistency:** every button/field/card comes from a component factory so screens match.

---

## 15. Accessibility considerations (where practical)

- Full **keyboard navigation**: tab order, arrow-key movement across the month grid, Enter to open,
  Esc to cancel.
- **Mnemonics/accelerators** for primary actions; set `setDisplayedMnemonicIndex`/accelerator keys.
- **Contrast**: meet WCAG AA for text and the selected/today states (don't rely on color alone — pair
  color with shape/border, as the current weekend tint already hints).
- **Screen-reader labels**: set `getAccessibleContext().setAccessibleName/Description` on buttons,
  day cells, and chips.
- **Scalable text**: avoid hard-coded pixel fonts where possible so OS text scaling is respected.

---

## 16. Implementation phases (proposed, post-approval)

- **R0 — Decompose the view (no visual change).** Split `CalGuiImpl` into `SidebarPanel`,
  `CalendarSurfacePanel`, `DetailPanel`, `Toolbar`, and dialog/form classes behind `CalGuiInterface`.
  Add a `Theme`/`UiConstants` module and component factories. This alone makes the God-class
  testable and sets up everything else.
- **R1 — Month view polish + indicators.** Custom day cells, event chips, today/selected styling,
  Today button. Biggest perceived win.
- **R2 — Shared event form with pickers + inline validation.** Replace the `JOptionPane` field stacks
  for create/edit/delete; add date pickers, time spinners, all-day toggle.
- **R3 — Detail panel + non-modal feedback.** Event/day cards, status strip, route status/range
  results to the panel.
- **R4 — Week and Day views** behind the view switcher.
- **R5 — Sidebar upgrade**: colors, per-calendar actions, file-chooser export, refined settings.
- **R6 — Accessibility + keyboard pass** and final theming cleanup.

Each phase keeps the app buildable and shippable, and the model/controller stay untouched.

---

## 17. Risks and tradeoffs

- **Swing ceiling.** A polished Swing UI still won't match CSS-grade JavaFX/web polish; accepted in
  exchange for zero new dependencies and a contained, lower-risk change.
- **Custom painting cost.** Calendar cells, chips, pickers, and the time grid are non-trivial to
  build and test in Swing; mitigated by component factories and incremental phases.
- **God-class momentum.** If R0 (decomposition) is skipped, every later phase makes `CalGuiImpl`
  worse. R0 must come first.
- **GUI testability.** Swing views remain hard to unit-test headlessly. Mitigation: push logic
  (formatting, validation, view-model state) into plain classes that are unit-tested, keep the
  `JFrame`/panels thin, and consider an optional test-only UI-testing dependency if justified.
- **Active-calendar tracking.** Correct highlight on rename needs explicit active-name tracking; a
  small, low-risk addition to make now.
- **Scope creep.** Week/Day views and pickers are sizable; they are deliberately late phases (R4+)
  so the high-value month polish and forms land first.

---

## 18. Open questions

- Is the **JDK-only-at-runtime** constraint firm? If a Look-and-Feel library (FlatLaf) or JavaFX
  were permitted, the quality ceiling rises materially — but that is an explicit dependency decision.
- Should the redesign add **delete-calendar** and **single-event copy** to the model now, or stay
  view-only? Both need small model additions.
- Preferred **week start** (Sunday vs Monday) and locale handling?
- Any **branding** (accent color, app name/icon) to anchor the visual language?
</content>
