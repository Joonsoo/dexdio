package com.giyeok.dexdio.widgets2

import org.eclipse.jface.resource.JFaceResources
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

object FigureTreeViewTest {
    val methodAbc = Container(
        Seq(Container(
            Seq(
                TextLabel("int", TextNoDecoration, Set("type:int")),
                SpacingLabel(0, 1),
                TextLabel("abc", TextNoDecoration, Set("methodName", "method:com/abc/aaa:abc")),
                TextLabel("(", TextNoDecoration, Set("punctuation")),
                Container(Seq(
                    Container(Seq(
                        TextLabel("int", TextNoDecoration, Set("type", "type:I")),
                        SpacingLabel(0, 1),
                        TextLabel("a", TextNoDecoration, Set("parameterName", "parameter:com/abc/aaa/:abc:0"))
                    ), Set("parameter", "parameter:com/abc/aaa/:abc:0")),
                    Container(Seq(
                        TextLabel("String", TextNoDecoration, Set("type", "type:java/lang/String")),
                        SpacingLabel(0, 1),
                        TextLabel("b", TextNoDecoration, Set("parameterName", "parameter:com/abc/aaa/:abc:1"))
                    ), Set("parameter", "parameter:com/abc/aaa/:abc:1"))
                ), Set("parameterList", "method:com/abc/aaa:abc")),
                TextLabel(")", TextNoDecoration, Set("punctuation")),

                Container(
                    Seq(
                        TextLabel("{", TextNoDecoration, Set("punctuation")),
                        NewLine(),
                        Deferred(Indented(
                            Container(Seq(
                                Container(
                                    Seq(
                                        TextLabel("statement", TextNoDecoration, Set("blahblah")),
                                        NewLine()
                                    ),
                                    Set("methodBodyLine")
                                ),
                                Container(
                                    Seq(
                                        TextLabel("statement2", TextNoDecoration, Set("blahblah")),
                                        NewLine()
                                    ),
                                    Set("methodBodyLine")
                                )
                            ), Set("methodBodyContent"))
                        )),
                        TextLabel("}", TextNoDecoration, Set("punctuation")),
                        NewLine()
                    ),
                    Set("methodBody", "method:com/abc/aaa:abc")
                )
            ),
            Set("methodSignature", "method:com/abc/aaa:abc")
        )),
        Set()
    )

    // import com.giyeok.dexdio.widgets2._
    def nextLine(stream: Stream[FlatFigure]): (Stream[FlatFigure], Stream[FlatFigure]) = {
        val (line, rest) = stream span {
            case FigureLabel(_: NewLine) => false
            case _ => true
        }
        if (rest.nonEmpty) {
            (line :+ rest.head, rest.tail)
        } else (line, rest)
    }
    def print(stream: Iterable[FlatFigure]): Unit = {
        stream foreach {
            case FigurePush(_) => println("FigurePush")
            case FigurePop(_) => println("FigurePush")
            case FigureLabel(label) => println(label)
        }
    }
    val (thisLine, rest) = nextLine(FigureTreeViewTest.methodAbc.flatten)
    print(thisLine)

    def main(args: Array[String]): Unit = {
        val display = new Display()
        val shell = new Shell(display)

        shell.setLayout(new FillLayout())
        shell.setBounds(100, 100, 800, 600)

        new FigureTreeView(shell, SWT.NONE, TextLabel("hello", TextNoDecoration, Set()), Seq(), DrawingConfig(15, JFaceResources.getFont(JFaceResources.TEXT_FONT)))

        shell.open()

        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }
}
