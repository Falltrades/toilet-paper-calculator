# 🛒 Smart Shopping Calculator — Android

Application Android native (Kotlin + WebView) qui emballe le calculateur web dans une vraie app Android.

## Structure du projet

```
smart-shopping-calculator-android/
├── app/
│   └── src/main/
│       ├── assets/index.html          ← L'app web complète (locale)
│       ├── java/com/smartshopping/
│       │   ├── MainActivity.kt        ← WebView host
│       │   └── AndroidBridge.kt      ← Pont JS ↔ Kotlin
│       └── res/…                      ← Icônes, layouts, thèmes
├── .github/workflows/build.yml        ← CI/CD GitHub Actions
└── key.properties                     ← (non versionné) pour la signature
```

## Build local (Android Studio)

1. Ouvrir le dossier dans **Android Studio Hedgehog** (2023.1+)
2. Laisser Gradle sync se terminer
3. `Build → Make Project` ou `Run`

## Build en ligne de commande

```bash
# Debug
./gradlew assembleDebug

# Release (nécessite key.properties, voir ci-dessous)
./gradlew assembleRelease
./gradlew bundleRelease
```

## Signature (release)

Créer un keystore :
```bash
keytool -genkey -v -keystore my-release.jks \
  -alias my-key -keyalg RSA -keysize 2048 -validity 10000
```

Créer `key.properties` à la racine (déjà dans `.gitignore`) :
```properties
storePassword=votre_mot_de_passe
keyPassword=votre_mot_de_passe_clé
keyAlias=my-key
storeFile=my-release.jks
```

## GitHub Actions — Secrets requis

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | `base64 -w0 my-release.jks` |
| `KEYSTORE_PASSWORD` | Mot de passe du keystore |
| `KEY_PASSWORD` | Mot de passe de la clé |
| `KEY_ALIAS` | Alias de la clé (ex: `my-key`) |

> Pour les PRs, un APK debug est produit sans signature.  
> Pour les pushs sur `main`, un APK signé + AAB sont publiés en GitHub Release.

## Tests de non-régression

### Playwright — E2E Web (`tests/e2e/`)

Couvre toute la logique métier dans `index.html` : les 4 méthodes papier toilette, les poudres protéinées, le ranking, les cas limites, et un viewport mobile (Pixel 7).

```bash
cd tests/e2e
npm ci
npx playwright install --with-deps chromium firefox
npx playwright test               # headless CI
npx playwright test --headed      # avec fenêtre visible
npx playwright test --ui          # mode interactif
npx playwright show-report        # rapport HTML après exécution
```

| Suite | Fichier | Nb tests |
|-------|---------|----------|
| Navigation & Dark Mode | `navigation.spec.ts` | 7 |
| Toilet Paper (4 méthodes) | `toilet-paper.spec.ts` | 22 |
| Protein Powder | `protein.spec.ts` | 18 |
| Edge Cases & Mobile | `edge-cases.spec.ts` | 11 |

### JUnit5 + Mockk — Tests unitaires Kotlin (`tests/unit/`)

Couvre `AndroidBridge`, toutes les formules de calcul, le ranking, et la logique `MainActivity` (cycle de vie, dark mode, navigation).

```bash
cd tests/unit
./../../gradlew test
# rapport : tests/unit/build/reports/tests/test/index.html
```

| Fichier | Ce qui est testé |
|---------|-----------------|
| `AndroidBridgeTest.kt` | Toast, dark mode système, version app |
| `CalcLogicTest.kt` | Toutes les formules (poids, feuilles, diamètre, protéines, ranking) |
| `MainActivityTest.kt` | Cycle de vie WebView, injection JS, swipe refresh, retour |

### Pipeline CI

```
push → test-kotlin ─┐
                     ├─ build → release
push → test-playwright ─┘
```
Le build ne se lance **que si les deux suites de tests passent**.

## Pont JavaScript ↔ Android

L'app expose `window.AndroidBridge` dans la WebView :

```javascript
// Afficher un Toast natif
AndroidBridge.showToast("Sauvegardé !");

// Détecter le mode sombre système
const dark = AndroidBridge.isSystemDarkMode(); // boolean

// Version de l'app
const v = AndroidBridge.getAppVersion(); // "1.0.0"
```
