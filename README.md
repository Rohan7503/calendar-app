# 1 Introduction

This project implements a virtual calendar application inspired by widely used tools such as Google Calendar and Apple iCalendar. The goal is to design and build a flexible, extensible system that supports creating, editing, and viewing calendar events through multiple interaction modes.

The application was developed incrementally, with each iteration expanding the feature set while preserving a scalable and modular design. The initial implementation focuses on core calendar functionality and user interaction through a command-line interface. Subsequent iterations extend the system to support richer interaction models, including a graphical user interface (GUI) and headless scripting support.

The final version of the application allows users to:
- Interactively create, edit, and view calendar events
- Use the application via a text-based interface or a GUI
- Run the calendar in headless mode for scripting and automation

# 2 Design & Architecture

The application is designed around a clear separation of concerns, following the Model–View–Controller (MVC) architectural pattern. The core calendar logic is encapsulated within the model and is fully decoupled from input/output mechanisms, allowing the system to support multiple interaction modes with minimal changes to the underlying implementation.

## 2.1 Core Model Design

The model represents the fundamental calendar concepts, including calendars and calendar events, using well-defined abstractions and concrete data types. Date and time handling relies on appropriate standard library types to ensure correctness and clarity, while maintaining flexibility for future extensions.

The model is intentionally independent of any user interface or I/O concerns. No GUI- or view-specific classes are referenced within the model layer, enabling the same core logic to be reused across command-line, GUI, and headless execution modes.

## 2.2 Extensibility and Abstraction

The design emphasizes forward compatibility. Abstractions are chosen to accommodate evolving requirements without requiring significant refactoring. New features are incorporated incrementally, with careful consideration given to minimizing disruption to existing components.

Key design goals include:
- Supporting multiple views without modifying the model
- Allowing different controllers for distinct interaction styles (interactive, headless, GUI)
- Ensuring new functionality can be added through extension rather than modification

## 2.3 MVC Adherence

The responsibilities of each layer are clearly defined:
- Model: Encapsulates core calendar data and business logic
- View: Handles presentation and user interaction
- Controller: Coordinates between user actions and model updates

Interactions between the view and controller are formalized through interfaces, allowing views to be swapped or extended with minimal coupling. The design continuously evaluates whether individual components can be changed independently, reinforcing proper MVC separation.

## 2.4 Design Principles

The system design is guided by the SOLID principles, with an emphasis on:
- Single Responsibility: Each class has a clearly defined purpose
- Open/Closed Principle: Behavior is extended through abstraction rather than modification
- Interface Segregation: Interfaces are kept focused and minimal
- Dependency Inversion: High-level components depend on abstractions, not concrete implementations
- Design changes introduced during feature expansion are documented and justified to ensure clarity and maintainability.
  
## 2.5 Testing Strategy

- Unit tests implemented using **JUnit** to verify model and controller logic
- **JaCoCo** used to measure code coverage and ensure comprehensive testing
- Mutation testing performed with **PIT** to validate test effectiveness

## 2.6 Packaging and Execution

The application is packaged as an executable JAR, allowing users to run the system from the command line with configurable arguments. File paths are handled in a platform-independent manner to ensure portability across environments.

## 2.7 Design Notes

The following design decisions reflect the current behavior of the system:

- **All-day events are a first-class concept.** An event carries an explicit `allDay` flag rather
  than being inferred from its start/end times. Events created with the `on <date>` form are
  all-day; events created with `from ... to ...` are timed. As a result, an ordinary timed meeting
  that happens to run 08:00–17:00 is no longer treated as all-day. CSV and iCalendar export rely on
  this flag to decide whether to write all-day or timed entries.
- **Event identity is subject + start + end.** Two events are considered the same (for duplicate
  detection, edits, copies, and deletes) when they share subject, start, and end. Other metadata
  (description, location, status, series membership, all-day flag) is intentionally excluded from
  identity.
- **Delete semantics mirror edit semantics.** `delete event` removes a single instance,
  `delete events` removes an event and all later events in its series, and `delete series` removes
  the whole series. A non-series event behaves the same under all three.
- **Timezone changes are transactional.** Changing a calendar's timezone builds the fully converted
  set of events and swaps it in atomically; if the conversion cannot be applied cleanly, the
  calendar is left unchanged rather than partially converted.
- **GUI parity.** The GUI exposes the previously text-only features (export to CSV/ICS, copying a
  range of events between calendars, busy/available status, and viewing a date range) through a
  **Tools** menu, in addition to creating all-day events and deleting events.
- **The build fails on test failures.** The test task no longer ignores failures, so a failing test
  fails the build.

# 3 Feature Set

The table below lists the available features at a high-level:

## Features

