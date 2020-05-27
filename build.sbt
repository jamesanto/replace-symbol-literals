import ReleaseTransformations._

lazy val V = _root_.scalafix.sbt.BuildInfo

lazy val commonSettings = Def.settings(
  List(
    organization := "com.github.xuwei-k",
    homepage := Some(url("https://github.com/xuwei-k/replace-symbol-literals")),
    licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
    description := "scalafix rule for replace deprecated scala.Symbol literals",
    scalaVersion := "2.13.2",
    addCompilerPlugin(scalafixSemanticdb("4.3.10")),
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    ),
    pomExtra := (
      <developers>
        <developer>
          <id>xuwei-k</id>
          <name>Kenji Yoshida</name>
          <url>https://github.com/xuwei-k</url>
        </developer>
      </developers>
      <scm>
        <url>git@github.com:xuwei-k/replace-symbol-literals.git</url>
        <connection>scm:git:git@github.com:xuwei-k/replace-symbol-literals.git</connection>
      </scm>
    ),
    publishTo := sonatypePublishTo.value,
    scalacOptions in (Compile, doc) ++= {
      val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
      Seq(
        "-sourcepath",
        (baseDirectory in LocalRootProject).value.getAbsolutePath,
        "-doc-source-url",
        s"https://github.com/xuwei-k/replace-symbol-literals/tree/${hash}€{FILE_PATH}.scala"
      )
    },
    scalacOptions ++= PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, v)) if v >= 12 =>
          Seq(
            "-Ywarn-unused:imports",
          )
      }
      .toList
      .flatten,
    scalacOptions ++= PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, v)) if v <= 12 =>
          Seq(
            "-Yno-adapted-args",
            "-Xfuture",
          )
      }
      .toList
      .flatten,
    scalacOptions ++= List(
      "-deprecation",
      "-unchecked",
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
)

commonSettings
skip in publish := true

lazy val rules = project.settings(
  commonSettings,
  name := "replace-symbol-literals",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % "0.9.15.2-SNAPSHOT",
  version := "0.1.2.2-SNAPSHOT",
  credentials += Credentials("Sonatype Nexus Repository Manager", "127.0.0.1", "admin", "admin"),
  publishTo := Some("Sonatype Nexus Repository Manager" at "http://127.0.0.1:8081/repository/maven-snapshots")
)

lazy val input = project.settings(
  commonSettings,
  skip in publish := true
)

lazy val output = project.settings(
  commonSettings,
  skip in publish := true
)

lazy val tests = project
  .settings(
    commonSettings,
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % "0.9.15.2-SNAPSHOT" % Test cross CrossVersion.full,
    compile in Compile :=
      compile.in(Compile).dependsOn(compile.in(input, Compile)).value,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value,
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
