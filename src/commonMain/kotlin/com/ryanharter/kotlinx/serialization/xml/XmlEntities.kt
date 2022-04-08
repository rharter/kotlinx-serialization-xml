package com.ryanharter.kotlinx.serialization.xml

public sealed interface XmlEntity
public sealed interface XmlContentEntity : XmlEntity

public data class XmlDocument(
  public val root: XmlElement
) : XmlEntity

public data class XmlValue(
  public val value: String
) : XmlContentEntity

public data class XmlElement(
  public val name: String,
  public val attributes: List<XmlAttribute> = emptyList(),
  public val content: List<XmlContentEntity> = emptyList(),
) : XmlContentEntity

public data class XmlAttribute(
  val name: String,
  val value: String,
) : XmlEntity

public data class XmlComment(
  val value: String,
) : XmlContentEntity