| Feature                                                                           | Supported in GUI | Supported in Headless | Supported in Interactive |  
|-----------------------------------------------------------------------------------|------------------|-----------------------|--------------------------|
| **1. Displaying calendar list**                                                   | Yes              | No                    | No                       |
| **2. Creating a calendar**                                                        | Yes              | Yes                   | Yes                      |
| **3. Selecting a calendar / switching between calendars**                         | Yes              | Yes                   | Yes                      |
| **4. Highlighting selected calendar**                                             | Yes              | No                    | No                       |
| **5. Month grid (clickable days)**                                                | Yes              | No                    | No                       |
| **6. Showing events for selected day**                                            | Yes              | Yes                   | Yes                      |
| **7. Showing events for range of days**                                           | Yes              | Yes                   | Yes                      |
| **8. Creating an event**                                                          | Yes              | Yes                   | Yes                      |
| **9. Creating an event series (count)**                                           | Yes              | Yes                   | Yes                      |
| **10. Creating an event series (until date)**                                     | Yes              | Yes                   | Yes                      |
| **11. Editing a single event**                                                    | Yes              | Yes                   | Yes                      |
| **12. Editing events of a series after this event**                               | Yes              | Yes                   | Yes                      |
| **13. Editing an entire event series**                                            | Yes              | Yes                   | Yes                      |
| **13a. Deleting a single event**                                                  | Yes              | Yes                   | Yes                      |
| **13b. Deleting events of a series from a point onwards**                         | Yes              | Yes                   | Yes                      |
| **13c. Deleting an entire event series**                                          | Yes              | Yes                   | Yes                      |
| **14. Informational messages (creation / edition / etc.)**                        | Yes              | Yes                   | Yes                      |
| **15. Error messages**                                                            | Yes              | Yes                   | Yes                      |
| **16. Default calendar creation on application start**                            | Yes              | No                    | No                       |
| **17. Exporting events to a .csv file**                                           | Yes              | Yes                   | Yes                      |
| **18. Exporting events to a .ics (iCal) file**                                    | Yes              | Yes                   | Yes                      |
| **19. Copying a single event from one calendar to another**                       | No               | Yes                   | Yes                      |
| **20. Copying events of a single day from one calendar to another**               | No               | Yes                   | Yes                      |
| **21. Copying events within a range of days from one calendar to another**        | Yes              | Yes                   | Yes                      |
| **22. User status on a given day (busy or available)**                            | Yes              | Yes                   | Yes                      |
| **23. Converting event times to the timezone of the calendar they are a part of** | Yes              | Yes                   | Yes                      |
| **24. Quiting the application**                                                   | Yes              | Yes                   | Yes                      |



The application is developed following the feature specifications described below for the text-based interface. Not all the features are exposed to the user through the GUI

## 3.1 Calendar-Level Operations

### Creating Calendars
- `create calendar --name <calName> --timezone area/location`

This command will create a new calendar with a unique name
and timezone as specified by the user. The expected timezone
format is the [IANA Time Zone Database format](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones). In this format the timezone is specified as "area/location". Few examples include "America/New_York", "Europe/Paris", "Asia/Kolkata", "Australia/Sydney", "Africa/Cairo", etc. The command is invalid if the user provides a non-unique calendar name or an unsupported timezone.

### Editing Calendars
- `edit calendar --name <name-of-calendar> --property <property-name> <new-property-value>`

This command is used to change/modify an existing property (`name` or `timezone`) of the
calendar. The command is invalid if the property being changed is absent
or the value is invalid in the context of the property.

### Using a Calendar
- `use calendar --name <name-of-calendar>`

A user can create/edit/print/export events in the context of a
calendar. They can use this command to set the calendar context.
Note this means that the commands specific to a single calendar (listed below) only
make sense when a calendar is in use (i.e. some calendar must be in use for them to work). Otherwise, they are invalid.

### Copying Events Between Calendars
- `copy event <eventName> on <dateStringTtimeString> --target <calendarName> to <dateStringTtimeString>`

The command is used to copy a specific event with the given name and
start date/time from the current calendar to the target calendar to start at the specified date/time. The "to" date/time is assumed to be specified in the timezone of the target calendar.

- `copy events on <dateString> --target <calendarName> to <dateString>  `

This command has the same behavior as the `copy event` above, except it
copies all events scheduled on that day. The times physically remain the same, except they are converted to the timezone of the target calendar (e.g. an event that starts at 2pm in the source calendar which is in EST would start at 11am in the destination calendar which is in PST).

- `copy events between <dateString> and <dateString> --target <calendarName> to <dateString>  `

The command has the same behavior as the other copy commands, except it copies all events scheduled in the specified date interval (i.e. overlaps with the specified interval). The date string in the target calendar corresponds to the start of the interval. The endpoint dates of the interval are inclusive.

In both the `copy events` commands, if an event series partly overlaps with the specified range, only those events in the series that overlap with the specified range should be copied, and their status as part of a series should be retained in the destination calendar.

### Exiting the Application
- `exit`

Stops listening for further commands.

