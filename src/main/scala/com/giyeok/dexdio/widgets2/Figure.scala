package com.giyeok.dexdio.widgets2

import java.util.concurrent.atomic.AtomicLong
import org.eclipse.swt.graphics.Image

// Label은 반드시 직사각형의 영역을 차지한다

trait Tag

sealed trait Figure {
    val id: Long = Figure.newId()
    val tags: Set[Tag]

    private[widgets2] var figureExtra = new FigureExtra(this)
}
object Figure {
    private val counter = new AtomicLong()
    private def newId(): Long = counter.incrementAndGet()
}
sealed trait FigureNoTags extends Figure {
    val tags = Set()
}

sealed trait Label extends Figure {
    def measureDimension(dc: DrawingContext): FigureDimension
}
case class TextLabel(text: String, deco: TextDecoration, tags: Set[Tag]) extends Label {
    def measureDimension(dc: DrawingContext): FigureDimension =
        FigureDimension(dc.textExtent(text, deco), None)
}
case class ImageLabel(image: Image, tags: Set[Tag]) extends Label {
    def measureDimension(dc: DrawingContext): FigureDimension = {
        FigureDimension(Dimension(image.getImageData.width, image.getImageData.height), None)
    }
}
case class SpacingLabel(pixelWidth: Int, spaceCount: Int) extends Label {
    val tags = Set()

    def widthInPixel(dc: DrawingContext): Int = {
        val spaceDim = dc.charSizeMap(' ')
        pixelWidth + spaceDim.width.toInt * spaceCount
    }
    def measureDimension(dc: DrawingContext): FigureDimension =
        FigureDimension(Dimension(widthInPixel(dc), dc.standardLineHeight), None)
}
// TODO ColumnRight 추가
case class ColumnSep() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): FigureDimension =
        FigureDimension(Dimension.zero, None)
}

object NewLine {
    def dimension(dc: DrawingContext, indentPixels: Int): FigureDimension =
        FigureDimension(Dimension.zero, Some(0, Dimension(indentPixels, dc.standardLineHeight)))
}
case class NewLine() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): FigureDimension =
        NewLine.dimension(dc, 0)
}

case class Container(children: Seq[Figure], tags: Set[Tag]) extends Figure {
    private[widgets2] var containerExtra = new ContainerExtra(this)
}

// Indented는 자동으로 위아래 NewLine이 추가된다
case class Indented(content: Figure) extends FigureNoTags

trait Deferred extends FigureNoTags {
    private[widgets2] var deferredExtra = new DeferredExtra(this)

    def content: Figure = deferredExtra.content

    def contentFunc: Figure
    def estimateDimension(dc: DrawingContext): FigureDimension
}
object Deferred {
    def apply(contentFunc: => Figure): Deferred = {
        val func: () => Figure = () => contentFunc

        new Deferred {
            override def estimateDimension(dc: DrawingContext): FigureDimension =
                FigureDimension(Dimension(100, 100), None)
            override def contentFunc: Figure = {
                println("Deferred called")
                func()
            }
        }
    }
}
case class Actionable(content: Figure) extends FigureNoTags

case class Transformable(contents: Map[String, Figure], defaultState: String) extends FigureNoTags {
    private var currentState: String = defaultState

    def state: String = currentState
    def state_(newState: String): Unit = {
        currentState = newState
    }

    def content: Figure = contents(currentState)
}
