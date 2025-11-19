#### This document explains how to run the **Calendar Application** by building and using the JAR file.

---
## Building the JAR file:
To build the JAR file, run:
```bash
./gradlew jar
```
This will create a JAR file in the `build/libs` directory

## Running the Application:
### 1. Interactive Mode (Default)  
The interactive mode allows you to enter commands manually through the terminal.  

**Command:**  
```bash
java -jar <JAR path>.jar
```
or explicitly:
```bash
java -jar <JAR path>.jar --mode interactive
```
You will then be prompted to enter calendar commands directly (e.g., creating, editing, or printing events).  
To exit, use the "Exit" command.


### 2. Headless Mode   
In headless mode, the program reads commands from a text file instead of user input.

**Command:**
```bash
java -jar <JAR path>.jar --mode headless <filename.txt>
```
Example:
```bash
java -jar <JAR path>.jar --mode headless res/commands.txt
```
Here, commands.txt should contain valid commands, one per line.  
The application will execute them sequentially and print results to the console.

---
### Example Runs:
**Example 1:**
```bash
java -jar build/libs/calendar-1.0.jar
```
Starts in interactive mode (default)  

**Example 2:**
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```
Starts in interactive mode  

**Example 3:**
```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt
```
Executes all commands in `inputCommands.txt` and displays the output in the terminal.