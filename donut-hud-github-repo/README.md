# Donut HUD

Client-side Fabric mod for Minecraft 1.21.11: themed black HUD, sound detector, block/entity ESP.

- Right Shift: settings
- J: toggle sound detector
- K: toggle block + entity ESP

## Build
Push to GitHub, open the **Actions** tab, open the latest run, download the `donut-hud-jar` artifact.
Use the jar WITHOUT `-sources` in the name. Needs Fabric Loader and Fabric API installed.

Locally: `gradle build` (Gradle 9, Java 21), jar ends up in `build/libs/`.
