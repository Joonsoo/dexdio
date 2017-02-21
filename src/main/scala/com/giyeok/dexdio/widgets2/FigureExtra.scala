package com.giyeok.dexdio.widgets2

import scala.collection.immutable.IndexedSeq

case class FigureDimension(leading: Dimension, rest: Option[(Long, Dimension)]) {
    def exclusiveHeight: Option[Long] = rest map { _._1 }
    def trailing: Option[Dimension] = rest map { _._2 }
    def totalHeight: Long = leading.height + (rest map { r => r._1 + r._2.height }).getOrElse(0L)
}

case class RenderingPoint(leftTop: Point, lineMax: Long) {
    def top: Long = leftTop.y
    def left: Long = leftTop.x

    def proceed(dimension: FigureDimension): RenderingPoint =
        dimension.rest match {
            case Some(rest) =>
                RenderingPoint(
                    Point(rest._2.width, top + Math.max(lineMax, dimension.leading.height) + rest._1),
                    rest._2.height
                )
            case None =>
                RenderingPoint(
                    Point(left + dimension.leading.width, top),
                    Math.max(lineMax, dimension.leading.height)
                )
        }
}

private class FigureExtra(figure: Figure) {
    var estimatedDimension: FigureDimension = _
    // coord는 ContainerExtra.prepareForFirstTime에서 셋팅되고 FigureTreeView.planRender에서 사용되는데,
    // planRender는 반드시 prepareForFirstTime이 호출된 이후에 실행되므로 사용되는 시점에 coord가 null이어서는 안된다
    var estimatedCoord: AnchorCoord = _

    implicit final class MyEnsuring[A](private val self: A) {
        def ensuringEquals(other: A): A = {
            assert(self == other)
            self
        }
    }

    def updateLayout(anchor: Anchor, p: RenderingPoint, dimension: FigureDimension): RenderingPoint = {
        estimatedDimension = dimension
        estimatedCoord = new AnchorCoord(anchor, Point(p.left, p.top) - anchor.position)
        p.proceed(dimension)
    }
    def estimateLayout(dc: DrawingContext, anchor: Anchor, p: RenderingPoint, indent: Int): RenderingPoint = {
        figure match {
            case NewLine() =>
                updateLayout(anchor, p, NewLine.dimension(indent * dc.indentWidth))
            case label: Label =>
                updateLayout(anchor, p, label.measureDimension(dc))
            case container @ Container(children, _) =>
                if (children.isEmpty) {
                    updateLayout(anchor, p, FigureDimension(Dimension.zero, None))
                } else {
                    // TODO anchor 말고 container.containerExtra에서 배정한 sub anchor로 하도록 수정
                    val p0 = children.foldLeft(p) { (m, i) => i.figureExtra.estimateLayout(dc, anchor, m, indent) }
                    val dims = children map { _.figureExtra.estimatedDimension }
                    val (leadingDims, restDims) = dims span { _.rest.isEmpty }
                    val leading0 = (leadingDims map { _.leading }).foldLeft(Dimension.zero) { _.addRight(_) }
                    val dimension = if (restDims.isEmpty) {
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
                    updateLayout(anchor, p, dimension) ensuringEquals p0
                }
            case Indented(content) =>
                val newLinePoint = p.proceed(NewLine.dimension((indent + 1) * dc.indentWidth))
                content.figureExtra.estimateLayout(dc, anchor, newLinePoint, indent + 1)
                updateLayout(anchor, p, FigureDimension(Dimension.zero, Some(content.figureExtra.estimatedDimension.totalHeight, Dimension.zero)))
            case deferred: Deferred =>
                deferred.deferredExtra.indent = indent
                if (!deferred.deferredExtra.isContentSet) {
                    updateLayout(anchor, p, deferred.estimateDimension(dc))
                } else {
                    val p0 = deferred.content.figureExtra.estimateLayout(dc, anchor, p, indent)
                    updateLayout(anchor, p, deferred.content.figureExtra.estimatedDimension) ensuringEquals p0
                }
            case Actionable(content) =>
                val p0 = content.figureExtra.estimateLayout(dc, anchor, p, indent)
                updateLayout(anchor, p, content.figureExtra.estimatedDimension) ensuringEquals p
            case transformable: Transformable =>
                // transformable.content.figureExtra.estimateLayout()
                val content = transformable.content
                val p0 = content.figureExtra.estimateLayout(dc, anchor, p, indent)
                updateLayout(anchor, p, content.figureExtra.estimatedDimension) ensuringEquals p0
        }
    }
}

sealed trait Anchor {
    def position: Point

    private var refererAnchors = List[Anchor]()
    def appendReferer(anchor: Anchor): Unit = {
        refererAnchors +:= anchor
    }

    private var refererCoords = List[AnchorCoord]()
    def appendReferer(coord: AnchorCoord): Unit = {
        refererCoords +:= coord
    }
}
object AnchorRoot extends Anchor {
    def position: Point = Point.zero
}
class AnchorBranch(parent: Anchor, initialPosition: Point) extends Anchor {
    parent.appendReferer(this)

    private var _position: Point = initialPosition
    def position: Point = _position
}

class AnchorCoord(val anchor: Anchor, initialRelative: Point) {
    anchor.appendReferer(this)

    private var _relative = initialRelative
    def relative: Point = _relative

    def position: Point = anchor.position + relative
}

private class ContainerExtra(container: Container) {
    private val anchorElemsMax = 1000
    private val anchorsCounts = (container.children.size / anchorElemsMax) + 1
    // Anchor의 최초 위치는 무관함 - 0, 0으로 고정해서 사용해도 상관 없음
    private var anchors: IndexedSeq[Anchor] = _

    def prepareAnchors(parentAnchor: Anchor, dc: DrawingContext): Unit = {
        anchors = (0 until anchorsCounts) map { _ => new AnchorBranch(parentAnchor, Point.zero) }

        for ((figures, anchor) <- container.children.grouped(container.children.size / anchorsCounts).toSeq zip anchors) {
            // anchor 셋팅
            figures foreach { _.figureExtra.estimatedCoord = new AnchorCoord(anchor, Point.zero) }
        }
    }
}

object DeferredExtra {
    // TODO LRU 형태로 메모리를 너무 많이 차지하게 되면 contentCache 날리는 기능 추가
}

private class DeferredExtra(deferred: Deferred) {
    var indent: Int = 0

    private var contentCache = Option.empty[Figure]
    def isContentSet: Boolean = contentCache.isDefined
    def clearCache(): Unit = { contentCache = None }
    def content: Figure = contentCache match {
        case Some(content) => content
        case None =>
            contentCache = Some(deferred.contentFunc)
            contentCache.get
    }
}
