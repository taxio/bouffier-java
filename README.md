# Bouffier Java
This convert Java source to AST representation.

AST file support the follow formats.
- yaml
- xml

## Usage
`BOUFFIER_JAVA_PROJECT_PATH`: This contains Java sources and output AST files  (default is `/bouffier-java`)
This directory should have the following composition.

```bash
projects/
├── out
│   └── {{AST file is output here}}
└── source
    └── {{There is Java sources}}
```

So, you need to create a directory in this format and mount it.

`BOUFFIER_JAVA_FORMAT`: the format of output AST file. (default is `yaml`)

### Example
```bash
$ docker run -e BOUFFIER_JAVA_FORMAT=xml -v {{your dir path}}:/bouffier-java taxio/bouffier-java
```