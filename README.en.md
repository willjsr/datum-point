# Datum Point

Datum Point is an open source Android app for GNSS reconnaissance and field checking with a smartphone.

It collects multiple GNSS fixes, filters weak readings, computes a representative coordinate, records dispersion statistics, and exports data for surveying, CAD, and GIS workflows.

Datum Point is not an RTK receiver and does not replace geodetic receivers, total stations, PPK workflows, cadastral precision surveys, or official georeferencing.

The project is offline-first and privacy-first: no login, no ads, no analytics, and no backend by default.

Build:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

License: MIT for original project code. Third-party dependencies keep their own licenses.
