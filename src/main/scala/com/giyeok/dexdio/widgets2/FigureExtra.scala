package com.giyeok.dexdio.widgets2

class LineLabels {
    // TODO labels를 FlatFigure로 바꾸는게 좋을까?
    private var labels = List[Label]()
    private var lineHeightCache = Option.empty[Long]

    // TODO LineLabelsPointer.idx 처리
    def add(label: Label): LineLabelsPointer = {
        labels +:= label
        lineHeightCache = None
        LineLabelsPointer(this, 0)
    }
    def lastPointer: LineLabelsPointer = {
        lineHeightCache = None
        LineLabelsPointer(this, 0)
    }

    def splitAt(idx: Int): LineLabels = {
        // TODO 현재 라인에서 idx 이후 부분은 제거하고 새 LineLabels에 편입시켜준다
        lineHeightCache = None
        ???
    }

    def lineHeight: Long = if (labels.isEmpty) 0 else {
        lineHeightCache match {
            case Some(height) => height
            case None =>
                val h = (labels map { _.figureExtra.dimension.totalHeight }).max
                lineHeightCache = Some(h)
                h
        }
    }
}

// Deferred 등에 의해서 한 라인인줄 알았던 내용이 두 줄로 쪼개질 때 idx가 필요하다
case class LineLabelsPointer(lineLabels: LineLabels, idx: Int) {
    def splitLine(): LineLabels = {
        lineLabels.splitAt(idx)
    }
}

private class FigureExtra(figure: Figure) {
    var parent: AbstractFigure = _
    var dimension: FigureDimension = _
    var leadingLine: LineLabelsPointer = _
    var trailingLine: Option[LineLabelsPointer] = _

    implicit final class MyEnsuring[A](private val self: A) {
        def ensuringEquals(other: A): A = {
            assert(self == other)
            self
        }
    }

    def updateParent(parent: AbstractFigure): Unit = {
        this.parent = parent
        figure match {
            case _: Label => // nothing to do
            case chunk @ Chunk(children) =>
                children foreach { _.figureExtra.updateParent(chunk) }
            case container: Container =>
                container.containerExtra.chunkChildren foreach { _.figureExtra.updateParent(container) }
            case indented @ Indented(content) =>
                content.figureExtra.updateParent(indented)
            case deferred: Deferred =>
                if (deferred.deferredExtra.isContentSet) {
                    deferred.content.figureExtra.updateParent(deferred)
                }
            case actionable @ Actionable(content) =>
                content.figureExtra.updateParent(actionable)
            case transformable: Transformable =>
                transformable.content.figureExtra.updateParent(transformable)
        }
    }

