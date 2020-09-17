# vim: ft=ruby

http_file(
  name: "dist",
  url: "https://download.java.net/java/GA/jdk14.0.2/205943a0976c4ed48cb16f1043c5c647/12/GPL/openjdk-14.0.2_linux-x64_bin.tar.gz",
  sha256: "91310200f072045dc6cef2c8c23e7e6387b37c46e9de49623ce0fa461a24623d",
  os: :linux,
)

extract(
  name: "bin_jar",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/jar"],
  permissions: 0755,
  strip_components: 1,
  os: :linux,
)

extract(
  name: "bin_java",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/java"],
  permissions: 0755,
  strip_components: 1,
  os: :linux,
)

extract(
  name: "bin_javac",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/javac"],
  permissions: 0755,
  strip_components: 1,
  os: :linux,
)

extract(
  name: "lib",
  archive: ":dist",
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
  os: :linux,
)

java_archiver(
  name: "archiver",
  jar: ":bin_jar",
  libs: ":lib",
  os: :linux,
)

java_compiler(
  name: "compiler",
  javac: ":bin_javac",
  libs: ":lib",
  os: :linux,
)

java_runtime(
  name: "runtime",
  java: ":bin_java",
  libs: ":lib",
  os: :linux,
)
