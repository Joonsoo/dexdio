package com.giyeok.dexdio.widgets2

import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.events.PaintListener
import org.eclipse.swt.widgets.Canvas
import org.eclipse.swt.widgets.Composite

case class Point(x: Long, y: Long)
case class Dimension(width: Long, height: Long) {
    def +(diff: Dimension): Dimension =
        Dimension(width + diff.width, height + diff.height)
    def addRight(right: Dimension): Dimension =
        Dimension(width + right.width, Math.max(height, right.height))
    def addBottom(bottom: Dimension): Dimension =
        Dimension(Math.max(width, bottom.width), height + bottom.height)
}

private class FigureExtra(figure: Figure) {
    var parent: Option[Figure] = None
    var estimatedDimension = Option.empty[Dimension]
    // TODO 이 figure의 위치(base가 될 offset figure와 상대 거리)

    def prepare(dc: DrawingContext): Unit = {
        if (estimatedDimension.isEmpty) {
            estimatedDimension = Some(figure.estimateDimension(dc))
        }
    }

    def updateParents(): Unit = {
        // figure의 자식들의 parent를 업데이트해줌
        // 자기 parent는 건드리지 않음
        figure match {
            case _: Label => // nothing to do
            case container: Container =>
                container.children foreach { child =>
                    child.extra.parent = Some(figure)
                    child.extra.updateParents()
                }
            case _: ColumnSep | _: NewLine => // nothing to do
            case indented: Indented =>
                indented.content.extra.parent = Some(figure)
                indented.content.extra.updateParents()
            case deferred: Deferred if deferred.isContentSet =>
                deferred.content.extra.parent = Some(figure)
                deferred.content.extra.updateParents()
            case actionable: Actionable =>
                actionable.content.extra.parent = Some(figure)
                actionable.content.extra.updateParents()
            case transformable: Transformable =>
                transformable.content.extra.parent = Some(figure)
                transformable.content.extra.updateParents()
        }
    }

    def notifyDimensionChanged(newDimension: Dimension): Unit = {
        estimatedDimension = Some(newDimension)
        parent foreach { _.extra.notifyDimensionChanged(???) }
    }

    def draw(dc: DrawingContext, scroll: Point, bounds: Dimension): Unit = {
        // 전체 캔버스에서 이 figure가 그려져야 할 정확한 위치를 계산한 뒤
        // scroll을 빼서 그게 bounds 안에 있으면 그리고 아니면 끝
        ???
    }
}

class FigureTreeView(parent: Composite, style: Int, rootFigure: Figure, columns: Seq[(Figure, Int)], drawingConfig: DrawingConfig)
        extends Canvas(parent, style) with PaintListener {

    private var scrollLeft = 0L
    private var scrollTop = 0L

    // adjustable column 왼쪽 X
    private var columnsLeft = Seq(0L)

    private var updated = List[Figure]()

    // Figure의 내용이 변경되는 경우엔 반드시 figureUpdated를 호출해주어야 한다
    // figure는 반드시 rootFigure로부터 도달 가능한 것이어야 함
    def figureUpdated(figure: Figure): Unit = {
        updated +:= figure
        figure.extra.updateParents()
        redraw()
    }

    private var firstRendering: Boolean = true

    // paintControl이 호출되는 경우는
    // 1. scroll이 변경된 경우(혹은 처음 화면에 표시되는 경우)
    // 2. 내용이 변경된 경우
    // 1의 경우엔 항상 전체 화면을 다시 그려야 하고
    // 2의 경우엔 변경된 내용이 화면 밖에 있으면 다시 그릴 필요가 없고, 화면 안에 있으면 변경된 영역만 다시 그리면 된다
    // 일단은 전부 다시 그리자
    def paintControl(e: PaintEvent): Unit = {
        val dc = DrawingContext(e.gc, drawingConfig)

        if (firstRendering) {
            rootFigure.extra.updateParents()
            rootFigure.estimateDimensionExtra(dc)
            firstRendering = false
        } else {
            updated foreach { figure =>
                val newDim = figure.estimateDimension(dc)
                // oldDim과 newDim의 변화량만큼 이 figure 상위 figure들의 크기를 변경
                figure.extra.notifyDimensionChanged(newDim)
            }
        }
        updated = List()

        val bounds = getBounds
        val wholeDimension = rootFigure.extra.estimatedDimension.get
        if (scrollLeft + bounds.width > wholeDimension.width) {
            scrollLeft = Math.max(0, wholeDimension.width - bounds.width)
        }
        if (scrollTop + bounds.height > wholeDimension.height) {
            scrollTop = Math.max(0, wholeDimension.height - bounds.height)
        }

        rootFigure.extra.draw(dc, Point(scrollLeft, scrollTop), Dimension(bounds.width, bounds.height))
    }
}
