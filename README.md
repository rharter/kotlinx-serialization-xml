# Kotlin Serialization XML

A fully native, multiplatform XML format add-on for [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization).

> **Status**: This project is in the early stages of development, so isn't yet feature complete.

## Usage

Kotlin Serialization XML provides an `Xml` format for Kotlin Serialization, allowing you to use the
standard `@Serializable` annotation to create reflectionless, multiplatform serializers for your Kotlin
classes.

```kotlin
@Serializable
data class Greeting(
  @XmlAttribute val from: String,
  @XmlAttribute val to: String,
  val message: Message
)

@Serializable
data class Message(
  @XmlContent val content: String
)

val xml = """
      <Greeting from="Ryan" to="Bill">
        <message>Hi</message>
      </Greeting>
    """.trimIndent()
val actual = Xml.Default.decodeFromString<Greeting>(xml)
```

By default, all properties are encoded as nested XML elements.

To encode properties as attributes, they must be annotated with `@XmlAttribute`. All `@XmlAttribute`
annotated properties must come before other properties in an object.

After the attributes, properties will be encoded as nested elements, but can also be encoded as
bare string content by annotating the property with `@XmlContent`.

## Installation

Development versions are available in Sonatype's snapshot repository.

```kotlin
repositories {
  maven {
    url("https://oss.sonatype.org/content/repositories/snapshots/")
  }
}
dependencies {
  implementation("com.ryanharter.kotlinx.serialization:kotlinx-serialization-xml:0.0.1-SNAPSHOT")
}
```

# License

    Copyright 2022 Ryan Harter

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
