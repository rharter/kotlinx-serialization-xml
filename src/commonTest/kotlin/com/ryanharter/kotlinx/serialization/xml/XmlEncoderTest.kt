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
  @XmlAttribute val from: String,
  @XmlAttribute val to: String,
  val message: Message
)

@Serializable
@SerialName("Message")
data class Message(
  @XmlContent val content: String
)

class XmlEncoderTest {

  private val default = Xml.Default

  @Test
  fun basic() {
    val xml = default.encodeToString(
      Greeting(
        from = "Ryan",
        to = "Bill",
        message = Message("Hi")
      )
    )
    val expected = """
      <Greeting from="Ryan" to="Bill"><Message>Hi</Message></Greeting>
    """.trimIndent()
    assertEquals(expected, xml)
  }

  @Test
  fun defaultNamespaces() {
    @Serializable
    @SerialName("DefaultNamespace")
    @XmlDefaultNamespace("http://example.com/entity")
    data class DefaultNamespace(
      @XmlAttribute val foo: String = "fooz",
      @XmlAttribute val bar: String = "barz",
    )

    val actual = default.encodeToString(DefaultNamespace())
    val expected = """
      <DefaultNamespace xmlns="http://example.com/entity" foo="fooz" bar="barz"/>
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun simpleAttributes() {
    @Serializable
    @SerialName("SimpleAttributes")
    data class SimpleAttributes(
      @XmlAttribute val first: String = "string",
      @XmlAttribute val second: Int = 1,
      @XmlAttribute val third: Float = 4.32f,
      @XmlAttribute val fourth: Double = 1.23,
      @XmlAttribute val fifth: Long = 123L,
      @XmlAttribute val sixth: Boolean = false,
      @XmlAttribute val seventh: Boolean = true,
    )

    val actual = default.encodeToString(SimpleAttributes())
    val expected =
      """<SimpleAttributes first="string" second="1" third="4.32" fourth="1.23" fifth="123" sixth="false" seventh="true"/>"""
    assertEquals(expected, actual)
  }

  @Test
  fun contentEncodedAsText() {
    @Serializable
    @SerialName("ContentAsText")
    data class ContentAsText(
      @XmlAttribute val first: String = "one",
      @XmlAttribute val second: String = "two",
      @XmlContent val third: String = "three",
      val fourth: String = "four",
    )

    val actual = default.encodeToString(ContentAsText())
    val expected =
      """<ContentAsText first="one" second="two">three<fourth>four</fourth></ContentAsText>"""
    assertEquals(expected, actual)
  }

  @Test
  fun encodesDefaultNamespaces() {
    @Serializable
    @SerialName("stream")
    @XmlNamespace("http://etherx.jabber.org/streams", "stream")
    @XmlDefaultNamespace("jabber:client")
    data class Stream(
      @XmlAttribute val from: String = "me@jabber.im",
      @XmlAttribute val to: String = "jabber.im",
      @XmlAttribute val version: String = "1.0",
      @XmlAttribute val lang: String = "en",
    )

    val actual = default.encodeToString(Stream())
    val expected = """<stream:stream xmlns:stream="http://etherx.jabber.org/streams" xmlns="jabber:client" from="me@jabber.im" to="jabber.im" version="1.0" lang="en"/>"""
    assertEquals(expected, actual)
  }

//  @Test
//  fun encodesMultipleNamespaces() {
//    @Serializable
//    @SerialName("stream")
//    @XmlNamespace("http://etherx.jabber.org/streams", "stream")
//    @XmlNamespace("jabber:client")
//    data class Stream(
//      @XmlAttribute val from: String = "me@jabber.im",
//      @XmlAttribute val to: String = "jabber.im",
//      @XmlAttribute val version: String = "1.0",
//      @XmlAttribute val lang: String = "en",
//    )
//
//    val actual = default.encodeToString(Stream())
//    val expected = """<stream:stream xmlns:stream="http://etherx.jabber.org/streams" xmlns="jabber:client" from="me@jabber.im" to="jabber.im" version="1.0" lang="en"/>"""
//    assertEquals(expected, actual)
//  }
}
