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
    * It is recommended to lower the value of `Maximum number of results` (under `General`) to prevent unnecessary lag. A sane value would be `8`.

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

If you've gone through the instructions and are still having trouble, feel free to create an issue here on GitHub or @/dm me on Discord.

## Limitations
Because Ankiconnect Android is a small project with a limited scope, not all API queries/cases are implemented/considered.
Currently every known essential feature has been added into the app, however some niche edge cases have been ignored.

The two big examples:
1. Duplicate checks always occur across the full Anki collection instead of whatever deck is selected (no matter what options are selected, assuming this feature is left enabled)
2. Show card button in Yomichan isn't functional and isn't always displayed
