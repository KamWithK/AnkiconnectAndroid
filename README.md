# Ankiconnect Android

Ankiconnect Android allows you to utilize the standard Anki mining workflow on Android devices like phones and eReaders.
Create Anki cards using [Yomichan](https://foosoft.net/projects/yomichan/) on [Kiwi Browser](https://kiwibrowser.com/) and add them straight into your Anki deck!
Mine on the go in the same way as you mine on your desktop pc.
Forvo audio is now supported!


Ankiconnect Android is a from scratch unofficial reimplementation of the [desktop Ankiconnect extension](https://github.com/FooSoft/anki-connect) and [desktop Forvo Server extension](https://github.com/jamesnicolas/yomichan-forvo-server).
It reimplements the core APIs used by Yomichan to work with [Ankidroid](https://github.com/ankidroid/Anki-Android/).

## Instructions
Here's how to set everything up from scratch (if you've already got Yomichan working, then skip to step 5):

1. Install [Kiwi Browser](https://play.google.com/store/apps/details?id=com.kiwibrowser.browser) and [Ankidroid](https://play.google.com/store/apps/details?id=com.ichi2.anki)
2. Install Ankiconnect Android - Download from the [Releases Section](https://github.com/KamWithK/AnkiconnectAndroid/releases/latest) or from [IzzyOnDroid repo](https://apt.izzysoft.de/fdroid/index/apk/com.kamwithk.ankiconnectandroid)
3. Start the Ankiconnect Android app, accept the permissions and hit start service
4. Set up the [Yomichan extension](https://chrome.google.com/webstore/detail/yomichan/ogmnaimimemjmbakcfefmnahgdfhfami) in Kiwi Browser to your liking:

    * `Import` 1+ dictionaries by clicking `Configure installed and enabled dictionaries` and then `Import` under `Dictionaries` section ([external resources](https://learnjapanese.moe/resources/#dictionaries))
    * **`Scan modifier key` under the `Scanning` section should be "No Key" (unless using mouse/keyboard, advanced options contains more config options)**
    * `Scan delay` under the `Scanning` section can feel laggy and so can be set to `0`
    * It is recommended to lower the value of `Maximum number of results` (under `General`) to prevent unnecessary lag. A sane value would be `8`

5. Set up Yomichan for sentence mining:
    * Toggle `Enable Anki integration` on, under `Anki`
    * Click on `Configure Anki card format` and choose the deck, model and field/values you desire
    * For further custamisation you can write/modify the script under `Configure Anki card templates`
6. Set up Forvo audio
    * Click on `Configure audio playback sources` and under the `Audio` section
    * Click the `Add` button (top right corner)
    * **Select `Custom URL (JSON)` and copy paste `http://localhost:8765/?term={term}&reading={reading}` into the `URL` box** (NOTE: This is NOT the same URL as from your PC, the port is different)

***Sections and Advanced options visible via the blue menu icon on the bottom right of the screen***

> Easier Yomichan setup: If you import settings from a computer, ensure that the bolded steps are still followed

### Additional Instructions: Show Card Button
By default, the show card button **will not work**.
The following is a *completely optional* set of instructions for getting the show card button to work:

1. Install a **pre-release (alpha) version** of AnkiDroid from their [releases page](https://github.com/ankidroid/Anki-Android/releases). This cannot be a parallel build.
    * As of writing this (2023/01/03), the stable release of AnkiDroid
        (i.e. the released version on Google Play) does not support the feature of
        [opening the card browser window](https://github.com/ankidroid/Anki-Android/pull/11899)
        from another app. Therefore, the first step is to manually download and install
        the most recent alpha version of AnkiDroid.
    * This carries all the risks of using a pre-release version! Only download it
        if you know what you are doing.

2. After installing AnkiDroid, you must allow Ankiconnect Android to open apps in the background.
    * Under Ankiconnect Android, tap on the settings gear at the top right corner.
    * Tap on the `Access Overlay Permissions` option. This should lead you to Android's settings page.
    * Activate the switch for `Ankiconnect Android` within this settings page.

From here, you should be able to use the show card button as normal.

> **Warning**
>
> Make sure you save your note changes if you edit your note! If you do not
> save your changes and re-click on the "show card" button from Kiwi Browser, you will
> lose all your current note changes!

### Additional Instructions: Setting up Local Audio

local audio desktop vs android:
```
 *   - initial get:
 *     python:  http://localhost:5050/?sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
 *     android: http://localhost:8765/localaudio/?type=getSources&sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
 *   - audio file get:
 *     python:  http://localhost:5050/SOURCE/FILE_PATH_TO_AUDIO_FILE
 *     android: http://localhost:8765/localaudio/?type=SOURCE&path=FILE_PATH_TO_AUDIO_FILE
```


local audio folder (user can verify by settings -> `Print Local Audio Directory`
```
(phone)/Android/data/com.kamwithk.ankiconnectandroid/files/
/storage/emulated/0/Android/data/com.kamwithk.ankiconnectandroid/files/
```

- when moving audio files: might want to spread this out over a few nights (android's file system is very slow and can easily take many hours to transfer files)



expected file directory (`user_files` be the exact same as original):
```
(local audio folder)
├── entries.db
└── user_files
    ├── forvo_files
    │   └── ...
    ├── jpod_alternate_files
    │   └── ...
    ├── jpod_files
    │   └── ...
    └── nhk16_files
        └── ...
```


developer notes:
- when building, must populate the jniLibs folder in order for sqlite3 to work
- to do this:

1. download the release specified under gradle (currently: https://github.com/xerial/sqlite-jdbc/releases/tag/3.40.1.0)
2. unzip
3. follow instructions [here](https://github.com/xerial/sqlite-jdbc/blob/master/USAGE.md#how-to-use-with-android)
    - `jniLibs` folder is exactly under `app/src/main/jniLibs`. It must be created.
    - don't forget to rename `aarch64` -> `arm64-v8a` and `arm` to `armeabi`!


fetching audio example
```
http://localhost:8765/localaudio/forvo/FILE_PATH
```


## Common Errors and Solutions
If you're experiencing any of these problems:
1. Forvo audio won't load
2. App crashes
3. Yomichan cannot connect

Check that:
* Latest [app release](https://github.com/KamWithK/AnkiconnectAndroid/releases/latest) is installed
* `http://localhost:8765/?term={term}&reading={reading}` is added as `Custom URL (JSON)` as a Yomichan audio source
* If you import Yomichan's settings that the URLs default or as-specified and card/deck options right (when in doubt import the sanitised version in-case)
* Battery saving/automatic optimisation is turned off for Ankidroid, Ankiconnect Android and optionally (but recommended) Kiwi browser
* You allowed Ankiconnect Android to be running in the background (if this option is available on your device)

If you've gone through the instructions and are still having trouble, feel free to create an issue here on GitHub or @/dm me on Discord.

## Limitations
Because Ankiconnect Android is a small project with a limited scope, not all API queries/cases are implemented/considered.
Currently every known essential feature has been added into the app, however some niche edge cases have been ignored.

Some examples:
1. Duplicate checks always occur across the full Anki collection instead of whatever deck is selected (no matter what options are selected, assuming this feature is left enabled)
2. The show card button will not work on the latest stable release of AnkiDroid. Instead, you must **manually install a pre-release version of AnkiDroid** for it to work. Please see [these instructions](#additional-instructions-show-card-button) for more details on how to make the show card button work.
3. When viewing the note, the note cannot be viewed directly within card editor. Instead, the note is shown from the card search screen.
