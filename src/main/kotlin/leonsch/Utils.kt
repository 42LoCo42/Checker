package leonsch

import kotlin.random.Random

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
fun randomString(length: Int = 16) =
	(1 .. length)
		.map { Random.nextInt('a'.code, 'z'.code).toChar() }
		.joinToString("")

fun <T> collectWhile(predicate: () -> Boolean, from: () -> T): List<T> {
	val result = mutableListOf<T>()
	while(predicate()) {
		result.add(from())
	}
	return result
}