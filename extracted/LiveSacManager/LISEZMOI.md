# Live Sac Manager

Application Android 100 % hors ligne pour gérer les commandes de sacs pendant les Facebook Live.
Kotlin · Jetpack Compose · Room (SQLite). Aucune permission, aucun compte, aucun serveur.

## Obtenir l'APK avec Android Studio (méthode recommandée)

1. Installez **Android Studio** (version stable la plus récente) : https://developer.android.com/studio
2. Décompressez `LiveSacManager.zip`.
3. Dans Android Studio : **File > Open**, choisissez le dossier `LiveSacManager` (celui qui contient `settings.gradle.kts`).
4. Attendez la fin de **Gradle Sync** (barre de progression en bas). La première fois, il télécharge des composants : prévoyez 5 à 15 minutes et une connexion Internet.
   - Si Android Studio propose d'installer une plateforme SDK manquante (« Android SDK Platform 35 »), acceptez.
5. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Quand « APK(s) generated successfully » s'affiche, cliquez sur **locate**. Le fichier est ici :
   `app/build/outputs/apk/debug/app-debug.apk`

## Installer l'APK sur votre téléphone

1. Copiez `app-debug.apk` sur le téléphone (câble USB, Google Drive, WhatsApp à vous-même, e-mail…).
2. Ouvrez le fichier depuis le gestionnaire de fichiers du téléphone.
3. Android demande d'autoriser l'installation depuis cette source (Chrome, Fichiers, WhatsApp…) : autorisez-la.
4. Si « Play Protect » affiche un avertissement (normal pour une application qui ne vient pas du Play Store), choisissez **Installer quand même**.
5. Ouvrez **Live Sac Manager**.

## Alternative sans Android Studio (optionnelle)

Le dossier `.github/workflows` contient une recette GitHub Actions qui compile l'APK en ligne :
1. Créez un dépôt GitHub privé et envoyez-y tout le contenu du projet.
2. Onglet **Actions > Compiler l'APK > Run workflow**.
3. À la fin, téléchargez l'APK dans la section **Artifacts**.

## Bon à savoir

- Les données sont enregistrées automatiquement sur le téléphone et restent après fermeture ou redémarrage.
- **Désinstaller l'application supprime toutes les données** (la sauvegarde cloud est volontairement désactivée).
- Les clients sont triés par ordre alphabétique (sans tenir compte des majuscules).
- Un même nom Facebook ne crée jamais de doublon dans un Live : l'application propose d'ajouter le sac à la fiche existante.
- Supprimer le dernier sac d'un client supprime aussi sa fiche.

## Vérifier les calculs

Les calculs (totaux, regroupement, recherche, format « 25 000 Ar ») sont dans
`app/src/main/java/com/livesac/manager/domain/Logic.kt`, testés par `app/src/test/.../LogicTest.kt`.
Pour les lancer : clic droit sur `LogicTest` dans Android Studio > **Run**.
