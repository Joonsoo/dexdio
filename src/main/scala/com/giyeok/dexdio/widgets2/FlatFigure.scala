package com.giyeok.dexdio.widgets2

sealed trait FlatFigure
case class FigureLabel(label: Label) extends FlatFigure
case class FigurePush(figure: Figure) extends FlatFigure
case class FigurePop(figure: Figure) extends FlatFigure

trait FlatFigureSeq {
    val seq: Seq[FlatFigure]

    def print(): Unit = {
        var indent = 0
        seq foreach {
            case FigurePush(pushed) =>
                println(s"${"  " * indent}FigurePush(${pushed.getClass.getSimpleName})")
                pushed match {
                    case _: Container => indent += 1
                    case _ => // nothing to do
                }
            case FigurePop(popped) =>
                println(s"${"  " * indent}FigurePop(${popped.getClass.getSimpleName})")
                popped match {
                    case _: Container => indent -= 1
                    case _ => // nothing to do
                }
            case FigureLabel(label) => println(s"${"  " * indent}$label")
        }
    }

    def textRender: String = {
        val strings = seq.foldLeft((List[String](), "")) { (cc, fig) =>
            val (result, indent) = cc
            fig match {
                case FigureLabel(TextLabel(text, _, _)) =>
                    (text +: result, indent)
                case FigureLabel(SpacingLabel(pixelWidth, spaceCount)) =>
                    ((" " * (spaceCount + (pixelWidth / 12))) +: result, indent)
                case FigureLabel(NewLine()) => (s"\n$indent" +: result, indent)
                case FigureLabel(ColumnSep()) => ("\t" +: result, indent)
                case FigurePush(Indented(_)) =>
                    val newIndent = indent + (" " * 2)
                    (s"\n$newIndent" +: result, newIndent)
                case FigurePop(Indented(_)) => ("\n" +: result, indent.substring(Math.min(indent.length, 2)))
                case _ => cc
            }
        }
        strings._1.reverse.mkString
    }
}

class FlatFigureLine(val seq: Seq[FlatFigure]) extends FlatFigureSeq

class FlatFigureStream(val seq: Stream[FlatFigure]) extends FlatFigureSeq {
    def isEmpty: Boolean = seq.isEmpty

    def nextLine: (FlatFigureLine, FlatFigureStream) = {
        val (line, rest) = seq span {
            case FigureLabel(_: NewLine) => false
            case _ => true
        }
        if (rest.nonEmpty) {
            (new FlatFigureLine(line :+ rest.head), new FlatFigureStream(rest.tail))
        } else (new FlatFigureLine(line), new FlatFigureStream(rest))
    }

    def lines: Stream[FlatFigureLine] = {
        if (isEmpty) {
            Stream()
        } else {
            val (line, rest) = nextLine
            line #:: rest.lines
        }
    }
}

object FlatFigureStream {
    implicit class FigureFlattable(figure: Figure) {
        def flatFigureStream: FlatFigureStream = {
            def traverse(figure: Figure): Stream[FlatFigure] =
                figure match {
                    case label: Label => Stream(FigureLabel(label))
                    case container @ Container(children, _) =>
                        (FigurePush(container) #:: (children.toStream flatMap traverse)) append Stream(FigurePop(container))
                    case indented @ Indented(content) =>
                        (FigurePush(indented) #:: traverse(content)) append Stream(FigurePop(indented))
                    case deferred: Deferred =>
                        (FigurePush(deferred) #:: traverse(deferred.content)) append Stream(FigurePop(deferred))
                    case actionable @ Actionable(content) =>
                        (FigurePush(actionable) #:: traverse(content)) append Stream(FigurePop(actionable))
                    case transformable: Transformable =>
                        (FigurePush(transformable) #:: traverse(transformable.content)) append Stream(FigurePop(transformable))
                }
            new FlatFigureStream(traverse(figure))
        }
    }
}
