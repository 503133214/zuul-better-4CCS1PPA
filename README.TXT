Project: The Last Lantern (World of Zuul coursework)

The Last Lantern is a complete text-adventure extension of the supplied
zuul-better BlueJ project. The player explores Lantern Keep, manages a
weight-limited inventory, feeds a dwarf with a three-word command, receives a
star wand, and relights the moon beacon to win.

RUN
---
BlueJ: open this folder, create a Game object, and call play().
JAR:   java -jar The-Last-Lantern.jar

The executable JAR is built with Game as its Main-Class. The source remains in
the default package so it opens naturally as a BlueJ teaching project.

COMMANDS
--------
go, back, look, take, drop, inventory, examine, talk, give, use, hint, wait,
help, quit

Examples:
  go north
  take bread
  give bread dwarf
  use wand beacon

WINNING WALKTHROUGH
-------------------
  go north
  go east
  take bread
  go west
  go north
  go east
  give bread dwarf
  go west
  go north
  use wand beacon

AUTOMATED TESTS
---------------
Compile all Java files, then run:
  java -ea GameTests

The dependency-free regression suite checks three-word parsing, multiple room
items, fixed items, exact/overweight carrying, multi-step backtracking, moving
characters, random transport, item rules, and the complete winning path.

FEATURE SUMMARY
---------------
- Nine rooms with stable, bidirectional ordinary exits.
- Any number of items per room; portable and fixed scenery items.
- Item weights and a 6 kg player carry limit.
- Full travel history for repeated back commands.
- Eleven new commands beyond go/help/quit.
- A recognised, clearly announced winning condition.
- A clockwork raven that moves independently.
- Three-word commands including give bread dwarf and use wand beacon.
- A shimmer chamber that transports the player to a random ordinary room.
- Injectable input/output/randomness and a no-library regression test suite.

Known bugs: none found in the supplied automated and scripted tests.
