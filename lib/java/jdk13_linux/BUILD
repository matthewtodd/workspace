# vim: ft=ruby

http_file(
  name: "dist",
  url: "https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz",
  sha256: "acc7a6aabced44e62ec3b83e3b5959df2b1aa6b3d610d58ee45f0c21a7821a71",
)

extract(
  name: "bin_jar",
  archive: "//lib/java/jdk13_linux:dist",
  format: :tar_gz,
  paths: ["bin/jar"],
  permissions: 0755,
  strip_components: 1,
)

extract(
  name: "bin_java",
  archive: "//lib/java/jdk13_linux:dist",
  format: :tar_gz,
  paths: ["bin/java"],
  permissions: 0755,
  strip_components: 1,
)

extract(
  name: "bin_javac",
  archive: "//lib/java/jdk13_linux:dist",
  format: :tar_gz,
  paths: ["bin/javac"],
  permissions: 0755,
  strip_components: 1,
)

extract(
  name: "lib",
  archive: "//lib/java/jdk13_linux:dist",
  format: :tar_gz,
  paths: [
    "lib/classlist",
    "lib/ct.sym",
    "lib/jexec",
    "lib/jfr/default.jfc",
    "lib/jfr/profile.jfc",
    "lib/jrt-fs.jar",
    "lib/jspawnhelper",
    "lib/jvm.cfg",
    "lib/libattach.so",
    "lib/libawt.so",
    "lib/libawt_headless.so",
    "lib/libawt_xawt.so",
    "lib/libdt_socket.so",
    "lib/libextnet.so",
    "lib/libfontmanager.so",
    "lib/libinstrument.so",
    "lib/libj2gss.so",
    "lib/libj2pcsc.so",
    "lib/libj2pkcs11.so",
    "lib/libjaas.so",
    "lib/libjava.so",
    "lib/libjavajpeg.so",
    "lib/libjawt.so",
    "lib/libjdwp.so",
    "lib/libjimage.so",
    "lib/libjli.so",
    "lib/libjsig.so",
    "lib/libjsound.so",
    "lib/liblcms.so",
    "lib/libmanagement.so",
    "lib/libmanagement_agent.so",
    "lib/libmanagement_ext.so",
    "lib/libmlib_image.so",
    "lib/libnet.so",
    "lib/libnio.so",
    "lib/libprefs.so",
    "lib/librmi.so",
    "lib/libsaproc.so",
    "lib/libsctp.so",
    "lib/libsplashscreen.so",
    "lib/libsunec.so",
    "lib/libunpack.so",
    "lib/libverify.so",
    "lib/libzip.so",
    "lib/modules",
    "lib/psfont.properties.ja",
    "lib/psfontj2d.properties",
    "lib/security/blacklisted.certs",
    "lib/security/cacerts",
    "lib/security/default.policy",
    "lib/security/public_suffix_list.dat",
    "lib/server/classes.jsa",
    "lib/server/libjsig.so",
    "lib/server/libjvm.so",
    "lib/src.zip",
    "lib/tzdb.dat",
  ],
  strip_components: 1,
)

java_compiler(
  name: "compiler",
  javac: "//lib/java/jdk13_linux:bin_javac",
  jar: "//lib/java/jdk13_linux:bin_jar",
  libs: "//lib/java/jdk13_linux:lib",
)

java_runtime(
  name: "runtime",
  java: "//lib/java/jdk13_linux:bin_java",
  libs: "//lib/java/jdk13_linux:lib",
)
