# Ltt.rs for Android

A proof of concept email ([JMAP](https://jmap.io/)) client currently in development. It makes heavy use of [Android Jetpack](https://developer.android.com/jetpack/) for a more maintainable code base than some of the preexisting Android email clients.

<img src="https://gultsch.de/files/lttrs-android.png" height="450"/>

## jmap-mua

Ltt.rs is based on [jmap-mua](https://github.com/iNPUTmice/jmap) which is basically a headless email client or a library that handles everything an email client would aside from data storage and UI. There is also lttrs-cli which uses the same library.

## Try it

At this stage of development trying it means compiling and setting up the latest development version of [Cyrus](https://github.com/cyrusimap/cyrus-imapd) and then putting your credentials into [Credentials.java](https://github.com/iNPUTmice/lttrs-android/blob/master/app/src/main/java/rs/ltt/android/Credentials.java).
