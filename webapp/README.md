# WEBAPP module

## Compile

## build and run app (open chrome)

`sbt>` `webapp / Compile / fastOptJS / webpack`

open `file:///home/fabio/workspace/ScalaDID/webapp/index-fastopt.html#/`

google-chrome-stable --disable-web-security --user-data-dir="/tmp/chrome_tmp" --new-window file:///home/fabio/workspace/ScalaDID/webapp/index-fastopt.html#/

When developing in a recompile-test iteration you can sbt to monitor source files and re-run.
```sbt
~ webapp / Compile / fastOptJS / webpack
```

## TODO LIST:

- Update to the [material-web version 3](https://github.com/material-components/material-web#readme).
