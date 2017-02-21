package com.giyeok.dexdio.widgets2

import scala.collection.immutable.IndexedSeq

case class FigureDimension(leading: Dimension, rest: Option[(Long, Dimension)]) {
    def exclusiveHeight: Option[Long] = rest map { _._1 }
    def trailing: Option[Dimension] = rest map { _._2 }
    def totalHeight: Long = leading.height + (rest map { r => r._1 + r._2.height }).getOrElse(0L)
}

private class FigureExtra(figure: Figure) {
    var estimatedDimension = Option.empty[FigureDimension]
    // coord는 ContainerExtra.prepareForFirstTime에서 셋팅되고 FigureTreeView.planRender에서 사용되는데,
    // planRender는 반드시 prepareForFirstTime이 호출된 이후에 실행되므로 사용되는 시점에 coord가 null이어서는 안된다
    var coord: AnchorCoord = _
}

private class LabelExtra(label: Label) {
    var measuredWidth: Long = 0L
}

class Anchor(defaultPosition: Point) {
    private var _position: Point = defaultPosition
    def position: Point = _position

    private var referer = List[AnchorCoord]()
    def appendReferer(coord: AnchorCoord): Unit = {
        referer +:= coord
    }

    var estimatedDimension = Option.empty[FigureDimension]
}

class AnchorCoord(anchor: Anchor, defaultRelative: Point) {
    anchor.appendReferer(this)

    private var _relative: Point = defaultRelative
    def relative: Point = _relative

    def position: Point = anchor.position + relative
}

private class ContainerExtra(container: Container) {
    // TODO anchors
    private val anchorElemsMax = 1000
    private val anchorsCounts = (container.children.size / anchorElemsMax) + 1
    val anchors: IndexedSeq[Anchor] = (0 until anchorsCounts) map { _ => new Anchor(Point(0, 0)) }

    def prepareForTheFirstTime(dc: DrawingContext): Unit = {
        println("prepareForTheFirstTime")
        container.estimateDimensionExtra(dc)

        for ((figures, anchor) <- container.children.grouped(container.children.size / anchorsCounts).toSeq zip anchors) {
        }
        // TODO anchor 셋팅, coord 셋팅
        //???
    }
}

object DeferredExtra {
    // TODO LRU 형태로 메모리를 너무 많이 차지하게 되면 contentCache 날리는 기능 추가
}

private class DeferredExtra(deferred: Deferred) {
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