    def updateDimension(dc: DrawingContext, lineLabels: LineLabels): LineLabels = {
        // TODO lineLabels를 LineLabels 대신 LineLabelsPointer로 받고 밑에서 new LineLabels 하는 부분을 split을 이용해야 할듯?
        def updateMultiFigures(children: Seq[Figure], lineLabels: LineLabels): LineLabels = {
            if (children.isEmpty) {
                this.leadingLine = lineLabels.lastPointer
                this.dimension = FigureDimension(Dimension.zero, None)
                this.trailingLine = None
                lineLabels
            } else {
                this.leadingLine = lineLabels.lastPointer
                val finalLineLabels = children.foldLeft(lineLabels) { (m, figure) =>
                    figure.figureExtra.updateDimension(dc, m)
                }
                val totalDimension = {
                    val childrenDims = children map { _.figureExtra.dimension }
                    val (leading, rest) = childrenDims span { _.rest.isEmpty }
                    val leadingDimension = leading.foldLeft(Dimension.zero) { (m, i) => m.addRight(i.leading) }
                    if (rest.isEmpty) {
                        FigureDimension(leadingDimension, None)
                    } else {
                        val (exclusiveHeight, trailingDimension) = rest.tail.foldLeft(rest.head.rest.get) { (cc, child) =>
                            val (height, trailing) = cc
                            child.rest match {
                                case Some((restHeight, restTrailing)) =>
                                    (height + Math.max(trailing.height, child.leading.height) + restHeight, restTrailing)
                                case None =>
                                    (height, trailing addRight child.leading)
                            }
                        }
                        FigureDimension(leadingDimension, Some((exclusiveHeight, trailingDimension)))
                    }
                }
                this.dimension = totalDimension
                if (totalDimension.rest.isEmpty) {
                    this.trailingLine = None
                    assert(lineLabels eq finalLineLabels)
                    lineLabels
                } else {
                    this.trailingLine = Some(finalLineLabels.lastPointer)
                    finalLineLabels
                }
            }
        }

        def updateContentDimension(content: Figure, lineLabels: LineLabels): LineLabels = {
            this.leadingLine = lineLabels.lastPointer
            val trailingLineLabels = content.figureExtra.updateDimension(dc, lineLabels)
            this.dimension = content.figureExtra.dimension
            if (content.figureExtra.dimension.rest.isEmpty) {
                this.trailingLine = None
                assert(lineLabels eq trailingLineLabels)
                lineLabels
            } else {
                this.trailingLine = Some(trailingLineLabels.lastPointer)
                trailingLineLabels
            }
        }

        figure match {
            case newLine: NewLine =>
                this.dimension = FigureDimension(Dimension.zero, Some((0L, Dimension(0, dc.standardLineHeight))))
                this.leadingLine = lineLabels.add(newLine)
                val newLineLabels = new LineLabels
                this.trailingLine = Some(newLineLabels.lastPointer)
                newLineLabels

            case label: Label =>
                this.dimension = FigureDimension(label.measureDimension(dc), None)
                this.leadingLine = lineLabels.add(label)
                this.trailingLine = None
                lineLabels

            case Chunk(children) =>
                updateMultiFigures(children, lineLabels)

            case container: Container =>
                updateMultiFigures(container.containerExtra.chunkChildren, lineLabels)

            case Indented(content) =>
                this.leadingLine = lineLabels.lastPointer
                val trailingLineLabels = content.figureExtra.updateDimension(dc, new LineLabels)
                this.dimension = FigureDimension(Dimension.zero, Some(content.figureExtra.dimension.totalHeight, Dimension.zero))
                this.trailingLine = Some(trailingLineLabels.lastPointer)
                new LineLabels

            case deferred: Deferred =>
                this.leadingLine = lineLabels.lastPointer
                if (!deferred.deferredExtra.isContentSet) {
                    this.dimension = deferred.estimateDimension(dc)
                    if (this.dimension.rest.isEmpty) {
                        this.trailingLine = None
                        lineLabels
                    } else {
                        val newLineLabels = new LineLabels
                        this.trailingLine = Some(newLineLabels.lastPointer)
                        newLineLabels
                    }
                } else {
                    updateContentDimension(deferred.content, lineLabels)
                }

            case Actionable(content) =>
                updateContentDimension(content, lineLabels)

            case transformable: Transformable =>
                // transformable.content.figureExtra.estimateLayout()
                updateContentDimension(transformable.content, lineLabels)
        }
    }

    // TODO totalWidth도 만들기
    def totalHeight: Long =
        if (dimension.rest.isEmpty) {
            leadingLine.lineLabels.lineHeight
        } else {
            if (trailingLine == null) {
                println(figure)
                println("???")
            }
            leadingLine.lineLabels.lineHeight + dimension.rest.get._1 + trailingLine.get.lineLabels.lineHeight
        }
}

private class ContainerExtra(container: Container) {
    // children의 크기가 너무 커지지 않도록 Chunk로 묶기
    val chunkChildren: Seq[Figure] = {
        def group(children: Seq[Figure]): Seq[Figure] =
            if (children.size < 100) children else {
                group((children grouped (children.size / 100) map Chunk).toSeq)
            }
        group(container.children)
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
