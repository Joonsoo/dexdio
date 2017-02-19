package com.giyeok.dexdio.widgets2

import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.GC

case class DrawingConfig(
    indentWidth: Int,
    defaultFont: Font
)

case class DrawingContext(gc: GC, conf: DrawingConfig) {
    val indentWidth: Int = conf.indentWidth

    lazy val charSizeMap: Map[Char, Dimension] = {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.~`!\\|!@#$%^&*()_+-=,./<>?"
        gc.setFont(conf.defaultFont)
        (chars map { c =>
            val d = gc.textExtent("" + c)
            c -> Dimension(d.x, d.y)
        }).toMap
    }
    lazy val standardLineHeight: Int = (charSizeMap map { _._2.height }).max.toInt

    def textExtent(text: String, deco: TextDecoration): Dimension = {
        deco.applyTo(gc)
        val dim = gc.textExtent(text)
        Dimension(dim.x, dim.y)
    }
}
