package com.giyeok.dexdio.widgets2

import org.eclipse.swt.graphics.GC

sealed trait TextDecoration {
    def applyTo(gc: GC): Unit
}
case object TextNoDecoration extends TextDecoration {
    def applyTo(gc: GC): Unit = {
        // nothing to do
    }
}
