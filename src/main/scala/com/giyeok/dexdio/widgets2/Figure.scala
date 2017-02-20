package com.giyeok.dexdio.widgets2

import java.util.concurrent.atomic.AtomicLong
import org.eclipse.swt.graphics.Image

sealed trait FlatFigure
case class FigureLabel(label: Label) extends FlatFigure
case class FigurePush(figure: Figure) extends FlatFigure
case class FigurePop(figure: Figure) extends FlatFigure

sealed trait Figure {
    val id: Long = Figure.newId()
    val tags: Set[String]

    private[widgets2] var extra = new FigureExtra(this)

    def estimateDimension(dc: DrawingContext): Dimension
    private[widgets2] def updateExtraDimension(dim: Dimension): Dimension = {
        extra.estimatedDimension = dim
        dim
    }
    private[widgets2] def estimateDimensionExtra(dc: DrawingContext): Dimension =
        updateExtraDimension(estimateDimension(dc))

    def flatten: Stream[FlatFigure] = {
        def traverse(figure: Figure): Stream[FlatFigure] =
            figure match {
                case label: Label => Stream(FigureLabel(label))
                case container @ Container(children, _) =>
                    (FigurePush(container) #:: (children.toStream flatMap traverse)) append Stream(FigurePop(container))
                case indented @ Indented(content) =>
                    (FigurePush(indented) #:: traverse(content)) append Stream(FigurePop(indented))
                case deferred: Deferred =>
                    (FigurePush(deferred) #:: traverse(deferred.content)) append Stream(FigurePop(deferred))
                case actionable @ Actionable(content) =>
                    (FigurePush(actionable) #:: traverse(content)) append Stream(FigurePop(actionable))
                case transformable: Transformable =>
                    (FigurePush(transformable) #:: traverse(transformable.content)) append Stream(FigurePop(transformable))
            }
        traverse(this)
    }
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
case class SpacingLabel(pixelWidth: Int, spaceCount: Int) extends Label {
    val tags = Set()

    def measureDimension(dc: DrawingContext): Dimension = {
        val spaceDim = dc.charSizeMap(' ')
        Dimension(pixelWidth + spaceDim.width * spaceCount, spaceDim.height)
    }
}
case class ColumnSep() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): Dimension = Dimension(0, 0)
}
case class NewLine() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): Dimension = Dimension(0, 0)
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

case class Indented(content: Figure) extends FigureNoTags {
    def estimateDimension(dc: DrawingContext): Dimension =
        content.estimateDimension(dc) + Dimension(dc.indentWidth, 0)
}

trait Deferred extends FigureNoTags {
    private var contentCache = Option.empty[Figure]
    def isContentSet: Boolean = contentCache.isDefined
    def clearCache(): Unit = { contentCache = None }
    def content: Figure = contentCache match {
        case Some(content) => content
        case None =>
            contentCache = Some(contentFunc)
            contentCache.get
    }

    def contentFunc: Figure
    def estimateDimension(dc: DrawingContext): Dimension
}
object Deferred {
    def apply(contentFunc: => Figure): Deferred = {
        val func: () => Figure = () => contentFunc

        new Deferred {
            override def estimateDimension(dc: DrawingContext) =
                Dimension(100, 100)
            override def contentFunc = {
                println("Deferred called")
                func()
            }
        }
    }
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
