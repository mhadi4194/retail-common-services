import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  application
  idea
  eclipse
  java
  id("net.ltgt.errorprone") version "0.8.1"
  id("com.github.johnrengelman.shadow") version "6.1.0"
  // id("com.google.cloud.artifactregistry.gradle-plugin") version "2.1.0"
}

repositories {
  maven {
    url = uri("file://${rootProject.projectDir}/libs/maven")
  }
  mavenCentral()
  google()
  // maven("artifactregistry://us-maven.pkg.dev/retail-common-services-249016/spez-maven-repo")
}

dependencies {
  implementation(project(":core"))
  implementation(project(":common"))

  implementation(Config.Libs.typesafe_config)
  implementation(Config.Libs.slf4j)
  implementation(Config.Libs.logback_classic)
  implementation(Config.Libs.logback_core)

  implementation(Config.Libs.groovy) // For logback
  implementation(Config.Libs.guava)
  implementation(Config.Libs.pubsub)
  implementation("io.opencensus:opencensus-exporter-trace-stackdriver:0.28.3")
  implementation("io.opencensus:opencensus-contrib-zpages:0.28.3")

  // AutoValue
  compileOnly("com.google.auto.value:auto-value-annotations:1.6.2")
  annotationProcessor("com.google.auto.value:auto-value:1.6.2")

  // ---
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
  annotationProcessor("com.uber.nullaway:nullaway:0.7.5")
  errorprone("com.google.errorprone:error_prone_core:2.3.3")
  errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")
}

// ErrorProne
tasks.withType<JavaCompile>().configureEach {
  options.errorprone.excludedPaths.set(".*/gen/.*")
  options.errorprone.disableWarningsInGeneratedCode.set(true)

  if (!name.toLowerCase().contains("test")) {
    options.errorprone {
      check("NullAway", CheckSeverity.ERROR)
      option("NullAway:AnnotatedPackages", "com.uber")
    }
  }
}

tasks.withType<ShadowJar>() {
  mergeServiceFiles()
}

val project_id = System.getenv().get("PROJECT_ID")
val sink_instance = System.getenv().get("SINK_INSTANCE")
val sink_database = System.getenv().get("SINK_DATABASE")
val sink_table = System.getenv().get("SINK_TABLE")
val lpts_instance = System.getenv().get("LPTS_INSTANCE")
val lpts_database = System.getenv().get("LPTS_DATABASE")
val lpts_table = System.getenv().get("LPTS_TABLE")
val default_log_level = System.getenv("DEFAULT_LOG_LEVEL")
val timestamp_column = System.getenv("TIMESTAMP_COLUMN")
val uuid_column = System.getenv("UUID_COLUMN")

application {
  mainClassName = "com.google.spez.cdc.Main"
  applicationDefaultJvmArgs = listOf(
    "-Dspez.auth.cloud_secrets_dir=${rootProject.projectDir}/secrets",
    "-Dspez.project_id=$project_id",
    "-Dspez.auth.credentials=credentials.json",
    "-Dspez.pubsub.topic=spez-ledger-topic",
    "-Dspez.sink.instance=$sink_instance",
    "-Dspez.sink.database=$sink_database",
    "-Dspez.sink.table=$sink_table",
    "-Dspez.sink.uuid_column=$uuid_column",
    "-Dspez.sink.timestamp_column=$timestamp_column",
    "-Dspez.lpts.instance=$lpts_instance",
    "-Dspez.lpts.database=$lpts_database",
    "-Dspez.lpts.table=$lpts_table",
    "-Dspez.loglevel.default=$default_log_level",
    "-Dspez.loglevel.com.google.spez.core.EventPublisher=INFO",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.allocator.type=pooled",
    "-XX:+UnlockExperimentalVMOptions",
    "-XX:ConcGCThreads=4",
    "-XX:+UseNUMA",
    "-XX:+UseStringDeduplication",
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-Dcom.sun.management.jmxremote",
    "-Dcom.sun.management.jmxremote.port=9010",
    "-Dcom.sun.management.jmxremote.rmi.port=9010",
    "-Dcom.sun.management.jmxremote.local.only=false",
    "-Dcom.sun.management.jmxremote.authenticate=false",
    "-Dcom.sun.management.jmxremote.ssl=false",
    "-Djava.rmi.server.hostname=127.0.0.1"
  )
}
