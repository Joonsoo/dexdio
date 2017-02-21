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
}

object Rectangle {
    def apply(rect: org.eclipse.swt.graphics.Rectangle): Rectangle =
        Rectangle(Point(rect.x, rect.y), Dimension(rect.width, rect.height))
}
