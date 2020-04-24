# tmrecords

A [re-frame](https://github.com/Day8/re-frame) single page application designed to store TrackMania records in real-time on a Firebase database.

Alpha preview is available here : https://tmrecords-fa4b2.firebaseapp.com/

## Todo

- ~~Google authentication to login and to implement some role based functionnalities~~

- ~~Submit a score from the application (with an input form at first, then use image upload later for proof or even the replay.gbx with some kind of client side parsing)~~

- ~~Add gbx links to the tracks~~ (plumbing is done, still need to populate the database now)

- ~~Further CSS polishing~~ (mark as done even if there is always room for improvemet)

- ~~The about page should have the same header and footer as the home page~~

- add a customized view for a connected user (with user centric statistics, graphs ?)

- tracks gallery (with image preview ? animated gif ?)

- allow the players to vote for their favorite tracks

- improve olympic ranking subscriptions. Do not return functions as subscriptions output. do the heavy computation in the subscription, not in the view.

## Development Mode


### Run application:

```
shadow-cljs watch app
```

## Production Build


To compile Clojurescript to Javascript:

```
shadow-cljs release app
```

To deploy to firebase (remember to change the API credentials to use your own):

```
firebase deploy
```

