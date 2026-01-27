INTRODUCTION

The jingleplayer app is inspired by Jonas Sitzmann's Linux version (https://github.com/jonasitzmann/jingle_player).
With the help of this android app you can play music from your phone (e.g. from spotify or any stream) and interrupt the music stream be play a Jingle. 
The timing of the jingles is defined by calendar events from a predefined calendar in your calendar app.

There are two mandatory jingles which are played at the start and end of the calendar event and an optional jingle played x minutes before the end of the calendar event (PrEnd).

Keep in mind, that the app will scheudle the jingles in such a way that the end of the jingle will coincide with the corresponding calendar event Start/PreEnd/End. So once the Start jingle is finished the calendar Event will start.

There is a set of defaut jingles available in the app. You can alternatively define your own jingles to be used.

INSTALLATION

* Download the following apk file on your phone:
https://github.com/CountDyykki/jingleplayerapp/blob/main/app/build/outputs/apk/debug/app-debug.apk
* Install the apk file on your phone and check that you trust the owner. The app is called JinglePlayer
* Long-Click on the app icon and choose App-Info. Allow access to the Calendar
* You should now be able to start the app

A FIRST TEST RUN
* Start your Music Stream
* Start the JinglePlayer App
* Configure Section Select Jingles:
If you want to play user-defined jingles select them from your phone using the Start, PreEnd and End Buttons
* Configure Section Timing:
Define how minutes before the end of the event you want to the PreEnd Jingle to be played. Default is 5 Minutes. If you choose 0 minutes the PreEnd jingle will be skipped.
* Select TestCalender:
  Klick on TestCalender and select the TestCalender for a first test. This calendar will start events a few minutes from now to check if everything is working as planned
* The Jingles will be added to the playlist and played one after the other

SERIOUS MATCH DAY
* Add a new calendar to your calendar app and define events in this calendar with name, start time and end time.
* Start your Music Stream
* Start the JinglePlayer App
* Configure your Jingles
* Configure the PreEnd Timing
* Select your Calendar
* The Jingles will be added to the playlist and played one after the other
 

