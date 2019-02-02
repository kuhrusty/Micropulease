## Interesting stuff in the code

- Using gestures to pan and scale a view (`BoardView`), and handling
  taps on the screen when no gesture is detected
  (`PlayGameActivity.boardTouchListener`)
- Running the bot on its own thread, and then joining back up to the UI
  thread when the bot makes its move (`PlayGameActivity.notifyBot()`)
- Loading an image from resources, then splitting it up into separate
  pieces (` BaseImageRenderer.imageToTiles()`,
  `StickMudRenderer2.quarter()` and `explodeNines()`)
- Loading audio files from names generated at runtime
  (`PlayGameActivity.getSoundID()`)
- Some inter-activity communication, where
  `StartGameActivity.doStartGame()` listens for the result from
  `PlayGameActivity`; if `PlayGameActivity` sent back the ending scores,
  then `StartGameActivity.onActivityResult()` sees if the scores were
  good enough to set a new record, and launches `LeaderboardActivty` if
  so.

## Unit test stuff

- Dropping in a no-op `Log` in non-instrumented tests, copied from
  [here](https://stackoverflow.com/questions/36787449/how-to-mock-method-e-in-log)
- Mocking a Context which gives back files from the filesystem when
  `openFileInput()`/`openFileOutput()` are called (`TestUtil`)
