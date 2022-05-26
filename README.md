# CursorMacro

A tool for automating mouse and keyboard tasks. Includes a recorder for capturing mouse or keyboard inputs and a player for broadcasting them back.

## Documentation

Read the wiki for details on how each feature works, as well as how to implement your own recorders or players.

## Advantages

- Open source - Code can be adjusted to your needs
- Highly customizable - Many different options for running the player or recorder
- Extendable base - You can add custom recorders or instructions to suit your needs if the options aren't enough
- Seed randomizer - Instead of having a randomizer that calculates delays at runtime, CursorMacro instead calculates delays beforehand, allowing for deterministic randomization
- Delay accounting - The delays are benchmarked to System.currentTimeMillis(), ensuring accurate delays
- Text-based compilation - CursorMacro uses a text pane to compile the script needed for the player, allowing you to use your favorite text editor to tweak the script beforehand

## Disadvantages

- The text editor gets very messy when the script is long
- This program only works for 64-bit windows versions (but one could compile the C++ sources to support other operating systems)

## Screenshot

![swing](swing.png)

## Building

This project makes use of native code. The native methods are all located in CursorMacro.java. To compile in Windows, first install Cygwin. Install mingw64-x86_64-gcc-g++ and launch the Cygwin terminal.

Then, run the following command to assemble the C++ code (ensure JDK is the main path):

```
x86_64-w64-mingw32-g++ -c -I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32 -I"src\cpp" "src\cpp\com_thistestuser_cursormacro_CursorMacro.cpp" -o "com_thistestuser_cursormacro_CursorMacro.o"
```
Finally, create a dll file:

```
x86_64-w64-mingw32-g++ -shared -o "src\lib\mouseutils.dll" "com_thistestuser_cursormacro_CursorMacro.o" -Wl,--add-stdcall-alias
```

For details on how to compile for other operating systems, see the tutorial at [Baeldung](https://www.baeldung.com/jni).
