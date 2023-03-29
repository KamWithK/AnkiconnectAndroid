# Actions

All actions here work exactly the same as [Anki-Connect](https://github.com/FooSoft/anki-connect#supported-actions) unless specified otherwise.

All calls support the version `<= 4` and `> 4` format.
Versions `<= 4` returns the result on success, but versions `> 4` returns a json
of the following format on success:
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


## Deck Actions

### `deckNames`

### `deckNamesAndIds`

### `deckNamesAndIds`

---

## Model Actions

### `modelNames`

### `modelNamesAndIds`

### `modelFieldNames`

---

## Note Actions

### `findNotes`
- This usually takes longer to run, compared to the PC version of Anki-Connect.
- TODO query with spaces will fail, must use rust backend

### `guiBrowse`
- Only works with Anki versions (TODO)

### `canAddNotes`
- TODO only searches for first field
- TODO query with spaces will fail, must use rust backend

### `addNote`

---

## Media Actions

### `storeMediaFile`

---

## Miscellaneous Actions

### `multi`
