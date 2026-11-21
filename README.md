# Any Armor Stand (Fabric, Minecraft 1.21.11)

Sieh einen Armor Stand an und drücke **J**. Es öffnet sich ein Menü mit allen Items, die normalerweise
nicht in einen Armor Stand passen. Ein Klick auf ein Item setzt es auf den Kopf des Armor Stands.

- Suchfeld und Seiten-Knöpfe (< >) im Menü.
- Kreativmodus: Items sind kostenlos.
- Überleben: Das Item muss in deinem Inventar sein, ein Stück wird verbraucht.
- Ein Item, das schon auf dem Kopf sitzt, wird getauscht und landet in deinem Inventar.
- Gesperrte Slots und Marker-Armor-Stands werden respektiert.
- Die Taste lässt sich unter Einstellungen > Steuerung ändern.
- Auf Servern muss die Mod auf dem Server und beim Spieler installiert sein.

## Die .jar ohne Installation bauen (GitHub)

1. Erstelle auf github.com ein neues Repository und lade den **Inhalt dieses Ordners** hoch
   (Add file > Upload files; der Ordner `.github` muss mit hochgeladen werden).
2. Öffne den Reiter **Actions**. Der Build startet automatisch (oder "build" > "Run workflow").
3. Wenn er grün ist, klicke auf den Durchlauf und lade unten unter **Artifacts** `any-armor-stand` herunter.
4. Entpacke das heruntergeladene ZIP: darin liegt `any-armor-stand-1.0.0.jar`.
   Genau diese .jar kommt in den `mods`-Ordner oder auf Modrinth.

## Lokal bauen

Voraussetzung: Java 21 und Internet.

    gradle wrapper      # einmalig, erzeugt gradlew
    ./gradlew build     # Windows: gradlew.bat build

Die .jar liegt danach in `build/libs/` (nicht die mit `-sources` im Namen).
Du brauchst zusätzlich Fabric API (Fabric Loader 0.18.4 oder neuer).
