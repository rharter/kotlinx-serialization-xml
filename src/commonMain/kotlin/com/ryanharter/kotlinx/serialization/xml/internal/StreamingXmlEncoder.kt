package com.ryanharter.kotlinx.serialization.xml.internal

import com.ryanharter.kotlinx.serialization.xml.Xml
import com.ryanharter.kotlinx.serialization.xml.XmlAttribute
import com.ryanharter.kotlinx.serialization.xml.XmlContent
import com.ryanharter.kotlinx.serialization.xml.XmlDefaultNamespace
import com.ryanharter.kotlinx.serialization.xml.XmlEncoder
import com.ryanharter.kotlinx.serialization.xml.XmlEntity
import com.ryanharter.kotlinx.serialization.xml.XmlNamespace
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class StreamingXmlEncoder(
  override val xml: Xml,
  private val composer: Composer,
  private val namespaces: Map<String, XmlNamespace> = emptyMap(),
) : XmlEncoder {
  override val serializersModule: SerializersModule = xml.serializersModule

  private var startTagClosed = false

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
    composer.indent().newElement()
    composer.append("<")

    // Get the namespace of the element
    val namespace = descriptor.annotations
      .filterIsInstance<XmlNamespace>()
      .firstOrNull { it.localName.isNotBlank() }
      ?.let { namespaces[it.uri] ?: it }

    if (namespace != null && namespace.localName.isNotBlank()) {
      composer.append(namespace.localName).append(":")
    }
    composer.append(descriptor.serialName)

    // We only define the namespace if it isn't already in scope
    if (namespace != null && !namespaces.contains(namespace.uri)) {
      composer.newAttribute().append(namespace)
    }

    // Check for a default namespace
    val defaultNamespace = descriptor.annotations.filterIsInstance<XmlDefaultNamespace>().firstOrNull()
    if (defaultNamespace != null) {
      composer.newAttribute().append(defaultNamespace)
    }

    // Collect all new child namespaces
    val newNamespaces = (0 until descriptor.elementsCount)
      // XmlContent annotated properties are encoded as text, not elements or annotations, so
      // we don't need their namespaces.
      .filterNot {
        descriptor.getElementAnnotations(it).filterIsInstance<XmlContent>().isNotEmpty()
      }
      .flatMap {
        descriptor.getElementAnnotations(it).filterIsInstance<XmlNamespace>() +
          descriptor.getElementDescriptor(it).annotations.filterIsInstance<XmlNamespace>()
      }
      .filterNot { namespaces.contains(it.uri) }
      .associateBy { it.uri }

    newNamespaces.values.forEach { ns ->
      composer.newAttribute().append(ns)
    }

    val childNamespaces = namespaces.toMutableMap()
    childNamespaces += newNamespaces
    namespace?.let { childNamespaces[it.uri] = it }

    return StreamingXmlEncoder(
      xml, composer, childNamespaces
    )
  }

  private fun Composer.append(namespace: XmlNamespace): Composer {
    append("xmlns")
    if (namespace.localName.isNotBlank()) {
      append(":").append(namespace.localName)
    }
    append("=")
    encodeString(namespace.uri)
    return this
  }

  private fun Composer.append(namespace: XmlDefaultNamespace): Composer {
    append("xmlns").append("=")
    encodeString(namespace.uri)
    return this
  }

  override fun endStructure(descriptor: SerialDescriptor) {
    if (!startTagClosed) {
      composer.append("/>").unindent()
      return
    }

    composer.unindent().appendLine().append("</")
    val namespace = descriptor.annotations
      .filterIsInstance<XmlNamespace>()
      .firstOrNull()
      ?.let {
        namespaces[it.uri] ?: it
      } // If the NS is already defined we don't need to redefine it.
    if (namespace != null && namespace.localName.isNotBlank()) {
      composer.append(namespace.localName).append(":")
    }
    composer.append(descriptor.serialName).append(">")
  }

  private fun SerialDescriptor.getNamespace(index: Int): XmlNamespace? {
    return (getElementAnnotations(index).filterIsInstance<XmlNamespace>().firstOrNull()
      ?: getElementDescriptor(index).annotations.filterIsInstance<XmlNamespace>().firstOrNull())
      ?.let { namespaces[it.uri] ?: it }
  }

  override fun encodeNull() {
    throw SerializationException("'null' is not supported by default")
  }

  private fun encodeValue(value: Any) {
    val quoteChar = if (startTagClosed) "" else "\""
    composer.append(quoteChar).append(value.toString()).append(quoteChar)
  }

  private fun encodeElement(descriptor: SerialDescriptor, index: Int, value: Any) {
    if (descriptor.getElementAnnotations(index).filterIsInstance<XmlAttribute>().isNotEmpty()) {
      require(!startTagClosed) {
        "Property ${descriptor.getElementName(index)} annotated with XmlAttribute after non-annotated properties."
      }

      encodeAttribute(descriptor, index, value)
      return
    }

    if (!startTagClosed) {
      startTagClosed = true
      composer.append(">").indent().appendLine()
    }

    // If this is a content element then we only need to write the value
    if (descriptor.getElementAnnotations(index).filterIsInstance<XmlContent>().isNotEmpty()) {
      composer.append(value.toString())
      return
    }

    val prefix = descriptor.getNamespace(index)?.localName?.let { "$it:" } ?: ""
    val elementName = descriptor.getElementName(index)
    val tagName = "$prefix$elementName"

    composer.append("<").append(tagName).append(">")
    composer.append(value.toString())
    composer.append("</").append(tagName).append(">")
  }

  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T
  ) {
    if (!startTagClosed) {
      startTagClosed = true
      composer.append(">")
    }
    encodeSerializableValue(serializer, value)
  }

  private fun encodeAttribute(descriptor: SerialDescriptor, index: Int, value: Any) {
    composer.newAttribute()
    descriptor.getNamespace(index)?.localName?.let { localName ->
      composer.append(localName).append(":")
    }
    composer.append(descriptor.getElementName(index))

    // TODO only add this if Xml.config.encodeBooleans == true
    composer.append("=")
    encodeValue(value.toString())
  }

  override fun encodeBoolean(value: Boolean): Unit = encodeValue(value)
  override fun encodeByte(value: Byte): Unit = encodeValue(value)
  override fun encodeShort(value: Short): Unit = encodeValue(value)
  override fun encodeInt(value: Int): Unit = encodeValue(value)
  override fun encodeLong(value: Long): Unit = encodeValue(value)
  override fun encodeFloat(value: Float): Unit = encodeValue(value)
  override fun encodeDouble(value: Double): Unit = encodeValue(value)
  override fun encodeChar(value: Char): Unit = encodeValue(value)
  override fun encodeString(value: String): Unit = encodeValue(value)
  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int): Unit = encodeValue(index)
  override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

  // Delegating implementation of CompositeEncoder
  override fun encodeBooleanElement(
    descriptor: SerialDescriptor,
    index: Int,
    value: Boolean
  ): Unit = encodeElement(descriptor, index, value)

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char): Unit =
    encodeElement(descriptor, index, value)

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String): Unit =
    encodeElement(descriptor, index, value)

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?
  ) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
    TODO("Not yet implemented")
  }

  override fun encodeXmlEntity(entity: XmlEntity) {
    encodeSerializableValue(XmlEntitySerializer, entity)
  }
}
