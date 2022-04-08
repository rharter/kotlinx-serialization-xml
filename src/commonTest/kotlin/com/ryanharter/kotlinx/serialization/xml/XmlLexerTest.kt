package com.ryanharter.kotlinx.serialization.xml

import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.AttributeName
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.AttributeValue
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.DocumentEnd
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.ElementEnd
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.ElementStart
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.ElementStartEnd
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer.Token.Text
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlLexerTest {

  @Test
  fun skipsXmlDecl() {
    XmlLexer(
      """
        <?xml version="1.1"?>
        <foo></foo>
      """.trimIndent()
    )
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun skipsDoctype() {
    XmlLexer(
      """
        <!DOCTYPE html5>
        <foo></foo>
      """.trimIndent()
    )
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun skipsComments() {
    XmlLexer(
      """
        <!-- This is a comment -->
        <foo>
          <!-- Here's one inside a thingy! -->
        </foo>
      """.trimIndent()
    )
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun element() {
    XmlLexer("""<foo></foo>""")
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun elementWithTextContent() {
    XmlLexer("""<foo>some text</foo>""")
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        Text("some text"),
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun elementWithCDATA() {
    XmlLexer("""<foo>some text with <![CDATA[cdata text that has w3!rd ch<r<ct3rs]]> in it</foo>""")
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        Text("some text with cdata text that has w3!rd ch<r<ct3rs in it"),
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun elementWithAttribute() {
    XmlLexer("""<foo attribute="bar"></foo>""")
      .expectNextTokens(
        ElementStart("foo"),
        AttributeName("attribute"),
        AttributeValue("bar"),
        ElementStartEnd,
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun elementWithAttributes() {
    XmlLexer("""<foo first="bar" second="baz"></foo>""")
      .expectNextTokens(
        ElementStart("foo"),
        AttributeName("first"),
        AttributeValue("bar"),
        AttributeName("second"),
        AttributeValue("baz"),
        ElementStartEnd,
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun selfClosingElement() {
    XmlLexer("""<foo/>""")
      .expectNextTokens(
        ElementStart("foo"),
        ElementEnd(),
        DocumentEnd,
      )
  }

  @Test
  fun selfClosingElementWithSpace() {
    XmlLexer("""<foo />""")
      .expectNextTokens(
        ElementStart("foo"),
        ElementEnd(),
        DocumentEnd,
      )
  }

  @Test
  fun selfClosingElementWithAttribute() {
    XmlLexer("""<foo attribute="bar"/>""")
      .expectNextTokens(
        ElementStart("foo"),
        AttributeName("attribute"),
        AttributeValue("bar"),
        ElementEnd(),
        DocumentEnd,
      )
  }

  @Test
  fun selfClosingElementWithAttributes() {
    XmlLexer("""<foo first="bar" second="baz"/>""")
      .expectNextTokens(
        ElementStart("foo"),
        AttributeName("first"),
        AttributeValue("bar"),
        AttributeName("second"),
        AttributeValue("baz"),
        ElementEnd(),
        DocumentEnd,
      )
  }

  @Test
  fun nestedElements() {
    XmlLexer("""<foo><bar first="baz">text</bar></foo>""")
      .expectNextTokens(
        ElementStart("foo"),
        ElementStartEnd,
        ElementStart("bar"),
        AttributeName("first"),
        AttributeValue("baz"),
        ElementStartEnd,
        Text("text"),
        ElementEnd("bar"),
        ElementEnd("foo"),
        DocumentEnd,
      )
  }

  @Test
  fun nestedElementsMixedWithContent() {
    XmlLexer(
      """
        <foo>
          Text Content
          <bar first="baz" />
        </foo>
      """.trimIndent()
    ).expectNextTokens(
      ElementStart("foo"),
      ElementStartEnd,
      Text("Text Content"),
      ElementStart("bar"),
      AttributeName("first"),
      AttributeValue("baz"),
      ElementEnd(),
      ElementEnd("foo"),
      DocumentEnd,
    )
  }

  private fun XmlLexer.expectNextTokens(vararg expected: Token) {
    expected.forEach {
      assertEquals(it, readNextToken())
    }
  }
}