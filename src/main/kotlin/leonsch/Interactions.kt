package leonsch

import java.io.BufferedReader
import java.io.File
import java.io.OutputStream

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
private var currentIndent = 0
fun print(string: String) {
	string.lines().forEach { println("\t".repeat(currentIndent) + it) }
}

fun special(text: String, set: String, clr: String) =
	set + text.replace("\n", "$clr\n$set") + clr

fun highlight(text: String) = special(text, "\u001b[7m", "\u001b[27m")
fun success(text: String) = special(text, "\u001b[32m", "\u001b[39m")
fun error(text: String) = special(text, "\u001b[31m", "\u001b[39m")

interface Interactive {
	fun run(stdin: OutputStream, stdout: BufferedReader)
}

enum class IOType {
	Nothing,
	Send,
	RecvLine,
	RecvMost,
	RecvEOF
}

class Operation(
	private val ioType: IOType,
	private val content: String,
	private val receiver: (String) -> Unit = {},
): Interactive {
	override fun run(stdin: OutputStream, stdout: BufferedReader) {
		print("Running operation ${ioType.name} ${highlight(content)}")

		if(ioType == IOType.Nothing) return
		if(ioType == IOType.Send) {
			stdin.write(content.toByteArray())
			stdin.flush()
			return
		}

		val result = when(ioType) {
			IOType.RecvLine -> stdout.readLine()
			IOType.RecvMost -> {
				while(!stdout.ready()) {
					Thread.sleep(125)
				}
				collectWhile({ stdout.ready() }) { stdout.read().toChar() }.joinToString("")
			}
			IOType.RecvEOF -> stdout.readText()
			else -> throw Exception("unreachable")
		}

		if(result == null || !result.matches(Regex(content))) {
			print(error("Unexpected result"))
			print("Got:    ${highlight(result)}")
			print("Expect: ${highlight(content)}")
		}

		receiver(result)
	}
}

class Interaction(
	private val name: String,
	private vararg val interactives: Interactive,
): Interactive {
	override fun run(
		stdin: OutputStream,
		stdout: BufferedReader,
	) {
		print("Running interaction $name")
		currentIndent++
		interactives.forEach {
			it.run(stdin, stdout)
		}
		currentIndent--
	}
}

fun interactiveTest(
	clazz: Class<*>,
	vararg interactions: Interaction,
) {
	val process = ProcessBuilder("stdbuf", "-o0", "java", clazz.name)
		.directory(File(System.getProperty("user.dir")))
		.start()
	val stdin = process.outputStream
	val stdout = process.inputStream.bufferedReader()
	val stderr = process.errorStream

	interactions.forEach {
		it.run(stdin, stdout)
	}

	println("Errors:\n" + stderr.bufferedReader().readText())
}