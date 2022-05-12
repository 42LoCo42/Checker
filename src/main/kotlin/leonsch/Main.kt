package leonsch

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.net.URLClassLoader

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
fun main(args: Array<String>) {
	if(args.isEmpty()) {
		GUI()
	} else if(args.size == 2) {
		runTest(File(args[0]), File(args[1]))
	} else {
		System.err.printf(
			"Usage: %s [<test file> <build directory>]\n",
			System.getProperty("program.name")
		)
	}
}

fun runTest(testFile: File, classDir: File) {
	try {
		System.setProperty("user.dir", classDir.absolutePath)
		with(URLClassLoader(arrayOf(testFile.parentFile.toURI().toURL()))) {
			loadClass(testFile.nameWithoutExtension)
				.method("run", Void.TYPE, URL::class.java)
				.invoke(null, classDir.toURI().toURL())
		}
	} catch(e: InvocationTargetException) {
		e.printStackTrace()
	}
}