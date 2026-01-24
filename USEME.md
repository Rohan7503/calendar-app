#### This document explains how to run the **Calendar Application** by building and using the JAR file.

---
## Building the JAR file:
To build the JAR file, run:
```bash
./gradlew jar
```
This will create a JAR file in the `build/libs` directory



## Running the Application:
### 1. GUI Mode (Default)  
The GUI mode allows you to interact with the application through a Graphical User Interface.  
Operating the GUI: [**Link**](#how-to-use-the-gui)

**Command:**  
```bash
java -jar <JAR path>.jar
```
**Instructions on how to use the GUI are [here](#how-to-use-the-gui)**


### 2. Interactive Mode
The interactive mode allows you to enter commands manually through the terminal.

**Command:**
```bash
java -jar <JAR path>.jar --mode interactive
```
You will then be prompted to enter calendar commands directly (e.g., creating, editing, or printing events).  
To exit, use the "Exit" command.


### 3. Headless Mode   
In headless mode, the program reads commands from a text file instead of user input.

**Command:**
```bash
java -jar <JAR path>.jar --mode headless <filename.txt>
```
Example:
```bash
java -jar <JAR path>.jar --mode headless res/sample_commands.txt
```
Here, `sample_commands.txt` should contain valid commands, one per line. The application will execute them sequentially and print the results to the console.

---
### Example Runs:
**Example 1:**
```bash
java -jar build/libs/calendar-1.0.jar
```
Starts in GUI mode (default)  

**Example 2:**
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```
Starts in interactive mode  

**Example 3:**
```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/sample_commands.txt
```
Executes all commands in `sample_ommands.txt` and displays the output in the terminal.  

---






## How to use the GUI

The GUI is divided into 4 panes, as shown below:  

![Image of the GUI](res/gui_images/panes.png)

### Calendars Pane:
- The left pane displays the available calendars, the selected (active) one is highlighted in orange.  
- By default, when the application is run, a default calendar is created and made active.
![Image of the calendars pane](res/gui_images/lpane.png)

### Day Events Pane:
- The right pane displays all events scheduled **for the selected day** (highlighted in yellow as shown below).
- Each of the events also have an edit button, which allows you to edit that particular event alone
![Image of the day events pane](res/gui_images/rpane.png)

### Month Grid Pane:
- The buttons to the left and right of the month name at the top of this pane can be used to navigate to different
month views.
- Selected day is highlighted in yellow. Image is shown above

### Top Action Buttons Pane
- This pane contains the main action buttons for the calendar application.
- Clicking on any of the action buttons opens a dialog box (as shown below),
where the date fields are autopopulated if any day was selected on the month grid
before clicking on the action buttons.
- Event times are defaulted to the "all-day" event hours (8am - 5pm), but can be changed
in the dialog box
![Image of the create event series until date dialog box](res/gui_images/tpane.png)

### Example flow:

1. Run the application in GUI mode (no arguments).  
2. A calendar is created and selected: "Default".  
3. Create another calendar if you want to, or proceed to creating events/event series. Creating a calendar automatically
sets that calendar as active (as seen by the orange highlight, which would have moved to the created calendar).  
4. Create an event either by directly clicking one of the create event/series buttons and manually entering the dates
for the event, or, select the day for which you want to create the event in the month grid, and then click one of the
create event/series buttons to see the date fields autopopulated. The time fields are defaulted to
all-day event timings (8am - 5pm).
5. View the events for a day on the right pane.
6. Edit a particular event if needed, or edit multiple events using the top action pane button.
7. The edition is reflected in the right pane on the correct day.
8. Switch to a different calendar if needed by clicking on it.
9. Close the window to quit the program.