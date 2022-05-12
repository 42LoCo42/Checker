import leonsch.*
import java.net.URL
import java.net.URLClassLoader
import kotlin.random.Random

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
typealias Values = Array<DoubleArray>

fun run(classDir: URL) {
	fun valuesToString(values: Values) =
		"\n" +
			values.joinToString("\n") { row ->
				row.joinToString { it.toString() }
			} + "\n"

	fun mkRandomValues(rowCount: Int, colCount: Int) =
		Array(rowCount) {
			DoubleArray(colCount) { Random.nextDouble() }
		}

	fun mkMatrixString(values: Values) =
		values.joinToString(";") {
			it.joinToString(" ")
		}

	with(URLClassLoader(arrayOf(classDir)).loadClass("student.Matrix")) {
		val twoStrings = constructor(String::class.java, String::class.java)

		val values = field("values", Values::class.java)
		val toString = method("toString", String::class.java)

		@Suppress("UNCHECKED_CAST")
		fun values(matrix: Any?) = values[matrix] as Values

		fun hasValues(
			matrix: Any?,
			values: Values,
			name: String = "Wertevergleich"
		) = ValueTest(
			name,
			{ values(it) },
			{ values },
			{ v1, v2 -> v1.contentDeepEquals(v2) },
			{ valuesToString(it) }
		).run(matrix)

		val randomLabel = randomString()
		val randomRowCount = Random.nextInt(1, 10)
		val randomColCount = Random.nextInt(1, 10)

		val randomMatrixValues = mkRandomValues(randomRowCount, randomColCount)
		val randomMatrixString = mkMatrixString(randomMatrixValues)
		val randomMatrix = twoStrings.newInstance(randomLabel, randomMatrixString)
		hasValues(randomMatrix, randomMatrixValues, "Matrix aus Wertestring")

		ExceptionTest(
			"Matrix aus nicht-numerischem Wertestring",
			IllegalArgumentException::class.java
		) { twoStrings.newInstance("", "foobarbaz") }.run(null)

		ExceptionTest(
			"Matrix aus verschieden langen Wertestrings",
			IllegalArgumentException::class.java
		) { twoStrings.newInstance("", "1 2; 3; 4 5 6 7") }.run(null)

		ValueTest(
			"Wertestring aus Matrix",
			{ toString.invoke(it) },
			{ randomMatrixString }
		).run(randomMatrix)

		val readMainMenu = Interaction(
			"Hauptmenü lesen",
			*("""
			1) Matrix * Skalar
			2) Matrix + Matrix
			3) Matrix * Matrix
			4) Matrix transponieren
			5) Beenden
		""".trimIndent().lines().map { Operation(IOType.RecvLine, Regex.escape(it)) } +
				listOf(Operation(IOType.RecvMost, "Eingabe der gewünschten Operation: "))).toTypedArray()
		)

		val readPrompt = Interaction(
			"Prompt lesen",
			Operation(IOType.RecvMost, "[^:]*: ?")
		)

		val readResult = { expect: String ->
			Interaction(
				"Ergebnis lesen",
				Operation(IOType.RecvLine, "[^:]*: ?[0-9.; ]+") {
					val got = it.replace(Regex("^[^:]*: ?"), "")
					if(got == expect) {
						print(success("Result matches"))
					} else {
						print(error("Incorrect result!"))
						print("Got:    $got")
						print("Expect: $expect")
					}
				}
			)
		}

		val scalarMultResult = randomMatrixValues.map { row ->
			row.map { it * 3 }.toDoubleArray()
		}.toTypedArray()

		val scalarMult = Interaction(
			"Skalarmultiplikation",
			readMainMenu,
			Operation(IOType.Send, "1\n"),
			readPrompt,
			Operation(IOType.Send, randomMatrixString + "\n"),
			readPrompt,
			Operation(IOType.Send, "3\n"),
			readResult(mkMatrixString(scalarMultResult)),
		)

		val summandMatrixValues = mkRandomValues(randomRowCount, randomColCount)
		val summandMatrixString = mkMatrixString(summandMatrixValues)
		val addResult = randomMatrixValues.zip(summandMatrixValues).map { (r1, r2) ->
			r1.zip(r2).map { (v1, v2) -> v1 + v2 }.toDoubleArray()
		}.toTypedArray()

		val add = Interaction(
			"Matrizenaddition",
			readMainMenu,
			Operation(IOType.Send, "2\n"),
			readPrompt,
			Operation(IOType.Send, randomMatrixString + "\n"),
			readPrompt,
			Operation(IOType.Send, summandMatrixString + "\n"),
			readResult(mkMatrixString(addResult)),
		)

		val factorMatrixValues = mkRandomValues(randomColCount, Random.nextInt(1, 10))
		val factorMatrixString = mkMatrixString(factorMatrixValues)
		val mulResult = Array(randomRowCount) { row ->
			DoubleArray(factorMatrixValues[0].size) { col ->
				var sum = 0.0
				for(k in 0 until randomColCount) {
					sum += randomMatrixValues[row][k] * factorMatrixValues[k][col]
				}
				sum
			}
		}

		val mul = Interaction(
			"Matrizenmultiplikation",
			readMainMenu,
			Operation(IOType.Send, "3\n"),
			readPrompt,
			Operation(IOType.Send, randomMatrixString + "\n"),
			readPrompt,
			Operation(IOType.Send, factorMatrixString + "\n"),
			readResult(mkMatrixString(mulResult)),
		)

		val transposeResult = Array(randomColCount) { row ->
			DoubleArray(randomRowCount) { col ->
				randomMatrixValues[col][row]
			}
		}

		val transpose = Interaction(
			"Transponieren",
			readMainMenu,
			Operation(IOType.Send, "4\n"),
			readPrompt,
			Operation(IOType.Send, randomMatrixString + "\n"),
			readResult(mkMatrixString(transposeResult)),
		)

		val stop = Interaction(
			"Programm beenden",
			readMainMenu,
			Operation(IOType.Send, "5\n"),
		)

		interactiveTest(
			this,
			scalarMult,
			add,
			mul,
			transpose,
			stop,
		)
	}
}
