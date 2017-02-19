package com.giyeok.dexdio.widgets2

import java.util.concurrent.atomic.AtomicLong
import org.eclipse.swt.graphics.Image

sealed trait Figure {
    val id: Long = Figure.newId()
    val tags: Set[String]

    private[widgets2] var extra = new FigureExtra(this)

    def estimateDimension(dc: DrawingContext): Dimension
    private[widgets2] def updateExtraDimension(dim: Dimension): Dimension = {
        extra.estimatedDimension = Some(dim)
        dim
    }
    private[widgets2] def estimateDimensionExtra(dc: DrawingContext): Dimension =
        updateExtraDimension(estimateDimension(dc))
}
object Figure {
    private val counter = new AtomicLong()
    private def newId(): Long = counter.incrementAndGet()
}
sealed trait FigureNoTags extends Figure {
    val tags = Set()
}

sealed trait Label extends Figure {
    def estimateDimension(dc: DrawingContext): Dimension =
        measureDimension(dc)

    def measureDimension(dc: DrawingContext): Dimension
}
case class TextLabel(text: String, deco: TextDecoration, tags: Set[String]) extends Label {
    def measureDimension(dc: DrawingContext): Dimension =
        dc.textExtent(text, deco)
}
case class ImageLabel(image: Image, tags: Set[String]) extends Label {
    def measureDimension(dc: DrawingContext): Dimension = {
        Dimension(image.getImageData.width, image.getImageData.height)
    }
}
case class SpaceLabel(width: Int) extends Label {
    val tags = Set()

    def measureDimension(dc: DrawingContext): Dimension = {
        Dimension(width, 0)
    }
}
case class Container(children: Seq[Figure], tags: Set[String]) extends Figure {
    def estimateDimension(dc: DrawingContext): Dimension = {
        val x = children.foldLeft((Dimension(0, 0), Dimension(0, 0))) { (cc, figure) =>
            val (all, currLineSize) = cc
            if (figure.isInstanceOf[NewLine]) {
                (all addBottom currLineSize, Dimension(0, 0))
            } else {
                (all, currLineSize addRight figure.estimateDimension(dc))
            }
        }
        x._1 addBottom x._2
    }

    override private[widgets2] def estimateDimensionExtra(dc: DrawingContext): Dimension = {
        val x = children.foldLeft((Dimension(0, 0), Dimension(0, 0))) { (cc, figure) =>
            val (all, currLineSize) = cc
            if (figure.isInstanceOf[NewLine]) {
                (all addBottom currLineSize, Dimension(0, 0))
            } else {
                (all, currLineSize addRight figure.estimateDimensionExtra(dc))
            }
        }
        updateExtraDimension(x._1 addBottom x._2)
    }
}

case class ColumnSep() extends FigureNoTags {
    def estimateDimension(dc: DrawingContext): Dimension = Dimension(0, 0)
}
case class NewLine() extends FigureNoTags {
    def estimateDimension(dc: DrawingContext): Dimension = Dimension(0, 0)
}
case class Indented(content: Figure) extends FigureNoTags {
    def estimateDimension(dc: DrawingContext): Dimension =
        content.estimateDimension(dc) + Dimension(dc.indentWidth, 0)
}

trait Deferred extends FigureNoTags {
    private var contentOpt = Option.empty[Figure]
    def isContentSet: Boolean = contentOpt.isDefined
    def content: Figure = contentOpt match {
        case Some(content) => content
        case None =>
            contentOpt = Some(contentFunc)
            contentOpt.get
    }

    def contentFunc: Figure
    def estimateDimension(dc: DrawingContext): Dimension
}
case class Actionable(content: Figure) extends FigureNoTags {
    def estimateDimension(dc: DrawingContext): Dimension =
        content.estimateDimension(dc)
}
case class Transformable(contents: Map[String, Figure], defaultState: String) extends FigureNoTags {
    private var currentState: String = defaultState
    def state: String = currentState
    def state_(newState: String): Unit = { currentState = newState }

    def content: Figure = contents(currentState)

    def estimateDimension(dc: DrawingContext): Dimension =
        content.estimateDimension(dc)
}
