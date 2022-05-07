package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Properties annotated with `XmlContent` are serialized as
 * text content, instead of elements or attributes.
 */
@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlContent

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
public annotation class XmlName(public val name: String = "")

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
public annotation class XmlDefaultNamespace(
  public val uri: String = ""
)

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
public annotation class XmlNamespace(
  /**
   * A namespace name, identified by a URI.
   */
  public val uri: String = "",
  /**
   * An optional local name for a namespace. This can be used
   * as the prefix in a qualified element or attribute name to
   * associate the element or attribute with the namespace.
   *
   * If no `localName` is provided, this namespace will be used as
   * the default namespace when annotating classes, otherwise
   * a localName will be generated.
   */
  public val localName: String = "",
)

/**
 * Properties annotated with `XmlAttribute` will be serialized as
 * attributes on an XML element.
 *
 * Properties annotated with `XmlAttribute` will be serialized as
 * a string, and must appear first in the serializable object.
 */
@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlAttribute
