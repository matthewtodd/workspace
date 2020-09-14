# vim: ft=ruby

http_file(
  name: "dist",
  url: "https://download.java.net/java/GA/jdk14.0.2/205943a0976c4ed48cb16f1043c5c647/12/GPL/openjdk-14.0.2_osx-x64_bin.tar.gz",
  sha256: "386a96eeef63bf94b450809d69ceaa1c9e32a97230e0a120c1b41786b743ae84",
)

extract(
  name: "bin_jar",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/jar"],
  permissions: 0755,
  strip_components: 3,
)

extract(
  name: "bin_java",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/java"],
  permissions: 0755,
  strip_components: 3,
)

extract(
  name: "bin_javac",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/javac"],
  permissions: 0755,
  strip_components: 3,
)

extract(
  name: "bin_jdb",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/jdb"],
  permissions: 0755,
  strip_components: 3,
)

extract(
  name: "lib",
  archive: ":dist",
  format: :tar_gz,
  paths: [
    "lib/classlist",
    "lib/ct.sym",
    "lib/fontconfig.bfc",
    "lib/fontconfig.properties.src",
    "lib/jfr/default.jfc",
    "lib/jfr/profile.jfc",
    "lib/jrt-fs.jar",
    "lib/jspawnhelper",
    "lib/jvm.cfg",
    "lib/libattach.dylib",
    "lib/libawt.dylib",
    "lib/libawt_lwawt.dylib",
    "lib/libdt_socket.dylib",
    "lib/libextnet.dylib",
    "lib/libfontmanager.dylib",
    "lib/libfreetype.dylib",
    "lib/libinstrument.dylib",
    "lib/libj2gss.dylib",
    "lib/libj2pcsc.dylib",
    "lib/libj2pkcs11.dylib",
    "lib/libjaas.dylib",
    "lib/libjava.dylib",
    "lib/libjavajpeg.dylib",
    "lib/libjawt.dylib",
    "lib/libjdwp.dylib",
    "lib/libjimage.dylib",
    "lib/libjli.dylib",
    "lib/libjsig.dylib",
    "lib/libjsound.dylib",
    "lib/liblcms.dylib",
    "lib/libmanagement.dylib",
    "lib/libmanagement_agent.dylib",
    "lib/libmanagement_ext.dylib",
    "lib/libmlib_image.dylib",
    "lib/libnet.dylib",
    "lib/libnio.dylib",
    "lib/libosx.dylib",
    "lib/libosxapp.dylib",
    "lib/libosxkrb5.dylib",
    "lib/libosxsecurity.dylib",
    "lib/libosxui.dylib",
    "lib/libprefs.dylib",
    "lib/librmi.dylib",
    "lib/libsaproc.dylib",
    "lib/libsplashscreen.dylib",
    "lib/libsunec.dylib",
    "lib/libverify.dylib",
    "lib/libzip.dylib",
    "lib/modules",
    "lib/psfont.properties.ja",
    "lib/psfontj2d.properties",
    "lib/security/blacklisted.certs",
    "lib/security/cacerts",
    "lib/security/default.policy",
    "lib/security/public_suffix_list.dat",
    "lib/server/classes.jsa",
    "lib/server/libjsig.dylib",
    "lib/server/libjvm.dylib",
    "lib/src.zip",
    "lib/tzdb.dat",
  ],
  strip_components: 3,
)

java_archiver(
  name: "archiver",
  jar: ":bin_jar",
  libs: ":lib",
)

java_compiler(
  name: "compiler",
  javac: ":bin_javac",
  libs: ":lib",
)

java_runtime(
  name: "runtime",
  java: ":bin_java",
  libs: ":lib",
)
