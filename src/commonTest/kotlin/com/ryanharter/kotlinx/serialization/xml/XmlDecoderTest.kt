package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class XmlDecoderTest {

  private val default = Xml.Default

  @Test fun basicXml() {
    val xml = """
      <Greeting from="Ryan" to="Bill">
        <message>
          Hi
        </message>
      </Greeting>
      
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(Greeting(
      from = "Ryan",
      to = "Bill",
      message = Message("Hi")
    ), actual)
  }

  @Test fun xmlWithNestedAttribute() {
    val xml = """
      <Greeting from="Ryan" to="Bill">
        <message content="Hi"/>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(Greeting(
      from = "Ryan",
      to = "Bill",
      message = Message("Hi")
    ), actual)
  }

  @Serializable
  data class NamespacedGreetings(
    @SerialName("greeting")
    val myGreeting: MyGreeting,
    @XmlName("greeting", "http://greetings.example.com/schema")
    val otherGreeting: OtherGreeting,
  )

  @Serializable
  data class MyGreeting(@XmlContent val message: String)

  @Serializable
  data class OtherGreeting(@XmlContent val message: String)


  @Test fun withElementNamespaces() {
    val xml = """
      <NamespacedGreetings xmlns:other="http://greetings.example.com/schema">
        <greeting>No namespaces here!</greeting>
        <other:greeting>Who is this?</other:greeting>
      </NamespacedGreetings>
    """.trimIndent()
    val actual = default.decodeFromString<NamespacedGreetings>(xml)
    assertEquals(NamespacedGreetings(
      MyGreeting("No namespaces here!"),
      OtherGreeting("Who is this?"),
    ), actual)
  }

  @Test fun undefinedNamespaces() {
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

  @Test fun withAttributeNamespaces() {
    val xml = """
      <NamespacedGreetings xmlns:other="http://greetings.example.com/schema">
        <greeting>No namespaces here!</greeting>
        <other:greeting>Who is this?</other:greeting>
      </NamespacedGreetings>
    """.trimIndent()
    val actual = default.decodeFromString<NamespacedGreetings>(xml)
    assertEquals(NamespacedGreetings(
      MyGreeting("No namespaces here!"),
      OtherGreeting("Who is this?"),
    ), actual)
  }

  @Test fun skipsComments() {
    val xml = """
      <!-- This is some fiiine XML! -->
      <Greeting from="Ryan" to="Bill">
        <!-- In here's not so bad! -->
        <message>Hi</message>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(Greeting(
      from = "Ryan",
      to = "Bill",
      message = Message("Hi")
    ), actual)
  }

  @Test fun skipsXmlDecl() {
    val xml = """
      <?xml version="1.1"?>
      <Greeting from="Ryan" to="Bill">
        <message>Hi</message>
      </Greeting>
    """.trimIndent()
    val actual = default.decodeFromString<Greeting>(xml)
    assertEquals(Greeting(
      from = "Ryan",
      to = "Bill",
      message = Message("Hi")
    ), actual)
  }
}