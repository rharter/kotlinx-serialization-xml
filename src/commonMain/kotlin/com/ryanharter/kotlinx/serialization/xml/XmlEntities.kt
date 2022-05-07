package com.ryanharter.kotlinx.serialization.xml

public sealed interface XmlEntity {
  public sealed interface ContentEntity : XmlEntity

  public data class Document(
    public val root: Element
  ) : XmlEntity

  public data class Value(
    public val value: String
  ) : ContentEntity

  public data class Element(
    public val name: String,
    public val namespace: String?,
    public val attributes: List<Attribute> = emptyList(),
    public val content: List<ContentEntity> = emptyList(),
  ) : ContentEntity

  public data class Attribute(
    public val name: String,
    public val value: String,
    public val prefix: String? = null
  ) : XmlEntity

  public data class Comment(
    val value: String,
  ) : ContentEntity
}