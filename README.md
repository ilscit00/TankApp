# Tankzeit

Native Android-App (Kotlin, Jetpack Compose) zum Vergleichen von Kraftstoffpreisen
in Deutschland und zur Prognose des günstigsten Tank-Zeitpunkts.

## Features

- **Preise**: Tankstellen im Umkreis (1–25 km) nach Preis sortiert, Badge für die
  günstigste Station. Standort per Postleitzahl oder GPS.
- **Prognose**: stündliche Preiskurve für heute + Wochenmuster, Basis: dokumentiertes
  ADAC/MTS-K-Tagesmuster deutscher Kraftstoffpreise, optional verfeinert durch den
  aktuellen Brent-Rohölpreistrend (stark gedämpft, damit die Kurve robust bleibt).
- **Einstellungen**: Kraftstofftyp (E5/E10/Diesel), Standortmodus, Suchradius,
  API-Keys — alles persistiert über Jetpack DataStore.

## Setup in Android Studio

1. Projektordner in Android Studio öffnen ("Open" → diesen Ordner auswählen).
2. Android Studio lädt beim ersten Öffnen automatisch den Gradle-Wrapper
   (Version 8.7) herunter — Internetverbindung wird dafür benötigt.
3. Gradle Sync abwarten, dann auf einem Gerät/Emulator mit **Android 8.0 (API 26)
   oder höher** ausführen (`minSdk 26`, `targetSdk 33`, `compileSdk 34`).
4. Beim ersten Start unter **Einstellungen** die beiden API-Keys eintragen:
   - Tankerkönig-Key: https://creativecommons.tankerkoenig.de
   - Alpha-Vantage-Key (optional): https://www.alphavantage.co
5. Ohne Tankerkönig-Key zeigt die App klar gekennzeichnete Demo-Daten an, damit
   das UI auch ohne Key sofort nutzbar ist.

## Architektur

```
app/src/main/java/de/tankzeit/app/
├── data/
│   ├── model/       Datenklassen (Station, Forecast, ...)
│   ├── remote/       Retrofit-Interfaces für Tankerkönig & Alpha Vantage
│   ├── repository/   FuelRepository, ForecastRepository
│   └── settings/      DataStore-basierte Settings-Persistenz
├── forecast/          ForecastEngine – Kernlogik der Preisprognose
├── location/          GPS + PLZ-Geocoding (FusedLocationProvider, Geocoder)
├── ui/
│   ├── theme/         Compose Material3 Theme
│   ├── navigation/     Bottom-Navigation mit 3 Tabs
│   ├── components/     Wiederverwendbare Composables (z. B. StationCard)
│   └── screens/        Preise, Prognose, Einstellungen (je Screen + ViewModel)
└── MainActivity.kt
```

## Hinweis zu CORS

Der Browser-Prototyp war durch CORS-Restriktionen bei Live-API-Aufrufen
eingeschränkt. Das betrifft die native App nicht: Die Netzwerkzugriffe laufen
über OkHttp/Retrofit direkt auf Betriebssystemebene, ohne Browser-Sandbox.

## Nächste sinnvolle Schritte

- Google Play Services Location-Berechtigung im Detail-Flow testen (Runtime
  Permission wird aktuell direkt beim Öffnen des Preise-Tabs angefragt, wenn
  GPS als Standortmodus gewählt ist).
- Caching der zuletzt geladenen Stationsliste (z. B. Room) für Offline-Anzeige.
- Unit-Tests für `ForecastEngine` (reine Funktion, gut testbar).
- App-Icon durch ein finales Design ersetzen (aktuell ein einfaches Platzhalter-Icon).
