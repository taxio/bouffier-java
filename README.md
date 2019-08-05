# Bouffier Java
This is a tool for converting Java source to AST representation.

AST file support the follow formats.
- yaml
- xml

## Usage
### `BOUFFIER_JAVA_PROJECT_PATH`
The specified directory contains Java sources and output AST files  (default is `/bouffier-java-project`)

This directory should have the following composition.

```bash
projects/
├── out
│   └── {{AST files will be outputed here}}
└── source
    └── {{Java sources}}
```

So, you need to create a directory in this format and mount it.

### `BOUFFIER_JAVA_FORMAT`
The format of output AST file. (default is `yaml`)

You can choose the following format.

- yaml
- xml

### `BOUFFIER_JAVA_PARSE_MODE`
You can choose the following type.

- file
- method

### Quick Start
Clone this repository and put java sources in `/tests/resources/source`.

Run by docker-compose.

```bash
$ docker-compose -f docker-compose.sample.yml up
```