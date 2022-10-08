import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost.DEFAULT
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.mavenPublish)
}

group = "com.ryanharter.kotlinx.serialization"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  explicitApi()

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "1.8"
    }
    withJava()
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }
  js {
    nodejs()
  }

  // Note: Keep native list in sync with kotlinx.serialization:
  // https://github.com/Kotlin/kotlinx.serialization/blob/master/gradle/native-targets.gradle
  linuxX64()
  linuxArm32Hfp()
  linuxArm64()
  macosX64()
  macosArm64()
  mingwX86()
  mingwX64()
  iosX64()
  iosArm32()
  iosArm64()
  iosSimulatorArm64()
  watchosX86()
  watchosX64()
  watchosArm32()
  watchosArm64()
  watchosSimulatorArm64()
  tvosX64()
  tvosArm64()
  tvosSimulatorArm64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.serialization)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }

  sourceSets.all {
    languageSettings {
      optIn("kotlinx.serialization.ExperimentalSerializationApi")
    }
  }

  // Add a test binary and execution for native targets which runs on a background thread.
  targets.withType(KotlinNativeTargetWithTests::class).all {
    binaries {
      test("background", listOf(NativeBuildType.DEBUG)) {
        freeCompilerArgs += listOf("-trw")
      }
    }
    testRuns {
      create("background") {
        setExecutionSourceFrom(binaries.getByName("backgroundDebugTest") as TestExecutable)
      }
    }
  }
}

mavenPublishing {
  configure(KotlinMultiplatform())

  publishToMavenCentral(DEFAULT)
  signAllPublications()
  pom {
    description.set("A fully native, multiplatform XML format add-on for Kotlin Serialization.")
    name.set(project.name)
    url.set("https://github.com/rharter/kotlinx-serialization-xml/")
    licenses {
      license {
        name.set("The Apache Software License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("repo")
      }
    }
    scm {
      url.set("https://github.com/rharter/kotlinx-serialization-xml/")
      connection.set("scm:git:git://github.com/rharter/kotlinx-serialization-xml.git")
      developerConnection.set("scm:git:ssh://git@github.com/rharter/kotlinx-serialization-xml.git")
    }
    developers {
      developer {
        id.set("rharter")
        name.set("Ryan Harter")
      }
    }
  }
}
