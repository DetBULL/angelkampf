# Angelkampf (Fabric 1.20.1)

Ersetzt das Klick-Minispiel von **Tide 2** durch einen Drill mit Spannungsleiste.

## Steuerung
- **Rechtsklick halten**: einholen (Spannung steigt, Fortschritt wächst)
- **Loslassen**: Leine geben (Spannung sinkt, Fisch zieht zurück)
- Spannung zu lange im **roten** Bereich → Schnur reißt (Rute verliert Haltbarkeit, wenn die Mod auch auf dem Server ist)
- Leine zu lange **locker** → Fisch schüttelt sich los
- Fortschrittslinie voll → **gefangen** (nie im gelben Bereich gewesen = perfekter Fang bei Tide)

## Einflüsse (unsichtbar)
Fischart/-stärke aus Tide, Regen/Gewitter, Nacht, Strömung, Wassertiefe, Rute + Haltbarkeit-Verzauberung, Lava/Void.

## Einstellungen
Über **Mod Menu** (Modliste → Angelkampf → Einstellungen) oder eine frei belegbare Taste
(Steuerung → Angelkampf → „Einstellungen öffnen“). Größe und Höhe mit Live-Vorschau.
Gespeichert in `config/angelkampf.json`.

## Installation
- Client: `angelkampf-fabric-1.20.1-1.0.0.jar` in den `mods`-Ordner (neben Tide 2 und Fabric API).
- Server: optional. Nur nötig, damit die Rute beim Reißen Schaden nimmt.

## Bauen
GitHub baut die Mod automatisch (Tab **Actions** → letzter Lauf → Artefakt `angelkampf-mod`).
Lokal: `gradle build` (Java 17, Gradle 8.10) → `build/libs/`.
