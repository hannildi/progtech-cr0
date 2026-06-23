# AGENTS.md

## Cursor Cloud specific instructions

This repo is **step CR0** of a JUnit-driven Java coding exercise (see `README.md`). There is **no build system** (no Maven/Gradle) and the production classes the tests import are **intentionally absent** — implementing them is the exercise.

- Tech: Java 21 (`javac`/`java` on PATH) + JUnit 5. No databases or runtime services; the "app" is the unit-test suite.
- Two parallel variants of the same exercise:
  - English tests: `StoreCR0Tests.java` → expects package `org.store` (`Store`, `Cart`, `Item`, `Product`, `ProductPrice`).
  - Hungarian tests: `AruhazCR0Tesztek.java` → expects package `org.aruhaz` (`Aruhaz`, `Kosar`, `Tetel`, `Termek`, `TermekAr`).
- The JUnit 5 standalone console launcher is downloaded by the environment update script to `lib/junit-platform-console-standalone.jar` (git-ignored, not committed).

### Compile & run tests
The tests will only compile once the corresponding production classes exist (e.g. under `src/`). To compile and run, e.g. for the English variant:

```bash
javac -cp lib/junit-platform-console-standalone.jar -d out \
  $(find src -name '*.java' 2>/dev/null) StoreCR0Tests.java
java -jar lib/junit-platform-console-standalone.jar execute \
  --class-path out --select-class StoreCR0Tests
```

Out of the box (no production code yet), the above `javac` fails with `package org.store does not exist` — this is expected, not an environment problem. Verify the toolchain itself with a throwaway JUnit test compiled against the jar.

- Lint: none configured.
