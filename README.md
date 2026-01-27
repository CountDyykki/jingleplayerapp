# JinglePlayer User Manual

The JinglePlayer app for Android is inspired by Jonas Sitzmann's Linux Jingle Player https://github.com/jonasitzmann/jingle_player. 
This tool allows you to seamlessly integrate jingles into your active music stream (e.g., Spotify or web streams) based on your mobile calendar events.

## Functional Overview

The app automates jingle playback by syncing with a dedicated calendar. It ensures precise timing by scheduling jingles so that they finish exactly at the event's milestone:

* Start Jingle: Ends exactly when the calendar event begins.
* Pre-End Jingle (PrEnd): Ends at a user-defined interval before the event conclusion.
* End Jingle: Ends exactly when the calendar event expires.

## Installation
To install the application, follow these steps:
* Download: Obtain the latest APK from the Official GitHub Repository. https://github.com/CountDyykki/jingleplayerapp/blob/main/app/build/outputs/apk/debug/app-debug.apk 
* Install: Open the APK on your Android device. You may need to "Allow installation from unknown sources" in your security settings.
* Permissions:
    * Locate the JinglePlayer icon.
    * Long-press the icon and select App Info.
    * Navigate to Permissions and grant access to the Calendar.
* Launch: You are now ready to open the app.

## Configuration & First Test
To verify the setup, perform a test run using the built-in test calendar:
* Initialize Audio: Start your preferred music stream (Spotify, Radio, etc.).
* Open JinglePlayer: Launch the application.
* Select Jingles:
     Use the default sounds or tap the Start, PreEnd, and End buttons to select custom audio files from your device.
* Set Timing: In the Timing section, define the "Minutes before end" for the Pre-End jingle.
        Note: Setting this to 0 will disable the Pre-End jingle.
* Run Test: Tap TestCalendar. This generates dummy events starting in the immediate future to confirm the playback logic and ducking behavior.

## Serious Match Day Operation
For live events, you simply need to add the definition of your events. Follow this workflow:
* Calendar Setup: Create a dedicated calendar in your Android Calendar App. Add events with specific start and end times.
* Prepare Stream: Start your background music.
* Active JinglePlayer:
   * Select your custom jingle files.
   * Set the Pre-End timing interval.
   * Tap Select Calendar and choose your live match-day calendar.
* Automation: The app will automatically queue and play the jingles at the calculated timestamps.

## Contributions
... are always welcome. Get in touch!
