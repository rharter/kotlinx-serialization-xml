package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class XmlDecoderTest {

  private val default = Xml.Default

  @Test
  fun basicXml() {
    val xml = """
      <Greeting from="Ryan" to="Bill">
        <message>Hi</message>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(
      Greeting(
        from = "Ryan",
        to = "Bill",
        message = Message("Hi")
      ), actual
    )
  }

  @Test
  fun xmlWithNestedAttribute() {
    val xml = """
      <Greeting from="Ryan" to="Bill">
        <message content="Hi"/>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(
      Greeting(
        from = "Ryan",
        to = "Bill",
        message = Message("Hi")
      ), actual
    )
  }

  @Serializable
  data class NamespacedGreetings(
    @SerialName("greeting")
    val myGreeting: MyGreeting,
    @XmlName("greeting")
    @XmlNamespace("http://greetings.example.com/schema", "")
    val otherGreeting: OtherGreeting,
  )

  @Serializable
  data class MyGreeting(@XmlContent val message: String)

  @Serializable
  data class OtherGreeting(@XmlContent val message: String)

  @Test
  fun withElementNamespaces() {
    val xml = """
      <NamespacedGreetings xmlns:other="http://greetings.example.com/schema">
        <greeting>No namespaces here!</greeting>
        <other:greeting>Who is this?</other:greeting>
      </NamespacedGreetings>
    """.trimIndent()
    val actual = default.decodeFromString<NamespacedGreetings>(xml)
    assertEquals(
      NamespacedGreetings(
        MyGreeting("No namespaces here!"),
        OtherGreeting("Who is this?"),
      ), actual
    )
  }

  @Test
  fun undefinedNamespaces() {
    val xml = """
      <NamespacedGreetings>
        <greeting>No namespaces here!</greeting>
        <other:greeting>Who is this?</other:greeting>
      </NamespacedGreetings>
    """.trimIndent()
    try {
      default.decodeFromString<NamespacedGreetings>(xml)
      fail("Expected UndefinedNamespaceException.")
    } catch (e: UndefinedNamespaceException) {
      // success
    }
  }

  @Serializable
  data class Attributes(
    val unannotated: String,
    @XmlAttribute
    val unnamed: String,
    @XmlAttribute
    @XmlName("namedAttribute")
    val named: String,
    @XmlAttribute
    @XmlName("namedNamespaced")
    @XmlNamespace("http://greetings.example.com/schema", "")
    val namedAndNamespaced: String,
    @XmlAttribute
    @XmlNamespace("http://greetings.example.com/schema", "")
    val namespaced: String,
    @XmlNamespace("http://greetings.example.com/schema", "")
    val onlyNamespaced: String,
  )

  @Test
  fun attributeNamespaces() {
    val xml = """
      <Attributes xmlns:ns="http://greetings.example.com/schema"
        unannotated="first"
        unnamed="second"
        namedAttribute="third"
        ns:namedNamespaced="fourth"
        ns:namespaced="fifth"
        ns:onlyNamespaced="sixth" />
    """.trimIndent()
    val actual = default.decodeFromString<Attributes>(xml)
    assertEquals(
      Attributes(
        "first",
        "second",
        "third",
        "fourth",
        "fifth",
        "sixth",
      ), actual
    )
  }

  @Test
  fun namespacedAttributeBeforeNamespaceDecl() {
    val xml = """
      <Attributes
        unannotated="first"
        unnamed="second"
        namedAttribute="third"
        ns:namedNamespaced="fourth"
        ns:namespaced="fifth"
        ns:onlyNamespaced="sixth"
        xmlns:ns="http://greetings.example.com/schema" />
    """.trimIndent()
    val actual = default.decodeFromString<Attributes>(xml)
    assertEquals(
      Attributes(
        "first",
        "second",
        "third",
        "fourth",
        "fifth",
        "sixth",
      ), actual
    )
  }

  @Serializable
  @XmlNamespace("http://etherx.jabber.org/streams", "")
  data class Stream(val from: String, val to: String)

  @Test
  fun namespacedElementContainingNamespaceDecl() {
    val xml = """
      <stream:stream
        from="source@xmpp.org"
        to="dest@xmpp.org"
        xmlns:stream="http://etherx.jabber.org/streams"/>
    """.trimIndent()
    val actual = default.decodeFromString<Stream>(xml)
    assertEquals(Stream(from = "source@xmpp.org", to = "dest@xmpp.org"), actual)
  }

  @Serializable
  data class StreamHolder(val stream: Stream)

  @Test
  fun embeddedNamespacedElementContainingNamespaceDecl() {
    val xml = """
      <StreamHolder>
        <stream:stream
          from="source@xmpp.org"
          to="dest@xmpp.org"
          xmlns:stream="http://etherx.jabber.org/streams"/>
      </StreamHolder>
    """.trimIndent()
    val actual = default.decodeFromString<StreamHolder>(xml)
    assertEquals(StreamHolder(Stream(from = "source@xmpp.org", to = "dest@xmpp.org")), actual)
  }

  @Test
  fun skipsComments() {
    val xml = """
      <!-- This is some fiiine XML! -->
      <Greeting from="Ryan" to="Bill">
        <!-- In here's not so bad! -->
        <message>Hi</message>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(
      Greeting(
        from = "Ryan",
        to = "Bill",
        message = Message("Hi")
      ), actual
    )
  }

  @Test
  fun skipsXmlDecl() {
    val xml = """
      <?xml version="1.1"?>
      <Greeting from="Ryan" to="Bill">
        <message>Hi</message>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(
      Greeting(
        from = "Ryan",
        to = "Bill",
        message = Message("Hi")
      ), actual
    )
  }

  @Test
  fun readsBooleanPresenceAsTrue() {
    @Serializable
    data class Data(
      val required: Boolean = true,
    )

    val xml = """
      <data><required/></data>
    """.trimIndent()
    val actual = default.decodeFromString<Data>(xml)
    assertEquals(Data(true), actual)
  }

  @Test
  fun readsBooleanAttributePresenceAsTrue() {
    @Serializable
    data class Data(
      @XmlAttribute val required: Boolean = true,
      @XmlAttribute val snakeCount: Int = 4,
    )

    val xml = """
      <date required snakeCount="4"/>
    """.trimIndent()
    val actual = default.decodeFromString<Data>(xml)
    assertEquals(Data(true), actual)
  }
}
