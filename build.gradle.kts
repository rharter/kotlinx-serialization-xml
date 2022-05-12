import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost.DEFAULT

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
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }


  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val jvmMain by getting
    val jvmTest by getting
    val jsMain by getting
    val jsTest by getting
    val nativeMain by getting
    val nativeTest by getting
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
