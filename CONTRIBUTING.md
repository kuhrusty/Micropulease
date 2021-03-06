If you're considering building this yourself, this is for you.

There are two main areas which need a lot of work: improving the UI, and
adding bots/AIs which don't totally suck.  See also the issue tracker at
https://github.com/kuhrusty/Micropulease/issues.

### Checking it out

In Android Studio 3.2.1:

1. File -> New -> Project from Version Control -> Git
1. Clone Repository:
   - URL: https://github.com/kuhrusty/Micropulease.git
   - Directory: (whatever you want; probably
     /home/.../AndroidStudioProjects/Micropulease)
1. That _should_ just build; then you can install on your device using
   the green triangle-thing in the toolbar.

### Adding a Bot

1. copy one of the existing bot classes in `com.kuhrusty.micropul.bot`;
   change its name.
1. Add its name and description to `app/src/main/res/values/strings.xml`.
1. Add it to the hard-coded list of bots in `com.kuhrusty.micropul.PlayerType.getPlayerTypes()`.

Build & install the app; confirm that your new bot shows up in the list
of available bots.

Then go back and start coding ...

When it's time for your bot to make its move, `takeTurn()` will be
called with a copy of the current game state.  This will be done on a
non-UI thread, so you don't have to worry about blocking the UI.  When
you're done, call one of the `MoveListener` methods; this will cause
your move to be made and the UI to be updated.

If your bot makes an illegal move, it will concede the game.

Currently there's no timer on a bot which takes too long to make a move.

### Adding a Renderer

1. copy one of the classes in `com.kuhrusty.micropul.renderer`;
   change its name.
1. Add its name to `app/src/main/res/values/strings.xml`.
1. Add it to the list of renderers in
   `com.kuhrusty.micropul.StartGameActivity.getTileRenderers()`.

Build & install the app; confirm that your new renderer shows up in the
list of available renderers.

Then go back and start coding ...

**To add a preview picture,** go into `GameState.initGame()` and
uncomment the bit which says "temporarily uncomment this if you want to
generate a preview."  Then build & run it; it should come up with four
tiles already played.  Capture the screen, crop it to half-a-tile border,
scale it to 400 x 400 (shrug, I just picked that because it seemed nice),
save it in `app/src/main/res/drawable`, and add its resource ID
(`R.drawable.your_file_name` or whatever) to your renderer's
`getPreviewDrawableID()`.

### Adding Sounds

**Micropulease** only uses a few sound files to hint at what a player or
bot is up to.  To add your own set of sounds:

1. In `app/src/main/res/values/strings.xml`, find `sound_set_ids`,
   and add an entry which will be your new sound set's ID.
1. Also add an entry to `sound_set_labels` with a human-readable name
   for your new sound set.
1. Create the following sound files, and put them in
   `app/src/main/res/raw` (where "ID" is the sound set ID you chose).
   If one is missing, that's fine; it will be ignored.
   - `sound_ID_cancel.m4a` - played when someone hits the undo button
     after placing a tile or stone
   - `sound_ID_ok.m4a` - not used
   - `sound_ID_stone_played.m4a` - played when someone places a stone
   - `sound_ID_tile_drawn.m4a` - played when someone draws a tile
   - `sound_ID_tile_played.m4a` - played when someone places a tile
1. Build & deploy the app, confirm that your new sound set shows up in
   Settings; then try it out!
