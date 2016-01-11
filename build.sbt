name := """dkpro-scala-example-2"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.clearnlp-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.opennlp-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.io.text-asl" % "1.7.0",
  "org.apache.uima" % "uimaj-as-activemq" % "2.6.0",
  "com.clearnlp" % "clearnlp-dictionary" % "1.0",
  "com.clearnlp" % "clearnlp-general-en-pos" % "1.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

