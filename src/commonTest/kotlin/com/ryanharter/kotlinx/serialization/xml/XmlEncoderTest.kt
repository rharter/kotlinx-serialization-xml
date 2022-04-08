@file:OptIn(ExperimentalSerializationApi::class)

package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
@SerialName("Greeting")
data class Greeting(
  val from: String,
  val to: String,
  val message: Message
)

@Serializable
@SerialName("Message")
data class Message(
  @XmlContent val content: String
)

class XmlEncoderTest {

  private val default = Xml.Default

  @Test fun element() {
    val xml = default.encodeToString(Greeting(
      from = "Ryan",
      to = "Bill",
      message = Message("Hi")
    ))
    val expected = """
      <Greeting from="Ryan" to="Bill">
        <Message>
          Hi
        </Message>
      </Greeting>
      
    """.trimIndent()
    assertEquals(expected, xml)
  }
}