# tmrecords

A [re-frame](https://github.com/Day8/re-frame) single page application designed to store TrackMania records in real-time on a Firebase database.

Alpha preview is available here : https://tmrecords-fa4b2.firebaseapp.com/

## Todo

- Google authentication to login and to implement some role based functionnalities
- Submit a score from the application (with an input form at first, then use image upload later for proof or even the replay.gbx with some kind of client side parsing)
- ~~Add gbx links to the tracks~~ (plumbing is done, still need to populate the database now)
- ~~Further CSS polishing~~ (mark as done even if there is always room for improvemet)
- ~~The about page should have the same header and footer as the home page~~
- add a customized view for a connected user (with user centric statistics)

## Development Mode

### Start Cider from Emacs:

Put this in your Emacs config file:

```
(setq cider-cljs-lein-repl
	"(do (require 'figwheel-sidecar.repl-api)
         (figwheel-sidecar.repl-api/start-figwheel!)
         (figwheel-sidecar.repl-api/cljs-repl))")
```

Navigate to a clojurescript file and start a figwheel REPL with `cider-jack-in-clojurescript` or (`C-c M-J`)

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile Clojurescript to Javascript:

``` :
lein clean
lein cljsbuild once min
```

To deploy to firebase :

```reStructuredText
firebase deploy
```

