package com.giyeok.dexdio.widgets2

import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.GC

case class DrawingConfig(
    indentWidth: Int,
    defaultFont: Font
)

object DrawingContext {
    private var charSizeMapCache = Option.empty[Map[Char, Dimension]]
    private def charSizeMap(gc: GC, conf: DrawingConfig): Map[Char, Dimension] = {
        charSizeMapCache match {
            case Some(map) => map
            case None =>
                val chars = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.~`!\\|!@#$%^&*()_+-=,./<>?"
                gc.setFont(conf.defaultFont)
                val map = (chars map { c =>
                    val d = gc.textExtent("" + c)
                    c -> Dimension(d.x, d.y)
                }).toMap
                charSizeMapCache = Some(map)
                map
        }
    }
}

case class DrawingContext(gc: GC, conf: DrawingConfig) {
    val indentWidth: Int = conf.indentWidth

    lazy val charSizeMap: Map[Char, Dimension] =
        DrawingContext.charSizeMap(gc, conf)
    lazy val standardLineHeight: Int = (charSizeMap map { _._2.height }).max.toInt

    def textExtent(text: String, deco: TextDecoration): Dimension = {
        deco.applyTo(gc)
        val dim = gc.textExtent(text)
        Dimension(dim.x, dim.y)
    }
}
