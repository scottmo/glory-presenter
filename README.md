# Glory Presenter

## Introduction

Glory presenter is an app that's solely focused on the power point experience.
Modern options such as ProPresenter, OpenLP, Quelea, etc. are all very good options if a church has a dedicated
department or staffs to maintain and operate. Unfortunately, this is not always feasible for small churches and
churches that don't have the necessary equipments and talents. For example, in my church, preachers always bring
their own power point slides for the sermons. The slides are usually in pptx format with different styles and themes.
Modern worship software usually have the option to load external power point, but they are all pretty slow and
non-responsive, especially when there are videos or audios embedded in the slides. The staffs in my church are also
limited in numbers. I'm pretty much the only one handling the whole worship presentation. When I'm sick or out, it's
important to get someone that's untrained to help out right away. Using modern worship software becomes a
difficult option as they all require training to use, whereas most people have experience operating a power
point deck.

## Caveats

As I'm developing this on my spare time, I haven't really got a chance to fully test everything. You can see that I have
not really written any tests for it. All I can say is that so far it's been working well for my church. Do note that
there is limited error handling, so you may see random errors happening and not sure what's going on. I'll try to patch
them as I continue to improve it, but support will be limited.

## Technology

- Java 17
- JavaFX 17
- Sqlite

## Features

- UI for adding/editing songs
- PPTX generation from bible verses or songs
- support importing songs in Open Lyrics format, see <https://docs.openlyrics.org/>
- support importing bible in OSIS format, see <https://crosswire.org/osis/>

## Sample App Config

## Testing

`mvn com.github.eirslett:frontend-maven-plugin:npm@run-frontend`

### Directory

- glory-presenter
  - glory-presenter.jar
  - config.json
  - output
  - data
    - song.db
    - bible.db
    - templates
      - template-bible.pptx
      - template-song.pptx

### config.json

```json
{
  "dataDir": "./data",
  "outputDir": "./output",
  "locales": [
    "zh_cn",
    "en_us"
  ],
  "bibleVersionToLocale": {
    "cuv": "zh_cn",
    "niv": "en_us"
  }
}
```

## PPTX Generation

Generation is  based off of an existing pptx template. Placeholder texts in the pptx template will be replaced with
the actual values fromm the local database. See samples folder for examples.

### Placeholders

- bible slides - e.g. genesis 1:2, assuming $version is asv
  - {verse.chapter} - 1
  - {verse.number} - 2
  - {verses} - 1:2
  - {verse.$version} - In the beginning God created the heavens and the earth.
  - {book.$version} - Genesis
- song slildes
  - {songbook}
  - {entry}
  - {copyright}
  - {publisher}
  - {verse.$locale}
  - {title.$locale}
