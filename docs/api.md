# Actions

All actions here work exactly the same as [Anki-Connect](https://github.com/FooSoft/anki-connect#supported-actions) unless specified otherwise.

All calls support the version `<= 4` and `> 4` format.
Versions `<= 4` returns the plain result on success (with no JSON object wrapping said result).
However, versions `> 4` returns a JSON of the following format on success:
```
{
    "result": (result),
    "error": null
}
```

On error, all versions will send the following:
```
{
    "result": null,
    "error": (error)
}
```

Do not expect the error message to be the exact same as the PC Anki-Connect error messages.

<br>

## Deck Actions

### `deckNames`
* See: [Anki-Connect `deckNames`](https://github.com/FooSoft/anki-connect#decknames)
* Used by Yomichan

### `deckNamesAndIds`
* See: [Anki-Connect `deckNamesAndIds`](https://github.com/FooSoft/anki-connect#decknamesandids)
* Used by Yomichan

<br>

## Model Actions

### `modelNames`
* See: [Anki-Connect `modelNames`](https://github.com/FooSoft/anki-connect#modelnames)
* Used by Yomichan

### `modelNamesAndIds`
* See: [Anki-Connect `modelnamesandids`](https://github.com/FooSoft/anki-connect#modelnamesandids)
* Used by Yomichan

### `modelFieldNames`
* See: [Anki-Connect `modelFieldNames`](https://github.com/FooSoft/anki-connect#modelfieldnames)
* Used by Yomichan

<br>

## Note Actions

### `findNotes`
* See: [Anki-Connect `findNotes`](https://github.com/FooSoft/anki-connect#findnotes)
* Attempting to escape a query with spaces using quotes will not work, unless
    AnkiDroid is using the new (Rust) backend.
    For example, the following query will not work: `"Note:My Mining Note"`
* Expect this to take longer to run compared to the PC version of Anki-Connect.
* Used by Yomichan

### `guiBrowse`
* See: [Anki-Connect `guiBrowse`](https://github.com/FooSoft/anki-connect#guibrowse)
* This call only works with AnkiDroid versions past [October 8, 2022](https://github.com/ankidroid/Anki-Android/pull/11899) (2.16alpha88 and above)
* Used by Yomichan

### `canAddNotes`
* See: [Anki-Connect `canAddNotes`](https://github.com/FooSoft/anki-connect#canaddnotes)
* Internally, this behaves entirely different from Anki-Connect. Anki-Connect literally attempts to add
    a note (without saving the collection) in order to determine whether the note can be added or not.
    AnkiConnect Android instead queries the collection with the first field, and sees if any other cards
    exist.
    For example, if your first field was `Word`, `canAddNotes` determines whether a note can be added
    or not by seeing if the query `Word:(WORD FIELD CONTENTS)` finds any matches.
    Finding any match will return `false`, and no matches will return `true`.
* Because this internally queries the database, this action suffers from the same problems as `findNotes`.
    To guarantee the correctness of the query, you must use the new backend.
* Used by Yomichan

### `addNote`
* See: [Anki-Connect `addNote`](https://github.com/FooSoft/anki-connect#addnote)
* Used by Yomichan
* Anki-Connect desktop allows using various formats for the media file, but this api currently only
  supports using urls.

### `updateNoteFields`
* See: [Anki-Connect `updateNoteFields`](https://github.com/FooSoft/anki-connect#updatenotefields)
* Does not support `url` or `skipHash` for media

<br>

## Media Actions

### `storeMediaFile`
* See: [Anki-Connect `storeMediaFile`](https://github.com/FooSoft/anki-connect#storemediafile)
* Filenames will get a random number appended to the end of them, i.e. `file.png` becomes `file_123456789.png`
* Used by Yomichan

<br>

## Miscellaneous Actions

### `multi`
* See: [Anki-Connect `multi`](https://github.com/FooSoft/anki-connect#multi)
* Used by Yomichan
