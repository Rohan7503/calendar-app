## Design Changes

---
The changes were limited to the view and the controller; the model remains unchanged.  
- A new interface (`CalGuiInterface`) and its implementation (`CalGuiImpl`) was added to the view's package to support the GUI.  
- A new interface (`Features`) and it's implementation (`GuiControllerImpl`) was added to the controller's package to define the callback functions that the GUI would call.  


| Change                                                | What Was Modified                                                                                                                         | Why It Was Needed                                                                                                                                                                |
|-------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **1. Added new `Features` interface**                 | Introduced a callback interface implemented by the controller for all GUI-initiated actions.                                              | Enforces proper MVC: the view does not directly call model methods; it delegates user actions to the controller.                                                                 |
| **2. Added new `CalGuiInterface` interface**          | Defines all methods that the view exposes for the controller to update the GUI.                                                           | Isolates the Swing implementation from the rest of the program and allows for multiple views to coexist, thereby letting the controller use whichever view as and when required. |
| **3. Added `runGui()` method in `CalControllerImpl`** | Added a dedicated method responsible for constructing the view and launching the GUI instead of starting it in the constructor or `main`. | Provides a clean entry point for the GUI, and clean extensibility; Supporting additional UI modes without modifying the model.                                                   |

## Features

| Feature                                                                           | Supported in GUI | Supported in Headless | Supported in Interactive |  
|-----------------------------------------------------------------------------------|------------------|-----------------------|--------------------------|
| **1. Displaying calendar list**                                                   | Yes              | No                    | No                       |
| **2. Creating a calendar**                                                        | Yes              | Yes                   | Yes                      |
| **3. Selecting a calendar / switching between calendars**                         | Yes              | Yes                   | Yes                      |
| **4. Highlighting selected calendar**                                             | Yes              | No                    | No                       |
| **5. Month grid (clickable days)**                                                | Yes              | No                    | No                       |
| **6. Showing events for selected day**                                            | Yes              | Yes                   | Yes                      |
| **7. Showing events for range of days**                                           | No               | Yes                   | Yes                      |
| **8. Creating an event**                                                          | Yes              | Yes                   | Yes                      |
| **9. Creating an event series (count)**                                           | Yes              | Yes                   | Yes                      |
| **10. Creating an event series (until date)**                                     | Yes              | Yes                   | Yes                      |
| **11. Editing a single event**                                                    | Yes              | Yes                   | Yes                      |
| **12. Editing events of a series after this event**                               | Yes              | Yes                   | Yes                      |
| **13. Editing an entire event series**                                            | Yes              | Yes                   | Yes                      |
| **14. Informational messages (creation / edition / etc.)**                        | Yes              | Yes                   | Yes                      |
| **15. Error messages**                                                            | Yes              | Yes                   | Yes                      |
| **16. Default calendar creation on application start**                            | Yes              | No                    | No                       |
| **17. Exporting events to a .csv file**                                           | No               | Yes                   | Yes                      |
| **18. Exporting events to a .ics (iCal) file**                                    | No               | Yes                   | Yes                      |
| **19. Copying a single event from one calendar to another**                       | No               | Yes                   | Yes                      |
| **20. Copying events of a single day from one calendar to another**               | No               | Yes                   | Yes                      |
| **21. Copying events within a range of days from one calendar to another**        | No               | Yes                   | Yes                      |
| **22. User status on a given day (busy or available)**                            | No               | Yes                   | Yes                      |
| **23. Converting event times to the timezone of the calendar they are a part of** | Yes              | Yes                   | Yes                      |
| **24. Quiting the application**                                                   | Yes              | Yes                   | Yes                      |

