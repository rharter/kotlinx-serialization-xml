@file:OptIn(ExperimentalSerializationApi::class)

package com.ryanharter.kotlinx.serialization.xml.internal

import com.ryanharter.kotlinx.serialization.xml.XmlAttribute
import com.ryanharter.kotlinx.serialization.xml.XmlDecoder
import com.ryanharter.kotlinx.serialization.xml.XmlEncoder
import com.ryanharter.kotlinx.serialization.xml.XmlEntity
import com.ryanharter.kotlinx.serialization.xml.XmlValue
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
      is XmlAttribute -> encoder.encodeSerializableValue(XmlAttributeSerializer, value)
      is XmlValue -> encoder.encodeSerializableValue(XmlValueSerializer, value)
    }
  }

  override fun deserialize(decoder: Decoder): XmlEntity {
    val input = decoder.asXmlDecoder()
    return input.decodeXmlEntity()
  }

}

@Serializer(forClass = XmlAttribute::class)
@PublishedApi
internal object XmlAttributeSerializer : KSerializer<XmlAttribute> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("com.ryanharter.kotlinx.serialization.xml.XmlAttribute", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: XmlAttribute) {
    verify(encoder)
    PairSerializer(String.serializer(), String.serializer()).serialize(encoder, value.name to value.value)
  }

  override fun deserialize(decoder: Decoder): XmlAttribute {
    val result = decoder.asXmlDecoder().decodeXmlEntity()
    if (result !is XmlAttribute) throw IllegalArgumentException("Unexpected XML entity, expected XmlAttribute, got ${result::class}")
    return result
  }

}

private object XmlValueSerializer : KSerializer<XmlValue> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("com.ryanharter.kotlinx.serialization.xml.XmlValue", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: XmlValue) {
    verify(encoder)
    encoder.encodeString(value.value)
  }

  override fun deserialize(decoder: Decoder): XmlValue {
    val result = decoder.asXmlDecoder().decodeXmlEntity()
    if (result !is XmlValue) throw IllegalArgumentException("Unexpected XML entity, expected XmlValue, got ${result::class}")
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
