package leonsch

import javax.swing.JFrame

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
class GUI: JFrame() {
	init {
		title = "test"
		setSize(800, 600)
		isVisible = true
		defaultCloseOperation = EXIT_ON_CLOSE
	}
}