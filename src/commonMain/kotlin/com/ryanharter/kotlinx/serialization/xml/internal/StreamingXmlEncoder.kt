package com.ryanharter.kotlinx.serialization.xml.internal

import com.ryanharter.kotlinx.serialization.xml.Xml
import com.ryanharter.kotlinx.serialization.xml.XmlContent
import com.ryanharter.kotlinx.serialization.xml.XmlEncoder
import com.ryanharter.kotlinx.serialization.xml.XmlEntity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

public interface XmlChild {
  public fun write(output: StringBuilder)
}

internal data class XmlContentValue(
  val value: String,
  val level: Int = 0
) : XmlChild {
  override fun write(output: StringBuilder) {
    repeat(level) { output.append("  ") }
    output.append(value).appendLine()
  }
}

@OptIn(ExperimentalSerializationApi::class)
internal class XmlElementEncoder(
  val encoder: StreamingXmlEncoder,
  val descriptor: SerialDescriptor,
  val level: Int = 0,
) : Encoder, CompositeEncoder, XmlChild {
  override val serializersModule: SerializersModule = encoder.xml.serializersModule

  private val attributes = mutableListOf<XmlEntity.Attribute>()
  private val children = mutableListOf<XmlChild>()

  override fun write(output: StringBuilder) {
    repeat(level) { output.append("  ") }
    output.append("<")
    output.append(descriptor.serialName)

    attributes.forEach {
      output.append(" ").append(it.name).append("=").append('"').append(it.value).append('"')
    }

    if (children.isNotEmpty()) {
      output.append(">").appendLine()
      children.forEach {
        it.write(output)
      }

      repeat(level) { output.append("  ") }
      output.append("</").append(descriptor.serialName).append(">")
    } else {
      output.append("/>")
    }

    output.appendLine()
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
    return this
  }

  override fun endStructure(descriptor: SerialDescriptor) {
    // no op
  }

  override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
    encodeStringElement(descriptor, index, value.toString())
  }

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
    encodeStringElement(descriptor, index, value.toString())
  }

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
    encodeStringElement(descriptor, index, value.toString())
  }

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
    encodeStringElement(descriptor, index, value.toString())
  }

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
    encodeStringElement(descriptor, index, value.toString())
  }

  @ExperimentalSerializationApi
  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
    TODO("Not yet implemented")
  }

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
    encodeStringElement(descriptor, index, value.toString())
  }

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
    encodeStringElement(descriptor, index, value.toString())
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?
  ) {
    TODO("Not yet implemented")
  }

  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T
  ) {
    val childEncoder = XmlElementEncoder(encoder, serializer.descriptor, level + 1).also { children.add(it) }
    serializer.serialize(childEncoder, value)
  }

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
    encodeStringElement(descriptor, index, value.toString())
  }

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
    if (descriptor.getElementAnnotations(index).any { it is XmlContent }) {
      children.add(XmlContentValue(value, level + 1))
    } else {
      attributes.add(XmlEntity.Attribute(descriptor.getElementName(index), value))
    }
  }

  override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
    return super.beginCollection(descriptor, collectionSize)
  }

  override fun encodeBoolean(value: Boolean) {
    TODO("Not yet implemented")
  }

  override fun encodeByte(value: Byte) {
    TODO("Not yet implemented")
  }

  override fun encodeChar(value: Char) {
    TODO("Not yet implemented")
  }

  override fun encodeDouble(value: Double) {
    TODO("Not yet implemented")
  }

  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
    TODO("Not yet implemented")
  }

  override fun encodeFloat(value: Float) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
    TODO("Not yet implemented")
  }

  override fun encodeInt(value: Int) {
    TODO("Not yet implemented")
  }

  override fun encodeLong(value: Long) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun encodeNotNullMark() {
    super.encodeNotNullMark()
  }

  @ExperimentalSerializationApi
  override fun encodeNull() {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableValue(serializer: SerializationStrategy<T>, value: T?) {
    super.encodeNullableSerializableValue(serializer, value)
  }

  override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
    super.encodeSerializableValue(serializer, value)
  }

  override fun encodeShort(value: Short) {
    TODO("Not yet implemented")
  }

  override fun encodeString(value: String) {
    TODO("Not yet implemented")
  }
}

public class StreamingXmlEncoder(
  private val result: StringBuilder,
  override val xml: Xml
) : XmlEncoder {
  override val serializersModule: SerializersModule = xml.serializersModule

  private val children = mutableListOf<XmlElementEncoder>()

  override fun toString(): String {
    children.first().write(result)
    return result.toString()
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
    return XmlElementEncoder(this, descriptor).also { children.add(it) }
  }

  override fun endStructure(descriptor: SerialDescriptor) {
//    result.append("</${descriptor.serialName}>")
  }


  override fun encodeXmlEntity(entity: XmlEntity) {
    encodeSerializableValue(XmlEntitySerializer, entity)
  }

  override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
    TODO("Not yet implemented")
  }

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
    TODO("Not yet implemented")
  }

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
    TODO("Not yet implemented")
  }

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
    TODO("Not yet implemented")
  }

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
    TODO("Not yet implemented")
  }

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
    TODO("Not yet implemented")
  }

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?
  ) {
    TODO("Not yet implemented")
  }

  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T
  ) {
    TODO("Not yet implemented")
  }

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
    TODO("Not yet implemented")
  }

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
    result.append("string: $value")
  }

  @ExperimentalSerializationApi
  override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean {
    return super.shouldEncodeElementDefault(descriptor, index)
  }

  override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
    return super.beginCollection(descriptor, collectionSize)
  }






  override fun encodeBoolean(value: Boolean) {
    TODO("Not yet implemented")
  }

  override fun encodeByte(value: Byte) {
    TODO("Not yet implemented")
  }

  override fun encodeChar(value: Char) {
    TODO("Not yet implemented")
  }

  override fun encodeDouble(value: Double) {
    TODO("Not yet implemented")
  }

  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
    TODO("Not yet implemented")
  }

  override fun encodeFloat(value: Float) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
    TODO("Not yet implemented")
  }

  override fun encodeInt(value: Int) {
    TODO("Not yet implemented")
  }

  override fun encodeLong(value: Long) {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun encodeNotNullMark() {
    super.encodeNotNullMark()
  }

  @ExperimentalSerializationApi
  override fun encodeNull() {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableValue(serializer: SerializationStrategy<T>, value: T?) {
    super.encodeNullableSerializableValue(serializer, value)
  }

  override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
    super.encodeSerializableValue(serializer, value)
  }

  override fun encodeShort(value: Short) {
    TODO("Not yet implemented")
  }

  override fun encodeString(value: String) {
    TODO("Not yet implemented")
  }
}
