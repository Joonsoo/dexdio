package com.giyeok.dexdio

import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import com.giyeok.dexdio.dexreader.DalvikExecutable
import com.giyeok.dexdio.dexreader.RandomAccessibleByteArray
import com.giyeok.dexdio.dexreader.RandomAccessibleFile
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell

object Main {
    def dexesOfApk(apkFile: File): Seq[(String, Array[Byte])] = {
        val zis = new ZipInputStream(new FileInputStream(apkFile))
        var zipEntry: ZipEntry = zis.getNextEntry
        var dexes: List[(String, Array[Byte])] = List()

        while (zipEntry != null) {
            val entryName = zipEntry.getName
            if ((entryName startsWith "classes") && (entryName endsWith ".dex")) {
                val dexSize = zipEntry.getSize
                // println(s"$entryName: $dexSize")
                assert(dexSize < Int.MaxValue)
                val bytes = new Array[Byte](dexSize.toInt + 1)
                var readBytes = zis.read(bytes)
                var length: Int = 0
                while (readBytes >= 0) {
                    length += readBytes
                    assert(bytes.length > length)
                    readBytes = zis.read(bytes, length, bytes.length - length)
                }
                // assert zis is at the end
                assert(zis.read() < 0)
                val bytesTrimmed = new Array[Byte](dexSize.toInt)
                System.arraycopy(bytes, 0, bytesTrimmed, 0, length)
                dexes +:= (entryName, bytesTrimmed)
            }
            zipEntry = zis.getNextEntry
        }
        dexes
    }

    def open(parent: Shell): IndexedSeq[DalvikExecutable] = {
        val fileDialog = new FileDialog(parent, SWT.OPEN | SWT.MULTI)
        fileDialog.setFilterExtensions(Seq("*.apk;*.dex").toArray)
        fileDialog.open()

        val path = fileDialog.getFilterPath
        val files = fileDialog.getFileNames.toSeq
        println(path, files)
        if (files.isEmpty) {
            System.exit(1)
            throw new Exception("No file selected")
        } else if (files.length == 1 && (files.head endsWith ".apk")) {
            val filename = files.head
            dexesOfApk(new File(path, filename)).toVector map { dex =>
                DalvikExecutable.load(s"$filename:${dex._1}", new RandomAccessibleByteArray(dex._2))
            }
        } else {
            if (!(files forall { _.endsWith(".dex") })) {
                val errorMessage = new MessageBox(parent)
                errorMessage.setMessage("1 apk or multiple dexes")
                errorMessage.open()
                System.exit(1)
                throw new Exception("Mixed apk and dexes")
            }
            files.toVector map { filename =>
                DalvikExecutable.load(filename, new RandomAccessibleFile(new File(path, filename)))
            }
        }
    }

    def main(args: Array[String]): Unit = {
        val display = new Display()
        val shell = new Shell(display)

        // TODO 파일 선택 창 추가
        Try {
            open(shell)
        } match {
            case Success(dexes) =>

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
                val program1 = measure("                                      model1") { new com.giyeok.dexdio.model.DexProgram(dexes.head) }
                val program2 = measure("                                      model2") { new com.giyeok.dexdio.model2.DexProgram(dexes) }
                // TODO support multi dex
                new MainView(dexes.head, program1, shell)

                while (!shell.isDisposed) {
                    if (!display.readAndDispatch()) {
                        display.sleep()
                    }
                }
                display.dispose()
            case Failure(error) =>
                error.printStackTrace()

                val msg = new MessageBox(shell)
                msg.setMessage(s"Error while reading dex: $error")
                msg.open()
        }
    }
}
