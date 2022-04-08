package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlContent

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlName(public val name: String, public val namespace: String = "")
