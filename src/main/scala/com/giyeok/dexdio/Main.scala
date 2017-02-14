package com.giyeok.dexdio

import com.giyeok.dexdio.dexreader.DalvikExecutable
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.MessageBox

object Main {
    def main(args: Array[String]): Unit = {
        // TODO 파일 선택 창 추가
        Try {
            lazy val kakaotalk = Vector(
                DalvikExecutable.load("./samples/Kakaotalk_6.0.1/classes.dex"),
                DalvikExecutable.load("./samples/Kakaotalk_6.0.1/classes2.dex")
            )
            lazy val mysample = Vector(
                DalvikExecutable.load("./samples/mysample.dex")
            )
            kakaotalk
        } match {
            case Success(dexes) =>
                val display = new Display()

                def measure[T](msg: String)(block: => T): T = {
                    val time = System.currentTimeMillis()
                    val result = block
                    println(s"$msg: ${System.currentTimeMillis() - time}")
                    result
                }

                //                val dex = dexes.head
                //                measure("total") {
                //                    (0 until 10000) foreach { _ =>
                //                        // measure("model1") { new com.giyeok.dexdio.model.DexProgram(dex) }
                //                        measure("model2") { new com.giyeok.dexdio.model2.DexProgram(dex) }
                //                    }
                //                }
                val program2 = measure("                                      model2") { new com.giyeok.dexdio.model2.DexProgram(dexes) }
                val program1 = measure("                                      model1") { new com.giyeok.dexdio.model.DexProgram(dexes.head) }
                // TODO support multi dex
                val shell = new Shell(display)
                new MainView(dexes.head, program1, shell)

                while (!shell.isDisposed) {
                    if (!display.readAndDispatch()) {
                        display.sleep()
                    }
                }
                display.dispose()
            case Failure(error) =>
                error.printStackTrace()

                val msg = new MessageBox(null)
                msg.setMessage("Error while reading dex")
                msg.open()
        }
    }
}
