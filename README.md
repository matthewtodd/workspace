# Workspace

## Install development tools

```
brew bundle --file=tools/Brewfile
```

## Configure IntelliJ

1. [Settings Repository...][intellij-settings-repository] → `https://github.com/matthewtodd/intellij-idea-settings` → Overwrite Local
1. Plugins → Search for "[Bazel][intellij-bazel-plugin]" → Install
1. Import Bazel Project → `tools/intellij/perquackey.bazelproject`

[intellij-bazel-plugin]: https://plugins.jetbrains.com/plugin/8609-bazel
[intellij-settings-repository]: https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#settings-repository
