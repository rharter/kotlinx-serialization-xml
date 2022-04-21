package com.ryanharter.kotlinx.serialization.xml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlContent

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlName(public val name: String = "")

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlNamespace(public val uri: String = "")

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class XmlAttribute()