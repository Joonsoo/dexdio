package com.giyeok.dexdio.widgets2

object Point {
    val zero = Point(0, 0)
}
case class Point(x: Long, y: Long) {
    def +(diff: Point): Point =
        Point(x + diff.x, y + diff.y)
    def -(other: Point): Point =
        Point(x - other.x, y - other.y)
}

object Dimension {
    val zero = Dimension(0, 0)
}
case class Dimension(width: Long, height: Long) {
    def +(diff: Dimension): Dimension =
        Dimension(width + diff.width, height + diff.height)
    def -(other: Dimension): Dimension =
        Dimension(width - other.width, height - other.height)
    def addRight(right: Dimension): Dimension =
        Dimension(width + right.width, Math.max(height, right.height))
    def addBottom(bottom: Dimension): Dimension =
        Dimension(Math.max(width, bottom.width), height + bottom.height)
}

case class Rectangle(leftTop: Point, dimension: Dimension) {
    def +(diff: Point): Rectangle =
        Rectangle(leftTop + diff, dimension)
    def left: Long = leftTop.x
    def top: Long = leftTop.y
    def right: Long = left + dimension.width
    def bottom: Long = top + dimension.height
    def width: Long = dimension.width
    def height: Long = dimension.height

    def shrink(left: Int, top: Int, right: Int, bottom: Int): Rectangle = {
        Rectangle(
            Point(leftTop.x + left, leftTop.y + top),
            Dimension(dimension.width - left - right, dimension.height - top - bottom)
        )
    }
}

object Rectangle {
    def apply(rect: org.eclipse.swt.graphics.Rectangle): Rectangle =
        Rectangle(Point(rect.x, rect.y), Dimension(rect.width, rect.height))
}

case class FigureDimension(leading: Dimension, rest: Option[(Long, Dimension)]) {
    def exclusiveHeight: Option[Long] = rest map { _._1 }
    def trailing: Option[Dimension] = rest map { _._2 }
    def totalHeight: Long = leading.height + (rest map { r => r._1 + r._2.height }).getOrElse(0L)
}

case class RenderingPoint(x: Long, y: Long) {
    def proceed(dimension: FigureDimension, leadingLine: LineLabels): RenderingPoint =
        dimension.rest match {
            case Some(rest) =>
                RenderingPoint(
                    rest._2.width, y + leadingLine.lineHeight + rest._1
                )
            case None =>
                RenderingPoint(
                    x + dimension.leading.width, y
                )
        }
    def proceed(figure: Figure): RenderingPoint = {
        val extra = figure.figureExtra
        proceed(extra.dimension, extra.leadingLine.lineLabels)
    }
}
