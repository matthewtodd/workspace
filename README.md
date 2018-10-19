# Perquackey

## Getting Started

* Install [Homebrew](https://brew.sh/).
* `brew update`
* `brew tap`
* `brew bundle --file=tools/Brewfile`
* Configure IntelliJ to [use](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#settings-repository) [this settings repository](https://github.com/matthewtodd/intellij-idea-settings).
* https://ij.bazel.build/docs/bazel-plugin.html#installing-the-plugin
* IntelliJ -> Import Bazel Project
  Project view: tools/intellij/perquackey.bazelproject

## JDK Updates

When running `brew bundle`, you may get a new version of the JDK
that pulls the rug out from under IntelliJ.
To tell IntelliJ where the new JDK is, go to
File -> Project Structure... and add the new SDK.

## IntelliJ Updates

It can take a little while for the Bazel plugin to be updated
to handle new versions of IntelliJ.
Before updating IntelliJ, check to see that the
[Bazel plugin](https://plugins.jetbrains.com/plugin/8609-bazel) can
[handle it](https://www.jetbrains.com/idea/download/#section=mac).

Adding to the complication, `brew cask` ignores IntelliJ updates by default,
since it's marked as `auto_updates true`.
To check for new IntelliJ versions, run
`brew cask outdated --greedy`,
and to install new IntelliJ versions
(once you've confirmed the latest Bazel plugin is compatible) run
`brew cask install intellij-idea-ce`.
`brew bundle` could support this `--greedy` flag,
but it doesn't seem to thread it all the way through;
see further notes in `tools/Brewfile`.
