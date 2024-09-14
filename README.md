# TravelPlanner Android App

TravelPlanner je Android aplikacija osmišljena kako bi pomogla korisnicima da planiraju svoja putovanja na jednostavan i organizovan način. Korisnici mogu kreirati spisak zadataka, destinacija i pratiti sve potrebne informacije o svom putovanju.

## Funkcionalnosti

- Kreiranje putovanja sa destinacijama.
- Dodavanje zadataka vezanih za putovanje.
- Sinhronizacija podataka sa Firebase Firestore.
- Interfejs prilagođen mobilnim uređajima.
- Upravljanje zadacima na jednostavan način.

## Tehnologije korišćene

- **Programski jezik**: Kotlin
- **Android SDK**: Android 5.0 (Lollipop) ili noviji
- **Biblioteke**:
  - Firebase Firestore (za skladištenje podataka u oblaku)
  - Android Jetpack Components (LiveData, ViewModel, Room, itd.)
  - Material Design Components

## Instalacija

1. **Preuzimanje projekta**:
   - Kloniraj repozitorijum koristeći Git:
     ```bash
     git clone https://github.com/tvoje-korisnicko-ime/ime-repozitorijuma.git
     ```

2. **Postavljanje Firebase-a**:
   - Otvori [Firebase Console](https://console.firebase.google.com/), kreiraj novi projekat i preuzmi `google-services.json` fajl.
   - Stavi `google-services.json` fajl u direktorijum `app/` projekta.

3. **Pokretanje projekta**:
   - Otvori projekat u Android Studiju.
   - Sinhronizuj Gradle fajlove.
   - Pokreni aplikaciju na Android emulatoru ili fizičkom uređaju.

## Korišćenje aplikacije

1. Kada aplikacija bude pokrenuta, kreiraj nalog ili se prijavi koristeći postojeće podatke.
2. Dodaj novo putovanje, definiši destinacije i zadatke vezane za putovanje.
3. Prati zadatke i uživaj u dobro organizovanom putovanju!

## Prilozi

- [Dokumentacija Firebase-a](https://firebase.google.com/docs)
- [Android Jetpack Components](https://developer.android.com/jetpack)

## Autor

- **Ime i Prezime** - [GitHub](https://github.com/LukaRakic00)
