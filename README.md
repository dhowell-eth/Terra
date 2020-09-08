# Terra
Terra Android App Project

Terra is an Android app I am developing for scientists to use for logging and managing data while in the field.
It currently has functionality for viewing and logging geographic locations as well as taking compass measuremens (3D planes, lines, and 2D bearings).

https://play.google.com/store/apps/details?id=com.blueridgebinary.terra

## Building

This project should be built using [Bazel](https://docs.bazel.build/versions/master/be/android.html). To build an unsigned apk, run `$ bazel build terra` in [app/src/main](./app/src/main). Note that you'll need
to update the Google Maps API token in [app/src/main/res/values/keys.xml](app/src/main/res/values/keys.xml) to a real value.
