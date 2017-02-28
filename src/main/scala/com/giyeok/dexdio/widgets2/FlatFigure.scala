package com.giyeok.dexdio.widgets2

sealed trait FlatFigure
case class FlatLabel(label: Label) extends FlatFigure
case class FlatPush(figure: Figure) extends FlatFigure
case class FlatPop(figure: Figure) extends FlatFigure
case class FlatDeferred(deferred: Deferred) extends FlatFigure

trait FlatFigureSeq {
    val seq: Seq[FlatFigure]

    def print(): Unit = {
        var indent = 0
        seq foreach {
            case FlatPush(pushed) =>
                println(s"${"  " * indent}FigurePush(${pushed.getClass.getSimpleName})")
                pushed match {
                    case _: Container => indent += 1
                    case _ => // nothing to do
                }
            case FlatPop(popped) =>
                println(s"${"  " * indent}FigurePop(${popped.getClass.getSimpleName})")
                popped match {
                    case _: Container => indent -= 1
                    case _ => // nothing to do
                }
            case FlatLabel(label) =>
                println(s"${"  " * indent}$label")
            case FlatDeferred(deferred) =>
                println(s"${"  " * indent}FigureDeferred($deferred)")
        }
    }

    def textRender: String = {
        val strings = seq.foldLeft((List[String](), "")) { (cc, fig) =>
            val (result, indent) = cc
            fig match {
                case FlatLabel(TextLabel(text, _, _)) =>
                    (text +: result, indent)
                case FlatLabel(SpacingLabel(pixelWidth, spaceCount)) =>
                    ((" " * (spaceCount + (pixelWidth / 12))) +: result, indent)
                case FlatLabel(NewLine()) => (s"\n$indent" +: result, indent)
                case FlatPush(Indented(_)) =>
                    val newIndent = indent + (" " * 2)
                    (s"\n$newIndent" +: result, newIndent)
                case FlatPop(Indented(_)) =>
                    ("\n" +: result, indent.substring(Math.min(indent.length, 2)))
                case _ => cc
            }
        }
        strings._1.reverse.mkString
    }
}

class FlatFigureLine(val seq: Seq[FlatFigure], val context: List[FlatPush]) extends FlatFigureSeq {
    def labels: Seq[Label] = seq collect {
        case FlatLabel(label) => label
    }
}

class FlatFigures(val seq: Seq[FlatFigure], val context: List[FlatPush]) extends FlatFigureSeq {
    def isEmpty: Boolean = seq.isEmpty

    def nextLine: (FlatFigureLine, FlatFigures) = {
        val (line0, rest0) = seq span {
            case FlatLabel(NewLine()) => false
            case FlatPush(_: Indented) => false
            case FlatPop(_: Indented) => false
            case _ => true
        }
        val (line, rest) = if (rest0.nonEmpty) (line0 :+ rest0.head, rest0.tail) else (line0, rest0)
        val newContext: List[FlatPush] = line.foldLeft(context) { (m, next) =>
            next match {
                case pushed: FlatPush => pushed +: m
                case FlatPop(popped) => m.tail ensuring m.head.figure == popped
                case _ => m
            }
        }
        (new FlatFigureLine(line, context), new FlatFigures(rest, newContext))
    }

    def linesStream: Stream[FlatFigureLine] = {
        if (isEmpty) {
            Stream()
        } else {
            val (line, rest) = nextLine
            line #:: rest.linesStream
        }
    }

    def lines: Seq[FlatFigureLine] = {
        if (isEmpty) {
            Stream()
        } else {
            val (line, rest) = nextLine
            line +: rest.lines
        }
    }
}

object FlatFigureStream {
    implicit class FigureFlattable(figure: Figure) {
        def flatFiguresStream(expandDeferred: Boolean): FlatFigures = {
            def traverse(fig: Figure): Stream[FlatFigure] =
                fig match {
                    case label: Label => Stream(FlatLabel(label))
                    case container @ Container(children, _) =>
                        (FlatPush(container) #:: (children.toStream flatMap traverse)) append Stream(FlatPop(container))
                    case indented @ Indented(content) =>
                        (FlatPush(indented) #:: traverse(content)) append Stream(FlatPop(indented))
                    case deferred: Deferred =>
                        if (expandDeferred) {
                            (FlatPush(deferred) #:: traverse(deferred.content())) append Stream(FlatPop(deferred))
                        } else {
                            Stream(FlatPush(deferred), FlatDeferred(deferred), FlatPop(deferred))
                        }
                    case actionable @ Actionable(content) =>
                        (FlatPush(actionable) #:: traverse(content)) append Stream(FlatPop(actionable))
                    case transformable: Transformable =>
                        (FlatPush(transformable) #:: traverse(transformable.content)) append Stream(FlatPop(transformable))
                }
            new FlatFigures(traverse(figure), List())
        }

        def flatFigures(expandDeferred: Boolean): FlatFigures = {
            def traverse(fig: Figure): Seq[FlatFigure] =
                fig match {
                    case label: Label =>
                        Seq(FlatLabel(label))
                    case container @ Container(children, _) =>
                        FlatPush(container) +: (children.toStream flatMap traverse) :+ FlatPop(container)
                    case indented @ Indented(content) =>
                        FlatPush(indented) +: traverse(content) :+ FlatPop(indented)
                    case deferred: Deferred =>
                        if (expandDeferred) {
                            FlatPush(deferred) +: traverse(deferred.content()) :+ FlatPop(deferred)
                        } else {
                            Seq(FlatPush(deferred), FlatDeferred(deferred), FlatPop(deferred))
                        }
                    case actionable @ Actionable(content) =>
                        FlatPush(actionable) +: traverse(content) :+ FlatPop(actionable)
                    case transformable: Transformable =>
                        FlatPush(transformable) +: traverse(transformable.content) :+ FlatPop(transformable)
                }
            new FlatFigures(traverse(figure), List())
        }
    }
}
