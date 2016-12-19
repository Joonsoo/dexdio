lazy val root = (project in file(".")).
    settings(
        name := "dexdio",
        version := "0.1",
        scalaVersion := "2.11.8"
        )

resolvers += "swt-repo" at "http://maven-eclipse.github.io/maven"

libraryDependencies += {
  val os = (sys.props("os.name"), sys.props("os.arch")) match {
    case ("Linux", "amd64" | "x86_64") => "gtk.linux.x86_64"
    case ("Linux", _) => "gtk.linux.x86"
    case ("Mac OS X", "amd64" | "x86_64") => "cocoa.macosx.x86_64"
    case ("Mac OS X", _) => "cocoa.macosx.x86"
    case (os, "amd64") if os.startsWith("Windows") => "win32.win32.x86_64"
    case (os, _) if os.startsWith("Windows") => "win32.win32.x86"
    case (os, arch) => sys.error("Cannot obtain lib for OS '" + os + "' and architecture '" + arch + "'")
  }
  val artifact = "org.eclipse.swt." + os
  "org.eclipse.swt" % artifact % "4.5"
}

libraryDependencies += "swt" % "jface" % "3.0.1"

javaOptions in run := {
  if (sys.props("os.name") == "Mac OS X") Seq("-XstartOnFirstThread", "-d64") else Seq()
}

javacOptions in compile ++= Seq("-encoding", "UTF-8")

EclipseKeys.relativizeLibs := false
EclipseKeys.withSource := true
