package com.giyeok.dexdio

import com.giyeok.dexdio.dexreader.DalvikExecutable
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.MessageBox
import com.giyeok.dexdio.model2.DexProgram
import com.giyeok.dexdio.model2.DexDefinedClass
import com.giyeok.dexdio.model2.DexDefinedClass
import com.giyeok.dexdio.model2.DexMarkerClass
import com.giyeok.dexdio.model2.DexInheritedField
import com.giyeok.dexdio.model2.DexStaticField
import com.giyeok.dexdio.model2.DexInstanceField

object Main {
    def main(args: Array[String]): Unit = {
        Try {
            Seq(DalvikExecutable.load("./samples/mysample.dex"))
        } match {
            case Success(dexes) =>
                val display = new Display()

                val mainViews = dexes map { new MainView(_, new Shell(display)) }

                // val dexP = new DexProgram(dexes.head)

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
