package com.ryanharter.kotlinx.serialization.xml

import com.ryanharter.kotlinx.serialization.xml.internal.StreamingXmlDecoder
import com.ryanharter.kotlinx.serialization.xml.internal.StreamingXmlEncoder
import com.ryanharter.kotlinx.serialization.xml.internal.XmlComposer
import com.ryanharter.kotlinx.serialization.xml.internal.XmlLexer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.native.concurrent.ThreadLocal

public sealed class Xml(
  override val serializersModule: SerializersModule,
) : StringFormat {

  @ThreadLocal
  public companion object Default : Xml(EmptySerializersModule)

  override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
    val sb = StringBuilder()
    val composer = XmlComposer(sb)
    val encoder = StreamingXmlEncoder(this, composer)
    serializer.serialize(encoder, value)
    return sb.toString()
  }

  override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
    val lexer = XmlLexer(string)
    val input = StreamingXmlDecoder(this, lexer)
    return input.decodeSerializableValue(deserializer)
  }
}