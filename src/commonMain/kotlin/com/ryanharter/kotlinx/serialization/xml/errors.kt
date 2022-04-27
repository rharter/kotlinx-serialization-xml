package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.SerializationException

public open class XmlSerializationException(message: String? = null, cause: Throwable? = null) :
  SerializationException(message, cause)

public class UndefinedNamespaceException
internal constructor(message: String?, cause: Throwable?) : XmlSerializationException(message, cause) {
  public constructor(name: String) : this("Namespace '$name' used, but no definition found.", null)
}