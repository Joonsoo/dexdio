package com.giyeok.dexdio.widgets2

import com.giyeok.dexdio.widgets2.FlatFigureStream._

// TODO 가로로 긴 데이터를 잘 처리하기 위해서 FlatFigure sequence도 chunk화
// line.context는 line의 시작점 기준으로 FlatPush들 목록. 이 목록은 다음 용도로 사용:
// 1. Tag 처리를 위해서
// 2. Indent 처리를 위해서

private trait LinesChunk {
    var _parent: Option[(LinesChunk, Int)] = None
    private var _dimension: Option[Dimension] = None
    private var _linesCount: Option[Int] = None

    // TODO estimatedDimension 개념 추가
    //   - Deferred가 아닌 figure가 많으면 처음 뜰 때 너무 느림
    //   - estimatedDimension은 이 라인이 화면에 보일지 안 보일지 결정할 때 사용. 실제 화면에 그려질 때는 실제로 measureDimension해서 사용
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
private case class Line(line: FlatFigureLine) extends LinesChunk {
    lazy val indentDepth: Int = line.context count { _.figure.isInstanceOf[Indented] }

    line.seq.zipWithIndex foreach { c =>
        c._1 match {
            case FlatPush(figure) =>
                figure.figureExtra.startPoint = (this, c._2)
            case FlatPop(figure) =>
                figure.figureExtra.endPoint = (this, c._2)
            case FlatLabel(label) =>
                label.figureExtra.startPoint = (this, c._2)
            case FlatDeferred(deferred) =>
                deferred.figureExtra.startPoint = (this, c._2)
        }
    }

    // figure가 업데이트되면 startPoint의 line에 `contentUpdated` 메소드를 호출한다
    // figure가 업데이트되었다는 것은 figure.startPoint~endPoint 사이에 포함된 figure의 목록이 변경된 경우.
    // Deferred의 내용이 구체화될 때도 호출됨.
    // 크기도 당연히 변할 것으로 본다.
    def contentUpdated(figure: Figure): Unit = {
        assert(figure.figureExtra.startPoint._1 == this)
        // figure.figureExtra.startPoint~endPoint 사이의 내용을 figure.flatFigures(false) 로 치환한다
    }

    // figure의 크기가 변한 경우 startPoint의 line에 `sizeUpdated` 메소드를 호출한다
    // estimated되었던 dimension이 실제로 measure해보니 변경된 경우 호출한다.
    def dimensionUpdated(figure: Figure): Unit = {
        assert(figure.figureExtra.startPoint._1 == this)
    }

    def calculateDimension(dc: DrawingContext): Dimension =
        line.seq.foldLeft(Dimension.zero) { (cc, figure) =>
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
    def calculateLinesCount(): Int = {
        val lines = line.seq collect {
            case FlatDeferred(deferred) if deferred.lines.following.isDefined =>
                deferred.lines.following.get._1
        }
        1 + lines.length + lines.sum
    }
}
private case class Chunk(children: Seq[LinesChunk]) extends LinesChunk {
    children.zipWithIndex foreach { c => c._1._parent = Some(this, c._2) }

    def calculateDimension(dc: DrawingContext): Dimension =
        children.foldLeft(Dimension.zero) { (cc, line) =>
            cc.addBottom(line.dimension(dc))
        }
    def calculateLinesCount(): Int = (children map { _.linesCount }).sum
}
