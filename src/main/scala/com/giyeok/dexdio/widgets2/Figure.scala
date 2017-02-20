package com.giyeok.dexdio.widgets2

import java.util.concurrent.atomic.AtomicLong
import org.eclipse.swt.graphics.Image

// Label은 반드시 직사각형의 영역을 차지한다

trait Tag

sealed trait Figure {
    val id: Long = Figure.newId()
    val tags: Set[Tag]

    private[widgets2] var figureExtra = new FigureExtra(this)

    def estimateHeight(dc: DrawingContext): Long
    private[widgets2] def updateExtraHeight(height: Long): Long = { figureExtra.estimatedHeight = height; height }
    private[widgets2] def estimateHeightExtra(dc: DrawingContext): Long
}
object Figure {
    private val counter = new AtomicLong()
    private def newId(): Long = counter.incrementAndGet()
}
sealed trait FigureNoTags extends Figure {
    val tags = Set()
}

sealed trait Label extends Figure {
    private[widgets2] var labelExtra = new LabelExtra(this)

    def estimateHeight(dc: DrawingContext): Long =
        measureDimension(dc).height

    private[widgets2] def estimateHeightExtra(dc: DrawingContext): Long =
        updateExtraHeight(estimateHeight(dc))

    def measureDimension(dc: DrawingContext): Dimension
}
case class TextLabel(text: String, deco: TextDecoration, tags: Set[Tag]) extends Label {
    def measureDimension(dc: DrawingContext): Dimension =
        dc.textExtent(text, deco)
}
case class ImageLabel(image: Image, tags: Set[Tag]) extends Label {
    def measureDimension(dc: DrawingContext): Dimension = {
        Dimension(image.getImageData.width, image.getImageData.height)
    }
}
case class SpacingLabel(pixelWidth: Int, spaceCount: Int) extends Label {
    val tags = Set()

    def measureDimension(dc: DrawingContext): Dimension = {
        val spaceDim = dc.charSizeMap(' ')
        Dimension(pixelWidth + spaceDim.width * spaceCount, dc.standardLineHeight)
    }
}
// TODO ColumnRight 추가
case class ColumnSep() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): Dimension = Dimension(0, 0)
}
case class NewLine() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): Dimension = Dimension(0, 0)
}

case class Container(children: Seq[Figure], tags: Set[Tag]) extends Figure {
    private[widgets2] var containerExtra = new ContainerExtra(this)

    def estimateHeight(dc: DrawingContext): Long = {
        val x = children.foldLeft((0L, 0L)) { (cc, figure) =>
            val (all, currLine) = cc
            if (figure.isInstanceOf[NewLine]) {
                (all + currLine, 0L)
            } else {
                (all, Math.max(currLine, figure.estimateHeight(dc)))
            }
        }
        x._1
    }

    override private[widgets2] def estimateHeightExtra(dc: DrawingContext): Long = {
        val x = children.foldLeft((0L, 0L)) { (cc, child) =>
            val (all, currLine) = cc
            if (child.isInstanceOf[NewLine]) {
                (all + currLine, 0L)
            } else {
                val newCurrLine = Math.max(currLine, child.estimateHeightExtra(dc))
                (all, newCurrLine)
            }
        }
        updateExtraHeight(x._1 + x._2)
    }
}

case class Indented(content: Figure) extends FigureNoTags {
    def estimateHeight(dc: DrawingContext): Long =
        Math.max(content.estimateHeight(dc), dc.standardLineHeight)

    private[widgets2] def estimateHeightExtra(dc: DrawingContext) =
        updateExtraHeight(estimateHeight(dc))
}

trait Deferred extends FigureNoTags {
    private[widgets2] var deferredExtra = new DeferredExtra(this)

    def content: Figure = deferredExtra.content

    def contentFunc: Figure
    def estimateHeight(dc: DrawingContext): Long
    def estimateHeightExtra(dc: DrawingContext): Long =
        updateExtraHeight(estimateHeight(dc))
}
object Deferred {
    def apply(contentFunc: => Figure): Deferred = {
        val func: () => Figure = () => contentFunc

        new Deferred {
            override def estimateHeight(dc: DrawingContext) = 100L
            override def contentFunc = {
                println("Deferred called")
                func()
            }
        }
    }
}
case class Actionable(content: Figure) extends FigureNoTags {
    def estimateHeight(dc: DrawingContext): Long =
        content.estimateHeight(dc)

    def estimateHeightExtra(dc: DrawingContext): Long =
        updateExtraHeight(estimateHeight(dc))
}
case class Transformable(contents: Map[String, Figure], defaultState: String) extends FigureNoTags {
    private var currentState: String = defaultState

    def state: String = currentState

    def state_(newState: String): Unit = {
        currentState = newState
    }

    def content: Figure = contents(currentState)

    def estimateHeight(dc: DrawingContext): Long =
        content.estimateHeight(dc)

    def estimateHeightExtra(dc: DrawingContext): Long =
        updateExtraHeight(estimateHeight(dc))
}
