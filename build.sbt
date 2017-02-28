lazy val root = (project in file(".")).
    settings(
        name := "dexdio",
        version := "0.1",
        scalaVersion := "2.12.1"
    )

resolvers += "swt-repo" at "http://maven-eclipse.github.io/maven"
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= {
    val archs = Seq(
        "gtk.linux.x86_64",
        "gtk.linux.x86",
        "cocoa.macosx.x86_64",
        "win32.win32.x86_64",
        "win32.win32.x86"
    )
    val artifacts = archs map { arch => s"org.eclipse.swt.$arch" }
    artifacts map { artifact => "org.eclipse.swt" % artifact % "4.6.1" }
}

libraryDependencies += "swt" % "jface" % "3.0.1"

libraryDependencies += "com.github.Joonsoo" % "StructuredTextView" % "master-SNAPSHOT"

javaOptions in run := {
    println(sys.props("os.name"))
    if (sys.props("os.name") == "Mac OS X") Seq("-XstartOnFirstThread", "-d64") else Seq()
}

javacOptions in compile ++= Seq("-encoding", "UTF-8")

fork in run := true

// javaOptions in run += "-agentlib:hprof=cpu=samples"
