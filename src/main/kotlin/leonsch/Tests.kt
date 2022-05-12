package leonsch

import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
fun runTestGroup(
	name: String,
	subject: Any?,
	vararg tests: Test,
) {
	tests.forEach {
		it.name = "$name → " + it.name
		it.run(subject)
	}
}

open class Test(
	var name: String,
	val condition: (Any?) -> Boolean,
	val cause: (Any?) -> String,
) {
	fun run(subject: Any?) {
		println(
			try {
				if(condition(subject)) {
					"[ OK ] $name"
				} else {
					"[FAIL] $name:\n\t${cause(subject)}"
				}
			} catch(e: Exception) {
				"[ERR ] $name:\n\tFehler beim Ausführen: ${e.localizedMessage}"
			}
		)
	}
}

open class ValueTest<V>(
	name: String,
	val accessor: (Any?) -> V,
	private val requested: (Any?) -> V,
	val comparator: (V, V) -> Boolean = { v1, v2 -> v1 == v2 },
	val printer: (V) -> String = { it.toString() }
): Test(
	name,
	{ comparator(accessor(it), requested(it)) },
	{ "Werte stimmen nicht überein: ${printer(accessor(it))} != ${printer(requested(it))}" }
)

class ExceptionTest(
	name: String,
	private val exceptionType: Class<out Throwable>,
	val generator: (Any?) -> Any?
): ValueTest<Class<out Throwable>?>(
	name,
	{
		try {
			generator(it)
			null
		} catch(e: InvocationTargetException) {
			e.targetException::class.java
		}
	},
	{ exceptionType }
)