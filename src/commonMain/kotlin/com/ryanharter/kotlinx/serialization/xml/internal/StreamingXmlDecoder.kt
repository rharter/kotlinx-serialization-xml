package com.ryanharter.kotlinx.serialization.xml.internal

import com.ryanharter.kotlinx.serialization.xml.UndefinedNamespaceException
import com.ryanharter.kotlinx.serialization.xml.Xml
import com.ryanharter.kotlinx.serialization.xml.XmlContent
import com.ryanharter.kotlinx.serialization.xml.XmlDecoder
import com.ryanharter.kotlinx.serialization.xml.XmlEntity
import com.ryanharter.kotlinx.serialization.xml.XmlName
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class XmlElementDecoder(
  private val decoder: StreamingXmlDecoder,
  private val lexer: XmlLexer,
  descriptor: SerialDescriptor,
  parentNamespaceMap: Map<String, String> = emptyMap(),
) : CompositeDecoder {
  private var lastTextToken: XmlLexer.Token.Text? = null

  private val namespaceMap = parentNamespaceMap.toMutableMap()
  private val elementNames = (0 until descriptor.elementsCount).map { i ->
    val xmlName = descriptor.getElementAnnotations(i)
      .filterIsInstance<XmlName>()
      .firstOrNull()
    xmlName ?: XmlName(descriptor.getElementName(i), "")
  }

  override val serializersModule: SerializersModule = decoder.xml.serializersModule

  init {
    // Consume the start token if it exists.
    if (lexer.peek() == '<') {
      val startElement = lexer.readNextToken()
      require(startElement is XmlLexer.Token.ElementStart)
    }
  }

  private fun getElementIndex(name: String, namespace: String?): Int {
    val namespaceUri = namespace?.let {
      namespaceMap[it] ?: throw UndefinedNamespaceException(it)
    } ?: ""
    val index = elementNames.indexOfFirst {
      it.name == name && namespaceUri == it.namespace
    }
    return if (index > -1) index else UNKNOWN_NAME
  }

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    while (true) {
      when (val token = lexer.readNextToken()) {
        is XmlLexer.Token.ElementStartEnd -> continue
        is XmlLexer.Token.ElementEnd -> return DECODE_DONE
        is XmlLexer.Token.ElementStart -> return getElementIndex(token.name, token.namespace)
        is XmlLexer.Token.AttributeName -> {
          if (token.namespace == "xmlns") {
            val namespaceUri = lexer.readNextToken()
            require(namespaceUri is XmlLexer.Token.AttributeValue)
            namespaceMap[token.name] = namespaceUri.value
            continue
          }

          return getElementIndex(token.name, token.namespace)
        }
        is XmlLexer.Token.Text -> {
          lastTextToken = token

          val index = (0 until descriptor.elementsCount).indexOfFirst { i ->
            descriptor.getElementAnnotations(i).any { it is XmlContent }
          }
          return if (index == -1) UNKNOWN_NAME else index
        }
        else -> return UNKNOWN_NAME
      }
    }
  }

  override fun endStructure(descriptor: SerialDescriptor) {
    // no op
  }

  override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
    return when (val t = lexer.readNextToken()) {
      is XmlLexer.Token.AttributeValue -> t.value.toBoolean()
      is XmlLexer.Token.Text -> t.content.toBoolean()
      else -> throw IllegalArgumentException("Invalid boolean value")
    }
  }

  override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
    return decodeStringElement(descriptor, index).toByte()
  }

  override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
    return decodeStringElement(descriptor, index).toCharArray()[0]
  }

  override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
    return super.decodeCollectionSize(descriptor)
  }

  override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
    return decodeStringElement(descriptor, index).toDouble()
  }

  override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
    return decodeStringElement(descriptor, index).toFloat()
  }

  @ExperimentalSerializationApi
  override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
    return decodeStringElement(descriptor, index).toInt()
  }

  override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
    return decodeStringElement(descriptor, index).toLong()
  }

  @ExperimentalSerializationApi
  override fun <T : Any> decodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T?>,
    previousValue: T?
  ): T? {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeSequentially(): Boolean {
    return super.decodeSequentially()
  }

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?
  ): T {
    return deserializer.deserialize(decoder.copy(namespaceMap))
  }

  override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
    return decodeStringElement(descriptor, index).toShort()
  }

  override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
    val lastContent = lastTextToken?.content
    if (lastContent != null) {
      lastTextToken = null
      return lastContent
    }

    return when (val t = lexer.readNextToken()) {
      is XmlLexer.Token.AttributeValue -> t.value
      else -> throw IllegalArgumentException("Invalid string value: $t")
    }
  }
}

internal class StreamingXmlDecoder(
  override val xml: Xml,
  private val lexer: XmlLexer,
  private val namespaceMap: Map<String, String> = emptyMap(),
) : XmlDecoder {
  override val serializersModule: SerializersModule = xml.serializersModule

  internal fun copy(
    namespaceMap: Map<String, String> = this.namespaceMap,
  ): StreamingXmlDecoder = StreamingXmlDecoder(xml, lexer, namespaceMap)

  override fun decodeXmlEntity(): XmlEntity {
    TODO("Not yet implemented")
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    return XmlElementDecoder(this, lexer, descriptor, namespaceMap)
  }

  override fun decodeBoolean(): Boolean {
    TODO("Not yet implemented")
  }

  override fun decodeByte(): Byte {
    TODO("Not yet implemented")
  }

  override fun decodeChar(): Char {
    TODO("Not yet implemented")
  }

  override fun decodeDouble(): Double {
    TODO("Not yet implemented")
  }

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
    TODO("Not yet implemented")
  }

  override fun decodeFloat(): Float {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeInt(): Int {
    TODO("Not yet implemented")
  }

  override fun decodeLong(): Long {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing? {
    TODO("Not yet implemented")
  }

  override fun decodeShort(): Short {
    TODO("Not yet implemented")
  }

  override fun decodeString(): String {
    TODO("Not yet implemented")
  }

}
