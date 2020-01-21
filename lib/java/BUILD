# vim: ft=ruby

java_sdk = if RUBY_PLATFORM =~ /darwin/i
             {
               url: 'https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_osx-x64_bin.tar.gz',
               sha256: '08fd2db3a3ab6fb82bb9091a035f9ffe8ae56c31725f4e17d573e48c39ca10dd',
               strip_components: 3,
               includes: ['**/Home/**/*'],
             }
           else
             {
               url: 'https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz',
               sha256: 'acc7a6aabced44e62ec3b83e3b5959df2b1aa6b3d610d58ee45f0c21a7821a71',
               strip_components: 1,
               includes: ['**/*'],
             }
           end

http_archive(
  name: 'sdk',
  **java_sdk,
  build_file: '',
)
