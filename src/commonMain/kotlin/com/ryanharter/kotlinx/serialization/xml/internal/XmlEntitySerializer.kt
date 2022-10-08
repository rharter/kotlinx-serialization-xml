@file:OptIn(ExperimentalSerializationApi::class)

package com.ryanharter.kotlinx.serialization.xml.internal

import com.ryanharter.kotlinx.serialization.xml.XmlDecoder
import com.ryanharter.kotlinx.serialization.xml.XmlEncoder
import com.ryanharter.kotlinx.serialization.xml.XmlEntity
import com.ryanharter.kotlinx.serialization.xml.XmlEntity.Attribute
import com.ryanharter.kotlinx.serialization.xml.XmlEntity.Value
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = XmlEntity::class)
@PublishedApi
internal object XmlEntitySerializer : KSerializer<XmlEntity> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("com.ryanharter.kotlinx.serialization.xml.XmlEntity") {
      element("XmlValue", XmlValueSerializer.descriptor)
      element("XmlAttribute", XmlAttributeSerializer.descriptor)
    }

  override fun serialize(encoder: Encoder, value: XmlEntity) {
    verify(encoder)
    when (value) {
      is Attribute -> encoder.encodeSerializableValue(XmlAttributeSerializer, value)
      is Value -> encoder.encodeSerializableValue(XmlValueSerializer, value)
      else -> {} // no op
    }
  }

  override fun deserialize(decoder: Decoder): XmlEntity {
    val input = decoder.asXmlDecoder()
    return input.decodeXmlEntity()
  }

}

@Serializer(forClass = Attribute::class)
@PublishedApi
internal object XmlAttributeSerializer : KSerializer<Attribute> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor(
      "com.ryanharter.kotlinx.serialization.xml.XmlEntity.XmlAttribute",
      PrimitiveKind.STRING
    )

  override fun serialize(encoder: Encoder, value: Attribute) {
    verify(encoder)
    PairSerializer(String.serializer(), String.serializer()).serialize(
      encoder,
      value.name to value.value
    )
  }

  override fun deserialize(decoder: Decoder): Attribute {
    val result = decoder.asXmlDecoder().decodeXmlEntity()
    if (result !is Attribute) throw IllegalArgumentException("Unexpected XML entity, expected XmlAttribute, got ${result::class}")
    return result
  }

}

private object XmlValueSerializer : KSerializer<Value> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor(
      "com.ryanharter.kotlinx.serialization.xml.XmlValue",
      PrimitiveKind.STRING
    )

  override fun serialize(encoder: Encoder, value: Value) {
    verify(encoder)
    encoder.encodeString(value.value)
  }

  override fun deserialize(decoder: Decoder): Value {
    val result = decoder.asXmlDecoder().decodeXmlEntity()
    if (result !is Value) throw IllegalArgumentException("Unexpected XML entity, expected XmlValue, got ${result::class}")
    return result
  }

}

private fun verify(decoder: Decoder) {
  decoder.asXmlDecoder()
}

private fun verify(encoder: Encoder) {
  encoder.asXmlEncoder()
}

internal fun Decoder.asXmlDecoder() = this as? XmlDecoder
  ?: throw IllegalStateException(
    "This serializer can be used only with Xml format. " +
      "Expected Decoder to be XmlDecoder, got ${this::class}"
  )

internal fun Encoder.asXmlEncoder() = this as? XmlEncoder
  ?: throw IllegalStateException(
    "This serializer can be used only with Xml format. " +
      "Expected Encoder to be XmlEncoder, got ${this::class}"
  )
