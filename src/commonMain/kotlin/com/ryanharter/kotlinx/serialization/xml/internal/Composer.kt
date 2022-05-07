package com.ryanharter.kotlinx.serialization.xml.internal

internal interface Composer {
  fun indent(): Composer
  fun unindent(): Composer
  fun newElement(): Composer
  fun newAttribute(): Composer
  fun append(value: String): Composer
  fun appendLine(): Composer
}

internal class XmlComposer(private val sb: StringBuilder) : Composer {
  override fun indent(): Composer = this
  override fun unindent(): Composer = this
  override fun newElement(): Composer = this
  override fun newAttribute(): Composer = also { sb.append(" ") }
  override fun append(value: String): Composer = also { sb.append(value) }
  override fun appendLine() = this
}

internal class PrettyPrintXmlComposer(
  private val sb: StringBuilder,
  private val indent: Int = 2,
) : Composer {

  private var level = 0

  override fun indent(): Composer = also {
    level++
  }

  override fun unindent(): Composer = also {
    level--
  }

  override fun newElement(): Composer = also {
    appendLine()
  }

  override fun newAttribute(): Composer = also {
    appendLine().append(" ".repeat(indent))
  }

  override fun append(value: String): Composer = also { sb.append(value) }

  override fun appendLine(): Composer = also {
    sb.appendLine()
    sb.append(" ".repeat(level * indent))
  }
}
