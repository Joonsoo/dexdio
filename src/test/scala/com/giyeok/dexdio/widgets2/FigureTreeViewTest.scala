package com.giyeok.dexdio.widgets2

import com.giyeok.dexdio.widgets2.FlatFigureStream._
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.swt.SWT
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

    val methodAbc = Container(
        Seq(
            Container(
                Seq(
                    Container(
                        Seq(
                            TextLabel("int", TextNoDecoration, Set(TypeTag("type:I"))),
                            SpacingLabel(0, 1),
                            TextLabel("abc", TextNoDecoration, Set(MethodTag("method:com/abc/aaa:abc"))),
                            TextLabel("(", TextNoDecoration, Set(Punctuation, OpeningBracket(1))),
                            Container(Seq(
                                Container(Seq(
                                    TextLabel("int", TextNoDecoration, Set(ParameterType, ParameterTag("com/abc/aaa:abc", 0), TypeTag("I"))),
                                    SpacingLabel(0, 1),
                                    TextLabel("a", TextNoDecoration, Set(ParameterName, ParameterTag("com/abc/aaa:abc", 0)))
                                ), Set(Parameter, ParameterTag("com/abc/aaa:abc", 0))),
                                TextLabel(", ", TextNoDecoration, Set(Punctuation)),
                                Container(Seq(
                                    TextLabel("String", TextNoDecoration, Set(ParameterType, ParameterTag("com/abc/aaa:abc", 1), TypeTag("Ljava/lang/String"))),
                                    SpacingLabel(0, 1),
                                    TextLabel("b", TextNoDecoration, Set(ParameterName, ParameterTag("com/abc/aaa/:abc", 1)))
                                ), Set(Parameter, ParameterTag("com/abc/aaa:abc", 1)))
                            ), Set(ParameterList, ParameterTag("com/abc/aaa:abc", 1))),
                            TextLabel(")", TextNoDecoration, Set(Punctuation, ClosingBracket(1))),
                            SpacingLabel(0, 1)
                        ),
                        Set(MethodDefinition, MethodTag("com/abc/aaa:abc"))
                    ),

                    Container(
                        Seq(
                            TextLabel("{", TextNoDecoration, Set(Punctuation, OpeningBracket(2))),
                            Indented(Deferred(
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
                                    )
                                ), Set(MethodBodyContent, MethodTag("com/abc/aaa:abc")))
                            )),
                            TextLabel("}", TextNoDecoration, Set(Punctuation, ClosingBracket(2)))
                        ),
                        Set(MethodBody, MethodTag("com/abc/aaa:abc"))
                    )
                ),
                Set(MethodDefinition, MethodTag("com/abc/aaa:abc"))
            )
        ),
        Set()
    )

    /*
    import com.giyeok.dexdio.widgets2._
    import com.giyeok.dexdio.widgets2.FlatFigureStream._
    */
    val stream = FigureTreeViewTest.methodAbc.flatFigureStream
    val linesStream = stream.lines

    def main(args: Array[String]): Unit = {
        val display = new Display()
        val shell = new Shell(display)

        shell.setLayout(new FillLayout())
        shell.setBounds(100, 100, 800, 600)

        println(methodAbc.flatFigureStream.textRender)

        // new FigureTreeView(shell, SWT.NONE, TextLabel("hello", TextNoDecoration, Set()), Seq(), DrawingConfig(15, JFaceResources.getFont(JFaceResources.TEXT_FONT)))
        new FigureTreeView(shell, SWT.NONE, methodAbc, Seq(), DrawingConfig(15, JFaceResources.getFont(JFaceResources.TEXT_FONT)))

        shell.open()

        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }
}
