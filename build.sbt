name := """dkpro-scala-example-2"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.clearnlp-asl" % "1.8.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.opennlp-asl" % "1.8.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.io.text-asl" % "1.8.0",
  "org.apache.uima" % "uimaj-as-activemq" % "2.8.1",
  "com.clearnlp" % "clearnlp-dictionary" % "1.0",
  "com.clearnlp" % "clearnlp-general-en-pos" % "1.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
  "org.jmock" % "jmock" % "2.8.2" % "test",
  "org.jmock" % "jmock-legacy" % "2.8.2" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

parallelExecution in Test := false

fork := true

javaOptions ++= Seq(
  "-Xmx4G")
