# Actions

All actions here work exactly the same as [Anki-Connect](https://git.sr.ht/~foosoft/anki-connect#supported-actions) unless specified otherwise.

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
* See: [Anki-Connect `deckNames`](https://git.sr.ht/~foosoft/anki-connect#codedecknamescode)
* Used by Yomichan

### `deckNamesAndIds`
* See: [Anki-Connect `deckNamesAndIds`](https://git.sr.ht/~foosoft/anki-connect#codedecknamesandidscode)
* Used by Yomichan

<br>

## Model Actions

### `modelNames`
* See: [Anki-Connect `modelNames`](https://git.sr.ht/~foosoft/anki-connect#codemodelnamescode)
* Used by Yomichan

### `modelNamesAndIds`
* See: [Anki-Connect `modelnamesandids`](https://git.sr.ht/~foosoft/anki-connect#codemodelnamesandidscode)
* Used by Yomichan

### `modelFieldNames`
* See: [Anki-Connect `modelFieldNames`](https://git.sr.ht/~foosoft/anki-connect#codemodelfieldnamescode)
* Used by Yomichan

<br>

## Note Actions

### `findNotes`
* See: [Anki-Connect `findNotes`](https://git.sr.ht/~foosoft/anki-connect#codefindnotescode)
* Attempting to escape a query with spaces using quotes will not work, unless
    AnkiDroid is using the new (Rust) backend.
    For example, the following query will not work: `"Note:My Mining Note"`
* Expect this to take longer to run compared to the PC version of Anki-Connect.
* Used by Yomichan

### `guiBrowse`
* See: [Anki-Connect `guiBrowse`](https://git.sr.ht/~foosoft/anki-connect#codeguibrowsecode)
* This call only works with AnkiDroid versions past [October 8, 2022](https://github.com/ankidroid/Anki-Android/pull/11899) (2.16alpha88 and above)
* Used by Yomichan

### `canAddNotes`
* See: [Anki-Connect `canAddNotes`](https://git.sr.ht/~foosoft/anki-connect#codecanaddnotescode)
* Internally, this behaves entirely different from Anki-Connect. Anki-Connect literally attempts to add
    a note (without saving the collection) in order to determine whether the note can be added or not.
    AnkiConnect Android instead queries the collection with the first field, and sees if any other cards
    exist.
    For example, if your first field was `Word`, `canAddNotes` determines whether a note can be added
    or not by seeing if the query `Word:(WORD FIELD CONTENTS)` finds any matches.
    Finding any match will return `false`, and no matches will return `true`.
* Because this internally queries the database, this action suffers from the same problems as `findNotes`.
    To guarantee the correctness of the query, you must use the new backend.
* If all notes have the same model, then the call is optimized as we can call an internal Ankidroid API
    function on the entire set of data.
* Used by Yomichan

### `canAddNotesWithErrorDetail`
* See: [Anki-Connect `canAddNotesWithErrorDetail](https://git.sr.ht/~foosoft/anki-connect#codecanaddnoteswitherrordetailcode)
* Currently, if a card fails `canAddNotes`, the error message will always say it's due to it being a duplicate, even if there was a different reason for the failure (For example, in Anki-Connect a card could fail the `canAddNotes` check if the first field was empty).
* Used by Yomitan

### `notesInfo`
* See: [Anki-Connect `notesInfo`](https://git.sr.ht/~foosoft/anki-connect#codenotesinfocode)
* Used by Yomitan

### `addNote`
* See: [Anki-Connect `addNote`](https://git.sr.ht/~foosoft/anki-connect#codeaddnotecode)
* Used by Yomichan
* Anki-Connect desktop allows using various formats for the media file, but this api currently only
  supports using the `url` and `data` field. Does not support `skipHash` for the `url` field.
  All of `picture`, `audio` and `video` are supported.

### `updateNoteFields`
* See: [Anki-Connect `updateNoteFields`](https://git.sr.ht/~foosoft/anki-connect#codeupdatenotefieldscode)
* See [addNote](#addnote) for supported media actions.

<br>

## Media Actions

### `storeMediaFile`
* See: [Anki-Connect `storeMediaFile`](https://git.sr.ht/~foosoft/anki-connect#codestoremediafilecode)
* Filenames will get a random number appended to the end of them, i.e. `file.png` becomes `file_123456789.png`
* Used by Yomichan

<br>

## Miscellaneous Actions

### `multi`
* See: [Anki-Connect `multi`](https://git.sr.ht/~foosoft/anki-connect#codemulticode)
* Used by Yomichan
