name := "KafkaMocProducer"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)


lazy val raiz = (project in file("."))
  .settings(mainClass in Compile := Some("TestApplication"))



libraryDependencies ++= Seq(
  "com.hortonworks.registries" % "schema-registry-serdes" % "0.8.0",
  "org.apache.kafka" % "kafka-clients" % "2.0.0",
  "com.typesafe" % "config" % "1.3.4",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "org.apache.avro" % "avro" % "1.8.2"
  //  "com.hortonworks" % "spark-schema-registry" % "1.1.0.3.1.5.14-1"
)