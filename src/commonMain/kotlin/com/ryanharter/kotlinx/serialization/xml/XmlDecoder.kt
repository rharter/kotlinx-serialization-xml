package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.encoding.Decoder

public interface XmlDecoder : Decoder {
  public val xml: Xml
  public fun decodeXmlEntity(): XmlEntity
}