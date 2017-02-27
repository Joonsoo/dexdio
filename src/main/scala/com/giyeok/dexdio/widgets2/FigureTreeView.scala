package com.giyeok.dexdio.widgets2

import scala.collection.immutable.NumericRange
import com.giyeok.dexdio.widgets2.FlatFigureStream._
import org.eclipse.draw2d.ColorConstants
import org.eclipse.swt.SWT
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.events.KeyListener
import org.eclipse.swt.events.MouseEvent
import org.eclipse.swt.events.MouseWheelListener
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.events.PaintListener
import org.eclipse.swt.widgets.Canvas
import org.eclipse.swt.widgets.Caret
import org.eclipse.swt.widgets.Composite

// TODO 가로로 긴 데이터를 잘 처리하기 위해서 FlatFigure sequence도 chunk화
// figures.context는 Line의 시작점 기준으로 FlatPush들 목록. 이 목록은 다음 용도로 사용:
// 1. Tag 처리를 위해서
// 2. Indent 처리를 위해서
case class Line(figures: FlatFigureLine) {
    val indentDepth: Int = figures.context count { _.figure.isInstanceOf[Indented] }

    def dimension(dc: DrawingContext): Dimension =
        figures.seq.foldLeft(Dimension.zero) { (cc, figure) =>
            figure match {
                case FlatLabel(label) =>
                    cc.addRight(label.measureDimension(dc))
                case FlatDeferred(deferred) =>
                    val dim = deferred.pixelsDimension(dc)
                    // TODO 수정
                    cc.addRight(dim.leading)
                case _ => cc
            }
        }
    def linesCount: Int = {
        val lines = figures.seq collect {
            case FlatDeferred(deferred) if deferred.lines.following.isDefined =>
                deferred.lines.following.get._1
        }
        1 + lines.length + lines.sum
    }
}

sealed trait LinesChunk {
    private var _dimension: Option[Dimension] = None
    private var _linesCount: Option[Int] = None

    // TODO Line 및 LineChunks에 estimatedHeight 개념이 필요할까 고민(Deferred가 아니면 처음 뜰 때 너무 느림)
    // TODO Figure에 해당 Figure가 속한 Line 포인터 추가(Line에 대한 reference, Push/Pop 혹은 Label의 인덱스)
    // TODO LinesChunk에 해당 Chunk가 속한 Chunk 포인터 추가(parent chunk에 대한 reference, 해당 청크 내에서 인덱스)
    // TODO Figure의 크기나 내용이 변경되면 Line에 notify, Line은 상위 LinesChunk에 notify
    def dimension(dc: DrawingContext): Dimension = _dimension match {
        case Some(dimension) => dimension
        case None =>
            val dimension = calculateDimension(dc)
            _dimension = Some(dimension)
            dimension
    }
    def linesCount: Int = _linesCount match {
        case Some(linesCount) => linesCount
        case None =>
            val linesCount = calculateLinesCount()
            _linesCount = Some(linesCount)
            linesCount
    }

    def calculateDimension(dc: DrawingContext): Dimension
    def calculateLinesCount(): Int
}
case class OneLine(line: Line) extends LinesChunk {
    def calculateDimension(dc: DrawingContext): Dimension = line.dimension(dc)
    def calculateLinesCount(): Int = line.linesCount
}
case class ChunksChunk(chunks: Seq[LinesChunk]) extends LinesChunk {
    def calculateDimension(dc: DrawingContext): Dimension =
        chunks.foldLeft(Dimension.zero) { (cc, line) =>
            cc.addBottom(line.dimension(dc))
        }
    def calculateLinesCount(): Int = (chunks map { _.linesCount }).sum
}

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

