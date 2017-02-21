package com.giyeok.dexdio.widgets2

import java.util.concurrent.atomic.AtomicLong
import com.giyeok.dexdio.widgets2.FlatFigureStream._
import org.eclipse.swt.graphics.Image

// Label은 반드시 직사각형의 영역을 차지한다

trait Tag

sealed trait Figure {
    val id: Long = Figure.newId()
    val tags: Set[Tag]

    private[widgets2] var figureExtra = new FigureExtra(this)

    def updateExtraDimension(dimension: FigureDimension): FigureDimension = {
        figureExtra.estimatedDimension = Some(dimension)
        dimension
    }
    def estimateDimensionExtra(dc: DrawingContext): FigureDimension
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

    def estimateDimensionExtra(dc: DrawingContext): FigureDimension = {
        updateExtraDimension(measureDimension(dc))
    }

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

    def measureDimension(dc: DrawingContext): FigureDimension = {
        val spaceDim = dc.charSizeMap(' ')
        FigureDimension(Dimension(pixelWidth + spaceDim.width * spaceCount, dc.standardLineHeight), None)
    }
}
// TODO ColumnRight 추가
case class ColumnSep() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): FigureDimension =
        FigureDimension(Dimension.zero, None)
}
case class NewLine() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): FigureDimension =
        FigureDimension(Dimension.zero, Some(0, Dimension.zero))
}

case class Container(children: Seq[Figure], tags: Set[Tag]) extends Figure {
    private[widgets2] var containerExtra = new ContainerExtra(this)

    def estimateDimensionExtra(dc: DrawingContext): FigureDimension = {
        val dimension = if (children.isEmpty) {
            FigureDimension(Dimension.zero, None)
        } else {
            val dims = children map { _.estimateDimensionExtra(dc) }
            val (leadingDims, restDims) = dims span { _.rest.isEmpty }
            val leading0 = (leadingDims map { _.leading }).foldLeft(Dimension.zero) { _.addRight(_) }
            if (restDims.isEmpty) {
                FigureDimension(leading0, None)
            } else {
                val restHead = restDims.head
                val leading = leading0 addRight restHead.leading
                val (exclusiveHeight, trailing) = restDims.tail.foldLeft((restHead.exclusiveHeight.get, restHead.trailing.get)) { (cc, dim) =>
                    val (exclusiveHeightCC, trailingCC) = cc
                    dim.rest match {
                        case Some((restExclusiveHeight, restTrailing)) =>
                            (exclusiveHeightCC + Math.max(trailingCC.height, dim.leading.height) + restExclusiveHeight,
                                restTrailing)
                        case None =>
                            (exclusiveHeightCC, trailingCC addRight dim.leading)
                    }
                }
                FigureDimension(leading, Some(exclusiveHeight, trailing))
            }
        }
        updateExtraDimension(dimension)
    }
}

// Indented는 자동으로 위아래 NewLine이 추가된다
case class Indented(content: Figure) extends FigureNoTags {
    def estimateDimensionExtra(dc: DrawingContext): FigureDimension = {
        val contentDimension = content.estimateDimensionExtra(dc)
        updateExtraDimension(FigureDimension(Dimension.zero, Some(contentDimension.totalHeight, Dimension.zero)))
    }
}

trait Deferred extends FigureNoTags {
    private[widgets2] var deferredExtra = new DeferredExtra(this)

    def content: Figure = deferredExtra.content

    def contentFunc: Figure

    // WARN estimateDimensionExtra는 사용시 updateExtraDimension를 빠뜨리지 않도록 특별히 주의해서 잘 작성해야 함
    def estimateDimensionExtra(dc: DrawingContext): FigureDimension
}
object Deferred {
    def apply(contentFunc: => Figure): Deferred = {
        val func: () => Figure = () => contentFunc

        new Deferred {
            override def estimateDimensionExtra(dc: DrawingContext): FigureDimension =
                updateExtraDimension(FigureDimension(Dimension(100, 100), None))
            override def contentFunc: Figure = {
                println("Deferred called")
                func()
            }
        }
    }
}
case class Actionable(content: Figure) extends FigureNoTags {
    def estimateDimensionExtra(dc: DrawingContext): FigureDimension =
        updateExtraDimension(content.estimateDimensionExtra(dc))
}
case class Transformable(contents: Map[String, Figure], defaultState: String) extends FigureNoTags {
    private var currentState: String = defaultState

    def state: String = currentState

    def state_(newState: String): Unit = {
        currentState = newState
    }

    def content: Figure = contents(currentState)

    def estimateDimensionExtra(dc: DrawingContext): FigureDimension =
        updateExtraDimension(content.estimateDimensionExtra(dc))
}
