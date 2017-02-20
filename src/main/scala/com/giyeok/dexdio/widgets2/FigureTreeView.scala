package com.giyeok.dexdio.widgets2

import com.giyeok.dexdio.widgets2.FlatFigureStream._
import org.eclipse.swt.SWT
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.events.PaintListener
import org.eclipse.swt.widgets.Canvas
import org.eclipse.swt.widgets.Caret
import org.eclipse.swt.widgets.Composite

// TODO Figure에서 LineNum 처리
sealed trait Layer
case class TagsLayer(tags: Set[Tag]) extends Layer
case class LinesLayer(lineNums: Set[Int]) extends Layer
case class CompositeLayer(tags: Set[Tag], lineNums: Set[Int]) extends Layer

// TODO RenderPlan은 planRender에 지정된 각 Layer를 그리는데 필요한 정보를 모두 포함한다
// 1. 화면에 표시될 Figure의 목록과 해당 Figure의 위치(차지하는 영역)
// 2. 그려지는 영역을 전부 포함하는 bound(점유한 영역) 정보(polygon 형태로 변환 가능한)가 포함되어야 한다
//   - 두개 이상의 분리된 영역일 수 있다
//   - 하나의 figure에 대한 bound는 분리되지 않고 한 덩어리로 표현되어야 한다
class RenderPlan

class FigureTreeView(parent: Composite, style: Int, root: Container, columns: Seq[(Figure, Int)], drawingConfig: DrawingConfig)
        extends Canvas(parent, style | SWT.DOUBLE_BUFFERED) with PaintListener with DisposeListener {

    private var scrollLeft = 0L
    private var scrollTop = 0L

    // adjustable column 왼쪽 X
    private var columnsLeft = Seq(0L)

    private var updated = List[Figure]()

    // Figure의 내용이 변경되는 경우엔 반드시 figureUpdated를 호출해주어야 한다
    // figure는 반드시 rootFigure로부터 도달 가능한 것이어야 함
    def figureUpdated(figure: Figure): Unit = {
        updated +:= figure
        // figure.figureExtra.updateParents()
        redraw()
    }

    private var firstRendering: Boolean = true

    // layers 순서대로 + layers에서 처리되지 않은 나머지 figure들을 담은 RenderPlan이 반환된다
    def planRender(figure: Figure, dc: DrawingContext, scroll: Point, screenBound: Rectangle, layers: Seq[Layer]): Seq[RenderPlan] = {
        val visibleArea = screenBound + scroll
        val position = figure.figureExtra.coord.position
        val visibleY = visibleArea.top to visibleArea.bottom
        if ((visibleY contains position.y) || (visibleY contains (position.y + figure.figureExtra.estimatedHeight))) {
            // 이 figure가 화면에 보여질 y와 겹치는 영역에 있어서 화면에 표시될 가능성이 있음
            figure match {
                case label: Label =>
                    val visibleX = visibleArea.left to visibleArea.right
                    if ((visibleX contains position.x) || (visibleX contains position.x + label.labelExtra.measuredWidth)) {
                        // 이 label은 보이는 영역 안에 있어서 보임
                        // TODO RenderPlan에 추가함
                    }
                case Actionable(content) => // content 에 대해서 처리
                case container: Container =>
                    container.flatFigureStream
                case deferred: Deferred =>
                    val content = deferred.content
                    if (deferred.figureExtra.estimatedHeight != content.figureExtra.estimatedHeight) {
                        // TODO deferred가 붙어있는 anchor에게 deferred의 사이즈가 바뀌었음을 알려줘서 relayout하고
                        // deferred.figureExtra.estimatedHeight 를 업데이트한다
                    }
                // content 에 대해서 처리
                case Indented(content) => // content에 대해서 처리
                case transformable: Transformable => // transformable.content 에 대해서 처리
            }
        }
        figure.flatFigureStream.lines
        // scroll을 빼서 그게 bounds 안에 있으면 그리고 아니면 끝
        // TODO
        Seq()
    }

    def executeRenderPlan(renderPlan: RenderPlan): Unit = {
        ???
    }

    // paintControl이 호출되는 경우는
    // 1. scroll이 변경된 경우(혹은 처음 화면에 표시되는 경우)
    // 2. 내용이 변경된 경우
    // 1의 경우엔 항상 전체 화면을 다시 그려야 하고
    // 2의 경우엔 변경된 내용이 화면 밖에 있으면 다시 그릴 필요가 없고, 화면 안에 있으면 변경된 영역만 다시 그리면 된다
    // 일단은 전부 다시 그리자
    def paintControl(e: PaintEvent): Unit = {
        println(s"paintControl ${System.currentTimeMillis()}")
        val dc = DrawingContext(e.gc, drawingConfig)

        if (firstRendering) {
            root.containerExtra.prepareForTheFirstTime(dc)
            // rootFigure.figureExtra.updateParents()
            firstRendering = false
        } else {
            // TODO figure가 업데이트되면서 새로 생긴 부분이 있으면 figureExtra.coord가 반드시 셋팅되도록 해야 함
            //            updated foreach { figure =>
            //                val newDim = figure.estimateDimension(dc)
            //                // oldDim과 newDim의 변화량만큼 이 figure 상위 figure들의 크기를 변경
            //                figure.figureExtra.notifyDimensionChanged(newDim - figure.figureExtra.estimatedDimension)
            //            }
        }
        updated = List()

        val bounds = getBounds
        val wholeHeight = root.figureExtra.estimatedHeight
        if (scrollLeft < 0) {
            scrollLeft = 0
        }
        // TODO scrollLeft max값 설정
        if (scrollTop < 0) {
            scrollTop = 0
        }
        if (scrollTop + bounds.height > wholeHeight) {
            scrollTop = Math.max(0, wholeHeight - bounds.height)
        }

        val plan = planRender(root, dc, Point(scrollLeft, scrollTop), Rectangle(bounds), Seq())
        plan foreach executeRenderPlan

        getCaret.setBounds(20, 20, 2, 15)
    }

    def widgetDisposed(e: DisposeEvent): Unit = {
        // getCaret.dispose() if needed
    }

    setCaret(new Caret(this, SWT.NONE))
    addPaintListener(this)
    addDisposeListener(this)
}
