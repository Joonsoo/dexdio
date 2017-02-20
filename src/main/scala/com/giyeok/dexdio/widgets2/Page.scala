package com.giyeok.dexdio.widgets2

sealed trait Page {
    val startLine: Int
    val endLine: Int
}

case class RootPage(startLine: Int, endLine: Int) extends Page
case class NonLeafPage(parent: Page, startLine: Int, endLine: Int) extends Page
case class LeafPage(parent: Page, startLine: Int, endLine: Int) extends Page
