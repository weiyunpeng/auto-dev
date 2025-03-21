/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.unitmesh.devti.language.compiler.service

import cc.unitmesh.devti.AutoDevNotifications
import cc.unitmesh.devti.gui.AutoDevToolWindowFactory
import cc.unitmesh.devti.gui.chat.message.ChatActionType
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.text.Strings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.StringWriter
import java.io.Writer
import kotlin.text.Charsets.UTF_8

class ProcessExecutor(val project: Project) {
    suspend fun executeCode(project: Project, code: String, dispatcher: CoroutineDispatcher): String? {
        val outputWriter = StringWriter()
        val errWriter = StringWriter()

        outputWriter.use {
            val exitCode = exec(code, outputWriter, errWriter, dispatcher)
            val stdOutput = outputWriter.toString()
            val errOutput = errWriter.toString()

            return if (exitCode == 0) {
                AutoDevNotifications.notify(project, "Shell command $code executed successfully")
                stdOutput
            } else {
                AutoDevToolWindowFactory.Companion.sendToSketchToolWindow(project, ChatActionType.SKETCH) { ui, _ ->
                    ui.putText("Error executing shell command: \n```bash\n$errOutput\n```")
                }
                "Error executing shell command: $errOutput"
            }
        }
    }

    /**
     * Base on SqliteCliClientImpl
     */
    suspend fun exec(shellScript: String,
                     stdWriter: Writer,
                     errWriter: Writer,
                     dispatcher: CoroutineDispatcher): Int = withContext(dispatcher) {
        val process = createProcess(shellScript)

        val errOutput = async { consumeProcessOutput(process.errorStream, errWriter, process, dispatcher) }
        val stdOutput = async { consumeProcessOutput(process.inputStream, stdWriter, process, dispatcher) }

        val exitCode = async { process.waitFor() }
        stdOutput.await()
        errOutput.await()
        exitCode.await()
    }


    /**
     * for share process
     */
    fun createInteractiveProcess(): Process {
        val commandLine = PtyCommandLine()
        commandLine.withConsoleMode(false)
        commandLine.withUnixOpenTtyToPreserveOutputAfterTermination(true)
        commandLine.withInitialColumns(240)
        commandLine.withInitialRows(80)
        commandLine.withEnvironment("TERM", "dumb")
        commandLine.withEnvironment("BASH_SILENCE_DEPRECATION_WARNING", "1")
        commandLine.withEnvironment("GIT_PAGER", "cat")
        val commands: List<String> = listOf("bash", "--noprofile", "--norc", "-i")
        return commandLine.startProcessWithPty(commands)
    }

    private fun createProcess(shellScript: String): Process {
        val basedir = ProjectManager.getInstance().openProjects.firstOrNull()?.basePath
        val commandLine = PtyCommandLine()
        commandLine.withConsoleMode(false)
        commandLine.withUnixOpenTtyToPreserveOutputAfterTermination(true)
        commandLine.withInitialColumns(240)
        commandLine.withInitialRows(80)
        commandLine.withEnvironment("TERM", "dumb")
        commandLine.withEnvironment("BASH_SILENCE_DEPRECATION_WARNING", "1")
        commandLine.withEnvironment("GIT_PAGER", "cat")
        val commands: List<String> = listOf("bash", "--noprofile", "--norc", "-c", formatCommand(shellScript))


        if (basedir != null) {
            commandLine.withWorkDirectory(basedir)
        }

        return commandLine.startProcessWithPty(commands)
    }


    private fun formatCommand(command: String): String {
        return "{ $command; EXIT_CODE=$?; } 2>&1 && echo \"EXIT_CODE: ${'$'}EXIT_CODE\""
    }

    private fun feedProcessInput(outputStream: OutputStream, inputLines: List<String>) {
        outputStream.writer(UTF_8).use { writer ->
            inputLines.forEach { line ->
                writer.write(line + System.lineSeparator())
                writer.flush()
            }
        }
    }

    private suspend fun consumeProcessOutput(source: InputStream?,
                                             outputWriter: Writer,
                                             process: Process,
                                             dispatcher: CoroutineDispatcher) = withContext(dispatcher) {
        if (source == null) return@withContext

        var isFirstLine = true
        BufferedReader(InputStreamReader(source, UTF_8.name())).use { reader ->
            do {
                val line = reader.readLine()
                if (Strings.isNotEmpty(line)) {
                    if (!isFirstLine) outputWriter.append(System.lineSeparator())
                    isFirstLine = false
                    outputWriter.append(line)
                }
                else {
                    yield()
                }
                ensureActive()
            }
            while (process.isAlive || line != null)
        }
    }
}
