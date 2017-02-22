package com.giyeok.dexdio.widgets2

import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.GC

sealed trait TextDecoration {
    def execute[T](gc: GC)(block: => T): T
}
case object TextNoDecoration extends TextDecoration {
    def execute[T](gc: GC)(block: => T): T = {
        // nothing to do
        block
    }
}
case class TextWithFont(font: Font) extends TextDecoration {
    def execute[T](gc: GC)(block: => T): T = {
        val prevFont = gc.getFont()
        gc.setFont(font)
        val result = block
        gc.setFont(prevFont)
        result
    }
}
