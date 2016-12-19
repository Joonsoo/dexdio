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
        Try {
            Seq(DalvikExecutable.load("./samples/mysample.dex"))
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
                val mainViews = dexes map { dex =>
                    val program1 = measure("                                      model1") { new com.giyeok.dexdio.model.DexProgram(dex) }
                    val program2 = measure("                                      model2") { new com.giyeok.dexdio.model2.DexProgram(dexes.head) }

                    new MainView(dex, program1, new Shell(display))
                }

                while (!(mainViews forall { _.getShell().isDisposed() })) {
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