## 3.2 Event-Level Operations

### Creating Events

- `create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>`

Creates a single event in the calendar. Note \<dateString\> is a string of the form "YYYY-MM-DD" \<timeString\> is a string of the form "hh:mm" and \<dateStringTtimeString\> is a string of the form "YYYY-MM-DDThh::mm".


- `create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`

Creates an event series that repeats N times on specific weekdays. Note \<weekdays\> is a
sequence of characters where character denotes a day of the week, e.g., MRU.
'M' is Monday, 'T' is Tuesday, 'W' is Wednesday, 'R' is Thursday, 'F' is Friday, 'S' is Saturday, and 'U' is Sunday.

- `create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateString>`

Creates an event series until a specific date (inclusive).

- `create event <eventSubject> on <dateString>`

Creates a single all day event.

- `create event <eventSubject> on <dateString> repeats <weekdays> for <N> times`

Creates a series of all day events that repeats N times on specific weekdays.

- `create event <eventSubject> on <dateString> repeats <weekdays> until <dateString>`

Creates a series of all day events until a specific date (inclusive).

For all of the above, the subject may have multiple words. Only in this case, the subject must be enclosed in double quotes.

### Editing Events

- `edit event <property> <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`

Identify the event that has the given subject and starts at the given date and time, and edit its property. This results in change in property for a single instance (irrespective of whether the identified event is single or part of a series).

- `edit events <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>`

Identify the event(s) that has the given subject and starts at the given date and time and edit its property. If this event is part of a series then the properties of all events in that series that start at or after the given date and time should be changed. If this event is not part of a series then this has the same effect as the command above.

- `edit series <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>`

Identify the event that has the given subject and starts at the given date and time and edit its property. If this event is part of a series then the properties of all events in that series should be changed. If this event is not part of a series then this has the same effect as the first edit command.

For all these queries the `<property>` field may be one of the following: *subject*, *start*, *end*, *description*, *location*, *status*. The format of the new property values are `string`, `dateStringTtimeString`, `dateStringTtimeString`, `string`, `string` and `string` respectively.

If an edition will result in any violations of the rule that two events cannot have the same subject, start and end date, then the edition should not occur. A useful error message may be printed in this case.

### Deleting Events

- `delete event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>`

Deletes the single event uniquely identified by the given subject, start, and end. This affects
only that one instance, whether or not it is part of a series.

- `delete events <eventSubject> from <dateStringTtimeString>`

Identifies the event with the given subject and start. If it is part of a series, this deletes that
event and all later events in the series. If it is not part of a series, only that event is deleted.

- `delete series <eventSubject> from <dateStringTtimeString>`

Identifies the event with the given subject and start. If it is part of a series, the entire series
is deleted. If it is not part of a series, this behaves like `delete event` for that instance.

### Queries

- `print events on <dateString>`

Prints a bulleted list of all events on that day along with their start and end time and location (if any).

- `print events from <dateStringTtimeString> to <dateStringTtimeString>`

Prints a bulleted list of all events that partly or completely lie in the given interval. Each event should be listed in a single line and must be in the following format: `<subject> starting on <startdate> at <starttime>, ending on <enddate> at <endtime>` including their start and end times and location (if any).

### Miscellaneous

- `export cal fileName.csv`

Exports the calendar as a csv file conforming to the specs listed [here](https://support.google.com/calendar/answer/37118?hl=en&co=GENIE.Platform%3DDesktop#zippy=%2Ccreate-or-edit-a-csv-file) that can be imported to the Google Calendar app. The command should also print the absolute path of the generated csv file.

- `export cal fileName.ics`

Exports the calendar as an iCal file conforming to the specs listed [here](https://support.google.com/calendar/answer/37118?hl=en&co=GENIE.Platform%3DDesktop&oco=1#zippy=%2Ccreate-or-edit-an-icalendar-file) that can be imported to the Google Calendar app. The command should also print the absolute path of the generated ics file.

- `show status on <dateStringTtimeString>`

Prints busy status if the user has events scheduled on a given day and time, otherwise, available.


# 4 Resources & Artifacts

This repository includes additional design and usage artifacts in the `res/`
directory, including:
- Class diagrams
- Sample input files for headless execution
- Screenshots of the graphical user interface

# 5 Project Evolution

**Iteration 1**
- Core system setup
- Implemented event-level operations
- Developed CLI and Headless modes

**Iteration 2**
- Extended system to support calendar-level operations
- Refactored design to improve modularity

**Iteration 3**
- Developed the GUI
- Final cleanup and documentation

# 6 How to Run
```
1. Clone the repository
2. Follow the instructions in USEME.md
```

# 7 Acknowledgement

This project was developed as a two-person team as part of the Programming
Design Paradigms course at Northeastern University, instructed by Prof. Amit
Shesh. The implementation in this repository represents the final consolidated
version of the project.
