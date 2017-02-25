package com.giyeok.dexdio.widgets2

import java.util.concurrent.atomic.AtomicLong
import org.eclipse.swt.graphics.Image

// TODO Figure에서 줄 수 정보 얻어낼 수 있어야 함
// Label은 반드시 직사각형의 영역을 차지한다
// TODO figureExtra 구조에 대해서 더 고민하기 -> 현재 구조로는 Figure 객체를 여러 view에서 재사용이 불가능

trait Tag

sealed trait AbstractFigure {
    val id: Long = AbstractFigure.newId()
}
object AbstractFigure {
    private val counter = new AtomicLong()
    private def newId(): Long = counter.incrementAndGet()
}

case object RootFigure extends AbstractFigure
sealed trait Figure extends AbstractFigure {
    val tags: Set[Tag]

    private[widgets2] var figureExtra = new FigureExtra(this)
}
sealed trait FigureNoTags extends Figure {
    val tags = Set()
}

sealed trait Label extends Figure {
    def measureDimension(dc: DrawingContext): Dimension
}
case class TextLabel(text: String, deco: TextDecoration, tags: Set[Tag]) extends Label {
    def measureDimension(dc: DrawingContext): Dimension =
        dc.textExtent(text, deco)
}
case class ImageLabel(image: Image, tags: Set[Tag]) extends Label {
    def measureDimension(dc: DrawingContext): Dimension =
        Dimension(image.getImageData.width, image.getImageData.height)
}
case class SpacingLabel(pixelWidth: Int, spaceCount: Int) extends Label {
    val tags = Set()

    def widthInPixel(dc: DrawingContext): Int = {
        val spaceDim = dc.charSizeMap(' ')
        pixelWidth + spaceDim.width.toInt * spaceCount
    }
    def measureDimension(dc: DrawingContext): Dimension =
        Dimension(widthInPixel(dc), dc.standardLineHeight)
}
// TODO ColumnRight 추가
case class ColumnSep() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): Dimension =
        Dimension.zero
}

case class NewLine() extends Label with FigureNoTags {
    def measureDimension(dc: DrawingContext): Dimension =
        Dimension.zero
}

private[widgets2] case class Chunk(children: Seq[Figure]) extends FigureNoTags {
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