class FigureTreeView(parent: Composite, style: Int, root: Figure, columns: Seq[(Figure, Int)], drawingConfig: DrawingConfig)
        extends Canvas(parent, style | SWT.DOUBLE_BUFFERED) with PaintListener with DisposeListener with KeyListener with MouseWheelListener {

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

    private val lineChunkSize = 100

    private var lines: LinesChunk = {
        def group(seq: Seq[LinesChunk]): Seq[LinesChunk] =
            if (seq.length < lineChunkSize) {
                seq
            } else {
                val groupSize = seq.length / lineChunkSize
                group((seq.grouped(seq.length / groupSize) map { ChunksChunk(_) }).toSeq)
            }

        val lines = root.flatFigures(false).linesStream
        val chunks = group(lines map { x => OneLine(Line(x)) })
        if (chunks.length > 1) ChunksChunk(chunks) else chunks.head
    }
    println(lines)

    def rangeOverlap(a: NumericRange.Inclusive[Long], b: NumericRange.Inclusive[Long]): Boolean =
        !((a.end < b.start) || (b.end < a.start))

    private def testRender(dc: DrawingContext, scroll: Point, screenBound: Rectangle): Unit = {
        val visibleArea = screenBound + scroll
        val visibleX = visibleArea.left to visibleArea.right
        val visibleY = visibleArea.top to visibleArea.bottom
        def traverse(chunk: LinesChunk, top: Long): Long = {
            val bottom = top + chunk.dimension(dc).height
            val occypingY = top to bottom
            if (rangeOverlap(visibleY, occypingY)) {
                chunk match {
                    case OneLine(line) =>
                        line.figures.seq.foldLeft(dc.indentWidth * line.indentDepth) { (x, figure) =>
                            figure match {
                                case FlatLabel(label) =>
                                    label match {
                                        case TextLabel(text, deco, _) =>
                                            val dimension = label.measureDimension(dc) // TODO 실제 렌더링할 떈 caching
                                            val occupyingX = x to (x + dimension.width)
                                            if (rangeOverlap(visibleX, occupyingX)) {
                                                deco.execute(dc.gc) {
                                                    dc.gc.drawText(text, (x - scroll.x).toInt, (bottom - dimension.height - scroll.y).toInt)
                                                }
                                            }
                                            x + dimension.width
                                        case ImageLabel(image, _) =>
                                            ???
                                        case spacing: SpacingLabel =>
                                            x + spacing.widthInPixel(dc)
                                        case NewLine() => x // nothing to do
                                    }
                                case _ => x
                            }
                        }
                    case ChunksChunk(chunks) =>
                        chunks.foldLeft(top) { (newTop, chunk) => traverse(chunk, newTop) }
                }
            }
            bottom
        }
        traverse(lines, 0)
        // println(s"TraverseCount:$traverseCount")
    }

    // layers 순서대로 + layers에서 처리되지 않은 나머지 figure들을 담은 RenderPlan이 반환된다
    private def planRender(dc: DrawingContext, scroll: Point, screenBound: Rectangle, layers: Seq[Layer]): Seq[RenderPlan] = {
        val visibleArea = screenBound + scroll
        val visibleY = visibleArea.top to visibleArea.bottom
        def traverse(figure: Figure, top: Long, lineBottom: Long, left: Long, indent: Int, interests: Map[Layer, Nothing]) = {
            //            val position = figure.figureExtra.estimatedCoord.position
            //            val occupyingY = position.y to (position.y + figure.figureExtra.estimatedDimension.totalHeight)
            //            if (rangeOverlap(visibleY, occupyingY)) {
            //                figure match {
            //                    case container: Container =>
            //                        container.flatFigureStream.lines takeWhile { line =>
            //                            val lineHeight = line.labels.foldLeft(0L) { (m, i) =>
            //                                Math.max(m, i.figureExtra.estimatedDimension.leading.height)
            //                            }
            //                            // line.seq에서 Container가 아닌 FigurePush, FigurePop 처리해서 interests 변경하고
            //                            // Label들에 대해서 traverse
            //                            ???
            //                        }
            //                }
            //                // 이 figure가 화면에 보여질 y와 겹치는 영역에 있어서 화면에 표시될 가능성이 있음
            //                /*
            //                    figure match {
            //                        case label: Label =>
            //                            val visibleX = visibleArea.left to visibleArea.right
            //                            if ((visibleX contains position.x) || (visibleX contains position.x + label.labelExtra.measuredWidth)) {
            //                                // 이 label은 보이는 영역 안에 있어서 보임
            //                                // TODO RenderPlan에 추가함
            //                            }
            //                        case Actionable(content) => // content 에 대해서 처리
            //                        case container: Container =>
            //                            container.flatFigureStream
            //                        case deferred: Deferred =>
            //                            val content = needDeferredContent(deferred)
            //                        // content 에 대해서 처리
            //                        case Indented(content) => // content에 대해서 처리
            //                        case transformable: Transformable => // transformable.content 에 대해서 처리
            //                    }
            //                */
            //            }
            figure.flatFigures(false).lines
            // scroll을 빼서 그게 bounds 안에 있으면 그리고 아니면 끝
            // TODO
            Seq()
        }
        traverse(root, 0, 0, 0, 0, Map())
        ???
    }

    def executeRenderPlan(renderPlan: RenderPlan): Unit = {
        ???
    }

    private def boundScroll(bounds: Rectangle): Unit = {
        val wholeHeight: Long = ??? //root.figureExtra.totalHeight
        // println(s"wholeHeight:$wholeHeight $scrollTop ${wholeHeight - bounds.height}")
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
    }

    // paintControl이 호출되는 경우는
    // 1. scroll이 변경된 경우(혹은 처음 화면에 표시되는 경우)
    // 2. 내용이 변경된 경우
    // 1의 경우엔 항상 전체 화면을 다시 그려야 하고
    // 2의 경우엔 변경된 내용이 화면 밖에 있으면 다시 그릴 필요가 없고, 화면 안에 있으면 변경된 영역만 다시 그리면 된다
    // 일단은 전부 다시 그리자
    def paintControl(e: PaintEvent): Unit = {
        // println(s"paintControl ${System.currentTimeMillis()}")
        val dc = DrawingContext(e.gc, drawingConfig)

        dc.gc.setFont(drawingConfig.defaultFont)

        if (firstRendering) {
            root.figureExtra.updateParent(None)
            // TODO lines
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

        val bounds = Rectangle(getBounds).shrink(0, 0, 0, 20)
        val visibleBound = bounds.shrink(left = 100, top = 150, right = 150, bottom = 150)

        // boundScroll(bounds)

        /*
        val plan = planRender(dc, Point(scrollLeft, scrollTop), Rectangle(bounds), Seq())
        plan foreach executeRenderPlan
        */

        dc.gc.setForeground(ColorConstants.red)
        dc.gc.drawRectangle(visibleBound.left.toInt, visibleBound.top.toInt, visibleBound.width.toInt, visibleBound.height.toInt)
        dc.gc.setForeground(ColorConstants.black)

        testRender(dc, Point(scrollLeft, scrollTop), visibleBound)

        // println(s"${root.figureExtra.dimension} ${root.figureExtra.leadingLine.lineLabels.lineHeight} ${root.figureExtra.trailingLine.get.lineLabels.lineHeight}")

        getCaret.setBounds(20, 20, 2, 15)
    }

    def widgetDisposed(e: DisposeEvent): Unit = {
        // getCaret.dispose() if needed
    }

    setCaret(new Caret(this, SWT.NONE))
    addPaintListener(this)
    addDisposeListener(this)

    addKeyListener(this)
    addMouseWheelListener(this)

    def keyPressed(e: KeyEvent): Unit = {
        // println(e.keyCode, SWT.UP, SWT.DOWN, SWT.LEFT, SWT.RIGHT)
        e.keyCode match {
            case SWT.ARROW_UP => scrollTop -= 5
            case SWT.ARROW_DOWN => scrollTop += 5
            case SWT.ARROW_LEFT => scrollLeft -= 5
            case SWT.ARROW_RIGHT => scrollLeft += 5
            case _ => // nothing to do
        }
        redraw()
    }

    def keyReleased(e: KeyEvent): Unit = {}

    def mouseScrolled(e: MouseEvent): Unit = {
        // println(e)
        scrollTop -= e.count * 5
        redraw()
    }
}
