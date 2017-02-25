package com.giyeok.dexdio.widgets2

import org.eclipse.jface.resource.JFaceResources
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

object FigureTreeViewTest {

    case class Line(lineNum: Int) extends Tag
    case object Parameter extends Tag
    case object ParameterType extends Tag
    case object ParameterName extends Tag
    case object ParameterList extends Tag
    case class TypeTag(typeDescriptor: String) extends Tag
    case class MethodTag(methodName: String) extends Tag
    case object Punctuation extends Tag
    case class ParameterTag(methodName: String, index: Int) extends Tag
    sealed trait BracketTag extends Tag {
        def opposite: BracketTag
    }
    case class OpeningBracket(id: Int) extends BracketTag {
        def opposite = ClosingBracket(id)
    }
    case class ClosingBracket(id: Int) extends BracketTag {
        def opposite = OpeningBracket(id)
    }
    case object JavaStatement extends Tag
    case object MethodBody extends Tag
    case object MethodBodyContent extends Tag
    case object MethodDefinition extends Tag
    case object MethodSignature extends Tag

    private lazy val largerFont = TextWithFont(new Font(null, "Menlo", 50, SWT.NONE))

    def methodFigure(className: String, methodName: String) = Container(
        Seq(
            Container(
                Seq(
                    Container(
                        Seq(
                            TextLabel("int", TextNoDecoration, Set(TypeTag("type:I"))),
                            SpacingLabel(0, 1),
                            TextLabel(methodName, largerFont, Set(MethodTag(s"method:$className:$methodName"))),
                            TextLabel("(", TextNoDecoration, Set(Punctuation, OpeningBracket(1))),
                            Container(Seq(
                                Container(Seq(
                                    TextLabel("int", TextNoDecoration, Set(ParameterType, ParameterTag(s"$className:$methodName", 0), TypeTag("I"))),
                                    SpacingLabel(0, 1),
                                    TextLabel("a", TextNoDecoration, Set(ParameterName, ParameterTag(s"$className:$methodName", 0)))
                                ), Set(Parameter, ParameterTag(s"$className:$methodName", 0))),
                                TextLabel(", ", TextNoDecoration, Set(Punctuation)),
                                Container(Seq(
                                    TextLabel("String", TextNoDecoration, Set(ParameterType, ParameterTag(s"$className:$methodName", 1), TypeTag("Ljava/lang/String"))),
                                    SpacingLabel(0, 1),
                                    TextLabel("b", TextNoDecoration, Set(ParameterName, ParameterTag(s"$className:$methodName", 1)))
                                ), Set(Parameter, ParameterTag(s"$className:$methodName", 1)))
                            ), Set(ParameterList, ParameterTag(s"$className:$methodName", 1))),
                            TextLabel(")", TextNoDecoration, Set(Punctuation, ClosingBracket(1))),
                            SpacingLabel(0, 1)
                        ),
                        Set(MethodDefinition, MethodTag(s"$className:$methodName"))
                    ),

                    Container(
                        Seq(
                            TextLabel("{", TextNoDecoration, Set(Punctuation, OpeningBracket(2))),
                            Indented(
                                Container(Seq(
                                    Container(
                                        Seq(
                                            TextLabel("statement1", TextNoDecoration, Set(JavaStatement)),
                                            TextLabel(";", TextNoDecoration, Set(Punctuation))
                                        ),
                                        Set(JavaStatement)
                                    ),
                                    NewLine(),
                                    Container(
                                        Seq(
                                            TextLabel("statement2", TextNoDecoration, Set(JavaStatement)),
                                            TextLabel(";", TextNoDecoration, Set(Punctuation))
                                        ),
                                        Set(JavaStatement)
                                    ),
                                    NewLine(),
                                    Container(
                                        Seq(
                                            TextLabel("statement3", TextNoDecoration, Set(JavaStatement)),
                                            TextLabel(";", TextNoDecoration, Set(Punctuation))
                                        ),
                                        Set(JavaStatement)
                                    ),
                                    NewLine(),
                                    Container(
                                        Seq(
                                            TextLabel("very_very_long_long_long_long_long_statement_it_is_indeed", TextNoDecoration, Set(JavaStatement)),
                                            TextLabel(";", TextNoDecoration, Set(Punctuation))
                                        ),
                                        Set(JavaStatement)
                                    ),
                                    NewLine(),
                                    Container(
                                        ("very_very_long_long_long_long_long_statement_it_is_indeed".toCharArray map { c => TextLabel(c.toString, TextNoDecoration, Set(JavaStatement)) }).toSeq :+ TextLabel(";", TextNoDecoration, Set(Punctuation)),
                                        Set(JavaStatement)
                                    )
                                ), Set(MethodBodyContent, MethodTag(s"$className:$methodName")))
                            ),
                            TextLabel("}", TextNoDecoration, Set(Punctuation, ClosingBracket(2)))
                        ),
                        Set(MethodBody, MethodTag(s"$className:$methodName"))
                    )
                ),
                Set(MethodDefinition, MethodTag(s"$className:$methodName"))
            )
        ),
        Set()
    )

    /*
    import com.giyeok.dexdio.widgets2._
    import com.giyeok.dexdio.widgets2.FlatFigureStream._
    val methodAbc = methodFigure("aaa/bbb/ccc", "abc")
    val stream = FigureTreeViewTest.methodAbc.flatFigureStream
    val linesStream = stream.lines
    */

    def main(args: Array[String]): Unit = {
        val display = new Display()
        val shell = new Shell(display)

        val figure = {
            val methodFigures: Seq[Figure] = {
                def methodAt(idx: Int) = methodFigure("aaa/bbb/ccc", s"method$idx")
                methodAt(0) +: ((0 until 1) flatMap { i => Seq(NewLine(), NewLine(), methodAt(i)) })
            }
            Container(methodFigures, Set())
        }

        shell.setLayout(new FillLayout())
        shell.setBounds(100, 100, 800, 600)

        // println(figure.flatFigureStream.textRender)

        // new FigureTreeView(shell, SWT.NONE, TextLabel("hello", TextNoDecoration, Set()), Seq(), DrawingConfig(15, JFaceResources.getFont(JFaceResources.TEXT_FONT)))
        val systemFont = JFaceResources.getFont(JFaceResources.TEXT_FONT)
        val myFont = new Font(null, "Menlo", 30, SWT.ITALIC | SWT.BOLD)
        new FigureTreeView(shell, SWT.NONE, figure, Seq(), DrawingConfig(SpacingLabel(pixelWidth = 0, spaceCount = 2), myFont))

        shell.open()

        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }
}
