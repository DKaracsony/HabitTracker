# 📱 Habit Tracker (Android)

Mobilná aplikácia na sledovanie návykov vytvorená ako bakalárska práca na Univerzite Konštantína Filozofa v Nitre.

Aplikácia pomáha používateľom budovať pozitívne návyky a eliminovať negatívne správanie prostredníctvom prehľadného rozhrania, štatistík a gamifikačných prvkov.

---

##  Hlavné funkcionality

-  Vytváranie a správa návykov (Build / Quit)
-  Sledovanie pokroku (denné, týždenné, mesačné ciele)
-  Štatistiky a vizualizácie:
    - Line chart (vývoj v čase)
    - Bar chart (týždenný prehľad)
    - Pie chart (úspešnosť)
-  Gamifikácia:
    - Streaks (aktuálna a najdlhšia séria)
    - Habit strength (sila návyku)
    - Motivačné prvky
-  Používateľský systém:
    - Registrácia / prihlásenie
    - Overenie e-mailu
    - Reset hesla
-  Online režim (Firebase):
    - Synchronizácia dát
    - Zdieľanie návykov medzi používateľmi
-  Offline režim:
    - Lokálne ukladanie pomocou SQLite
-  Lokalizácia:
    - Slovenčina / Angličtina
-  Denné notifikácie (reminders)

---

##  Použité technológie

**Mobile development**
- Java
- XML (Android UI)
- Android Studio

**Data & backend**
- SQLite (lokálne úložisko)
- Firebase Authentication
- Firebase Firestore

**Libraries**
- MPAndroidChart (vizualizácia dát)

**Design**
- Figma
- Inkscape

---

##  Inštalácia aplikácie

Aplikáciu je možné nainštalovať pomocou APK súboru:

👉 **[Stiahnuť APK](https://mega.nz/folder/MtkHTCwb#Ai4oKH7jSnFAOlEsdUN3nw)**

---

##  Architektúra aplikácie

Aplikácia kombinuje **lokálne a cloudové úložisko**:

-  SQLite → offline režim
-  Firebase → používateľský systém + synchronizácia + sociálne funkcie

Tento prístup umožňuje:
- používanie aplikácie bez internetu
- rozšírené funkcie po prihlásení

---

##  Štatistiky a gamifikácia

Aplikácia využíva viacero spôsobov vizualizácie:

- Denné, týždenné a mesačné grafy
- Dynamické vyhodnocovanie splnenia cieľov
- Systém streakov pre motiváciu používateľa

---

##  Bezpečnosť

- Autentifikácia riešená cez Firebase Authentication
- Prístup k dátam riadený pomocou Firebase Security Rules
- Každý používateľ má unikátne UID

---

##  Testovanie

Aplikácia bola testovaná:
- na viacerých Android zariadeniach
- na rôznych verziách systému (od Android 7 / API 24)
- v offline aj online režime

---

##  Stav projektu

Projekt bol úspešne dokončený ako bakalárska práca a je plne funkčný.

Možné budúce rozšírenia:
- rozšírené sociálne funkcie
- pokročilé analýzy dát
- cloudová synchronizácia v reálnom čase

---

##  Autor

**Dávid Karácsony**  
Študent aplikovanej informatiky  
Univerzita Konštantína Filozofa v Nitre

---

##  GitHub repozitár

👉 https://github.com/DKaracsony/HabitTracker
