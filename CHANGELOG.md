# Changelog

All notable changes to this project will be documented in this file.  The
format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

## [1.1] (Android versionCode 2) - 2019-01-27
#### Added
- Add solitaire mode
  ([Issue #19](https://github.com/kuhrusty/Micropulease/issues/19)).
- Add the ability to swap renderers during play
  ([Issue #9](https://github.com/kuhrusty/Micropulease/issues/9)).  As
  part of this, add a Settings activity.
- Add the ability to pan & scale the board view.  This is not perfect
  (see [Issue #8](https://github.com/kuhrusty/Micropulease/issues/8)),
  but is not bad.
- Add sound
  ([Issue #17](https://github.com/kuhrusty/Micropulease/issues/17)).
- Make bot sleep time configurable
  ([Issue #5](https://github.com/kuhrusty/Micropulease/issues/5)).
- Add Help activity.

#### Changed
- Fix the roughness at the end of the game
  ([Issue #15](https://github.com/kuhrusty/Micropulease/issues/15)).
- Fix the ability to cancel the change-player dialog
  ([Issue #18](https://github.com/kuhrusty/Micropulease/issues/18)).
- Clear the selected tile/stone when drawing a new tile
  ([Issue #16](https://github.com/kuhrusty/Micropulease/issues/16)).
- At startup, attempt to restore the last selected player type.
- In PlayGameActivity, make the Cancel button an ImageButton.
- Update to Android Studio 3.3/Gradle 4.10.  No idea whether this breaks
  anything.
