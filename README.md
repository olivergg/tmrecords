# tmrecords

A [re-frame](https://github.com/Day8/re-frame) single page application designed to store TrackMania records in real-time on a Firebase database.

Alpha preview is available here : https://tmrecords-fa4b2.firebaseapp.com/

## Todo

- Google authentication to login and to implement some role based functionnalities
- Submit a score from the application (input form at first, use image upload later for proof)
- Add gbx links to the tracks
- Further CSS polishing
- About page should have the same header and footer as the home page

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

