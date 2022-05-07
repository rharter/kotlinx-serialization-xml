package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder

public interface XmlEncoder : Encoder, CompositeEncoder {
  public val xml: Xml
  public fun encodeXmlEntity(entity: XmlEntity)
}