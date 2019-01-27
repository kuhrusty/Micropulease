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
