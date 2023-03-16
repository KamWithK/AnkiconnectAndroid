# Ankiconnect Android

Ankiconnect Android allows you to utilize the standard Anki mining workflow on Android devices like phones and eReaders.
Create Anki cards using [Yomichan](https://foosoft.net/projects/yomichan/) on [Kiwi Browser](https://kiwibrowser.com/) and add them straight into your Anki deck!
Mine on the go in the same way as you mine on your desktop pc.
Forvo and local audio are now supported!


Ankiconnect Android is a from scratch unofficial reimplementation of the [desktop Ankiconnect extension](https://github.com/FooSoft/anki-connect), [desktop Forvo Server extension](https://github.com/jamesnicolas/yomichan-forvo-server) and [desktop Local Audio Server for Yomichan](https://github.com/themoeway/local-audio-yomichan).
It reimplements the core APIs used by Yomichan to work with [Ankidroid](https://github.com/ankidroid/Anki-Android/).

## Table of Contents
* [Instructions](#Instructions)
    * [Additional Instructions: Forvo Audio](#additional-instructions-forvo-audio)
    * [Additional Instructions: Show Card Button](#additional-instructions-show-card-button)
    * [Additional Instructions: Local Audio](#additional-instructions-local-audio)
* [Common Errors and Solutions](#common-errors-and-solutions)
    * [First Steps](#first-steps)
    * [Problem: The Yomichan popup appears upon scrolling](#problem-the-yomichan-popup-appears-upon-scrolling)
    * [Problem: The add button is always greyed out](#problem-the-add-button-is-always-greyed-out)
    * [Problem: The add card button does not appear](#problem-the-add-card-button-does-not-appear)
    * [Problem: Duplicate checks aren't working](#problem-duplicate-checks-arent-working)
    * [Problem: Forvo audio won't load](#problem-forvo-audio-wont-load)
    * [Problem: On card add, I get `Incorrect flds argument`](#problem-on-card-add-i-get-incorrect-flds-argument)
    * [I still have a problem](#i-still-have-a-problem)
* [Limitations](#limitations)

## Instructions
Here's how to set everything up from scratch (if you've already got Yomichan working, then skip to step 5):

1. Install [Kiwi Browser](https://play.google.com/store/apps/details?id=com.kiwibrowser.browser) and [Ankidroid](https://play.google.com/store/apps/details?id=com.ichi2.anki)
2. Install Ankiconnect Android - Download from the [Releases Section](https://github.com/KamWithK/AnkiconnectAndroid/releases/latest) or from [IzzyOnDroid repo](https://apt.izzysoft.de/fdroid/index/apk/com.kamwithk.ankiconnectandroid)
3. Start the Ankiconnect Android app, accept the permissions and hit start service
4. Set up the [Yomichan extension](https://chrome.google.com/webstore/detail/yomichan/ogmnaimimemjmbakcfefmnahgdfhfami) in Kiwi Browser to your liking:
    * `Import` 1+ dictionaries by clicking `Configure installed and enabled dictionaries` and then `Import` under `Dictionaries` section ([external resources](https://learnjapanese.moe/resources/#dictionaries))
    * **`Scan modifier key` under the `Scanning` section should be "No Key" (unless using mouse/keyboard, advanced options contains more config options)**
    * **Ensure `Anki` → `Show Card Tags` is set to "Never"**
5. **Optimize Yomichan for mobile usage:**
    * `Scan delay` under the `Scanning` section can feel laggy and so can be set to `0`
    * It is recommended to lower the value of `Maximum number of results` (under `General`) to prevent unnecessary lag. A sane value would be `8`
    * Ensure `Scanning Inputs` is optimized for mobile (prevents lookups on scrolling):
        * Ensure advanced settings is enabled (button at the bottom right corner)
        * Navigate to `Scanning` → `Configure advanced scanning inputs`
            * Ensure that advanced options within the `Scanning Inputs` window is enabled.
                To do this, scroll to the right, and tap on the three dots.
            *   <details> <summary>Match your settings to the this image: <i>(click here)</i></summary>
                <a href="https://github.com/KamWithK/AnkiconnectAndroid/blob/master/img/scanning_inputs.jpg"><img src="https://github.com/KamWithK/AnkiconnectAndroid/blob/master/img/scanning_inputs.jpg" width="400" /></a>
                </details>
        * Navigate to `Scanning` → `Support inputs for devices with touch screens`
            * Ensure that `Touch inputs` is checked, and `Pointer inputs` is NOT checked.
6. Set up Yomichan for sentence mining:
    * Toggle `Enable Anki integration` on, under `Anki`
    * Click on `Configure Anki card format` and choose the deck, model and field/values you desire
    * For further customisation you can write/modify the script under `Configure Anki card templates`
***Sections and Advanced options visible via the blue menu icon on the bottom right of the screen***

> Easier Yomichan setup: If you import settings from a computer, ensure that the bolded steps are still followed

### Additional Instructions: Forvo Audio
The default audio sources from Yomichan should already work.
However, Forvo can be added as an audio source.
It is recommended that you add this Forvo audio source
because Forvo significantly extends the coverage of audio
compared to the default audio sources from Yomichan.

1. Click on `Configure audio playback sources` and under the `Audio` section
2. Click the `Add` button (top right corner)
3. **Select `Custom URL (JSON)` and copy paste `http://localhost:8765/?term={term}&reading={reading}` into the `URL` box** (NOTE: This is NOT the same URL as from your PC, the port is different)



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

> **Warning**:
> Make sure you save your note changes if you edit your note! If you do not
> save your changes and re-click on the "show card" button from Kiwi Browser, you will
> lose all your current note changes!

### Additional Instructions: Local Audio
The [(desktop) local audio server](https://github.com/themoeway/local-audio-yomichan)
setup for Yomichan has been ported over to Ankiconnect Android, and can be used similarly.
Again, this is *a completely optional* setup that does not need to be done.

General information about the setup, including reasons for and against using the setup,
can be found within the above link.

> **Warning**:
> This setup takes up about 5gb of space on your Android device! Ensure you have enough space before setting this up.

1. Ensure you have set up the latest version of the [desktop local audio server](https://github.com/themoeway/local-audio-yomichan) setup.
    If you already have the add-on installed, check for updates by navigating to `Tools` → `Add-ons` → (select "Local Audio Server for Yomichan") → `Check for Updates`.

2. Generate the Android database.
    To do this, navigate to (Anki) → `Tools` (top left corner) → `Local Audio Server` → `Generate Android database`.

    Expect this to take a while (potentially **over 30 minutes!**). You will **not be able to use Anki** during this time.

    This database stores all the audio files into one large file, in order to make file transfer to Android much faster (transferring the folder took about 24 hours, while transferring the large file took less than 3 minutes).

3. Copy the files from desktop to Android.
    * Locate the add-on folder on desktop.
        To do this, navigate to `Tools` → `Add-ons` → (select "Local Audio Server for Yomichan") → `View Files`.
        When you are here, navigate to `user_files`.

    * Locate AnkiConnect Android's data folder. By default, it is under:
        ```
        (phone)/Android/data/com.kamwithk.ankiconnectandroid/files/
        ```
        However, one can verify the location of the folder by going into the settings
        (gear at the top right corner), and tapping on `Print Local Audio Directory`.
        The following output specifies that the folder is indeed in the default position:
        ```
        /storage/emulated/0/Android/data/com.kamwithk.ankiconnectandroid/files/
        ```

    * After locating the two folders, copy `android.db` from the desktop's add-on folder
        into Ankiconnect Android's data folder.
        Do NOT copy the entire `user_files` folder.

        This should result in the following:
        ```
        /storage/emulated/0/Android/data/com.kamwithk.ankiconnectandroid/files/android.db
        ```

4. Setup local audio on Kiwi Browser's Yomichan. (Warning: this URL is different than the one on desktop!)
    * Click on `Configure audio playback sources` and under the `Audio` section
    * Click the `Add` button (top right corner)
    * Select `Custom URL (JSON)` and copy paste the following into the `url` box (tap the code box, and then tap the button to the right to copy the text to the clipboard):
        ```
        http://localhost:8765/localaudio/get/?sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
        ```

        The `sources` and `user` parameters should behave exactly like the desktop local audio plugin.

5. Ensure it works.
    * You can do the
        [exact same check](https://github.com/themoeway/local-audio-yomichan#steps)
        as the desktop local audio server (the last step),
        by scanning 読む and checking that all sources appear.
        Be sure to play all of the sources to ensure that the audio is properly fetched.


## Common Errors and Solutions

### First Steps
If you are having issues with anything, such as Yomichan being unable to connect to AnkiDroid, please ensure all these steps are followed before continuing:

* Make sure the latest [app release](https://github.com/KamWithK/AnkiconnectAndroid/releases/latest) is installed.
* If you imported the settings from the PC, try to use the sanitized version upon import, and manually re-add the handlebars after.
* Double check that your Yomichan settings are correct. In particular, check that the `Configure Anki card format...` section, and the audio sources section is correct.
    * On rare occasions, settings exported from the computer and imported into your Android device may not work. Instead, try to reset Yomichan's settings and redo everything from scratch.
* Battery saving/automatic optimisation is turned off for Ankidroid, Ankiconnect Android and optionally (but recommended) Kiwi browser.
* You allowed Ankiconnect Android to be running in the background (if this option is available on your device).


### Problem: The Yomichan popup appears upon scrolling
Try going through step 5 of the [instructions](#instructions).
In particular, see the step that says
**Ensure `Scanning Inputs` is optimized for mobile (prevents lookups on scrolling)**.


### Problem: The add button is always greyed out
This usually happens if `Enable Content Scanning` is switched off.
To fix this, simply switch it on (under `Yomichan Settings` → `General` → `Enable content scanning`).

> **Note**:
> If you have this switched off in the first place, it is also very likely that the popup is
> showing up at unwanted times, i.e. while scrolling through a page.
> To solve this, try going through step 5 of the [instructions](#instructions).


### Problem: The add card button does not appear
- Check that the `Enable Anki integration` setting in Yomichan is indeed enabled, and properly connected.
- Under `Anki` → `Configure Anki card format`, ensure that the Deck and Model at the top right corner
    are not highlighted in red. If they are, please select the correct deck and/or model.
- Under `Anki` → `Show card tags`, make sure this is set to `Never`.


### Problem: Duplicate checks aren't working
To determine that duplicate checks aren't working:
- Enable duplicate checks in the Yomichan settings (under `Anki` → `Check for card duplicates`),
- Select a word and add a card
- Tap outside of the popup, and re-select the word. Normally, you should not be able to add a card here.

If you are able to add a card (i.e. you see the plus button), then duplicate checks are indeed not working.
Check that your first field name does not include spaces,
and your first field contents do not include quotes (`"`) or spaces.
If either of those are true, the only way to solve it is by using the Alpha version of AnkiDroid, and
[enable the new backend](https://github.com/ankidroid/Anki-Android/issues/13399)
under the advanced settings.


### Problem: Forvo audio won't load
Please make sure that this exact URL under
[Additional Instructions: Forvo Audio](#additional-instructions-forvo-audio) is used.
This URL is different from the one on PC.

> **Note**:
> There is always a chance that Forvo has changed the layout of their website,
> which can lead to AnkiConnect Android improperly fetching the audio.
> If you suspect this is the case, please create an issue on Github.


### Problem: On card add, I get `Incorrect flds argument`
This happens when you change the fields of a card. For example, if you added a field,
renamed a field, or deleted a field, then this error may pop up.
To fix it, navigate to `Yomichan Settings` → `Anki` →  `Configure Anki card format...`,
and update the model fields (i.e. by switching it to a different model and back).


### I still have a problem
If you've gone through the instructions and are still having trouble, feel free to create an issue here on GitHub or @/dm me on Discord (`@KamWithK#0634` on [TheMoeWay](https://learnjapanese.moe/join/)).



## Limitations
Because Ankiconnect Android is a small project with a limited scope, not all API queries/cases are implemented/considered.
Currently every known essential feature has been added into the app, however some niche edge cases have been ignored.

Some examples:
1. Duplicate checks always occur across the full Anki collection instead of whatever deck is selected (no matter what options are selected, assuming this feature is left enabled)
2. The show card button will not work on the latest stable release of AnkiDroid. Instead, you must **manually install a pre-release version of AnkiDroid** for it to work. Please see [these instructions](#additional-instructions-show-card-button) for more details on how to make the show card button work.
3. When viewing the note, the note cannot be viewed directly within card editor. Instead, the note is shown from the card search screen.
4. You are unable to view the note tags on duplicate notes.
