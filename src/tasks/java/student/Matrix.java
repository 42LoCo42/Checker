package student;

import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Leon Schumacher
 */
public class Matrix {
	double[][] values;
	String label;

	public Matrix() {
		this("E", 3, 3);
		setIdentity();
	}

	public Matrix(String label, int rows, int cols) {
		this(label, rows, cols, 0);
	}

	public Matrix(String label, int rows, int cols, double value) {
		errIfIllegal(rows, cols);
		this.label = label;

		values = new double[rows][cols];
		for(var row : values) {
			Arrays.fill(row, value);
		}
	}

	public Matrix(String label, String definition) {
		try {
			var numbers =
				Arrays.stream(definition.split(";"))
					.map(s ->
						Arrays.stream(s.trim().split(" "))
							.map(Double::valueOf)
							.toList()
					)
					.toList();

			this.label = label;
			this.values = new double[numbers.size()][numbers.get(0).size()];

			for(int row = 0; row < rows(); row++) {
				var thisRow = numbers.get(row);
				for(int col = 0; col < columns(); col++) {
					values[row][col] = thisRow.get(col);
				}
			}
		} catch(Exception e) {
			throw new IllegalArgumentException("Ungültiger Wertestring");
		}
	}

	private void errIfIllegal(int row, int col) {
		if(
			row < 0 || col < 0
				|| (values != null && (row >= rows() || col >= columns()))
		)
			throw new IllegalArgumentException("The matrix must at least be 1x1");
	}

	public double[][] getValues() {
		return values;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int rows() {
		return values.length;
	}

	public int columns() {
		return values[0].length;
	}

	public double getValue(int row, int col) {
		errIfIllegal(row, col);
		return values[row][col];
	}

	public void setValue(int row, int col, double value) {
		errIfIllegal(row, col);
		values[row][col] = value;
	}

	public void setIdentity() {
		if(rows() != columns()) return;
		for(var row : values) Arrays.fill(row, 0);
		for(int i = 0; i < rows(); i++) {
			values[i][i] = 1;
		}
	}

	public Matrix mul(double scalar) {
		var result = new Matrix("Result", rows(), columns());
		for(int row = 0; row < rows(); row++) {
			for(int col = 0; col < columns(); col++) {
				result.values[row][col] = values[row][col] * scalar;
			}
		}
		return result;
	}

	public Matrix add(Matrix other) {
		if(other.rows() != rows() || other.columns() != columns())
			throw new IllegalArgumentException(String.format(
				"Other matrix must be equally sized (has %dx%d, needs %dx%d)",
				other.rows(), (other.columns()),
				rows(), columns()
			));

		var result = new Matrix("Result", rows(), columns());
		for(int row = 0; row < rows(); row++) {
			for(int col = 0; col < columns(); col++) {
				result.values[row][col] = values[row][col] + other.values[row][col];
			}
		}
		return result;
	}

	public Matrix mul(Matrix other) {
		if(columns() != other.rows())
			throw new IllegalArgumentException(String.format(
				"Other matrix must have %d rows",
				columns()
			));

		var result = new Matrix("Result", rows(), other.columns());
		for(int row = 0; row < result.rows(); row++) {
			for(int col = 0; col < result.columns(); col++) {
				var sum = 0.0;
				for(int i = 0; i < columns(); i++) {
					sum += values[row][i] * other.values[i][col];
				}
				result.values[row][col] = sum;
			}
		}
		return result;
	}

	public Matrix transpose() {
		var result = new Matrix("Result", columns(), rows());
		for(int row = 0; row < rows(); row++) {
			for(int col = 0; col < columns(); col++) {
				result.values[col][row] = values[row][col];
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return Arrays.stream(values).map(row ->
			Arrays.stream(row).mapToObj(String::valueOf).collect(Collectors.joining(" "))
		).collect(Collectors.joining(";"));
	}

	private void print() {
		for(var row : values) {
			for(var val : row) {
				System.out.print(val + " ");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		var menu = """
			1) Matrix * Skalar
			2) Matrix + Matrix
			3) Matrix * Matrix
			4) Matrix transponieren
			5) Beenden
			Eingabe der gewünschten Operation:\s""";

		try(var in = new Scanner(System.in)) {
			Function<Void, Matrix> promptMatrix = a -> {
				System.out.print("Matrix eingeben: ");
				return new Matrix("", in.nextLine());
			};

			Function<Void, Double> promptDouble = a -> {
				System.out.print("Double eingeben: ");
				return Double.valueOf(in.nextLine());
			};

			Function<Matrix, Void> printResult = m -> {
				System.out.println("Ergebnis: " + m.toString());
				return null;
			};

			while(true) {
				System.out.print(menu);
				switch(Integer.parseInt(in.nextLine())) {
					case 1 -> {
						var matrix = promptMatrix.apply(null);
						var scalar = promptDouble.apply(null);
						printResult.apply(matrix.mul(scalar));
					}
					case 2 -> {
						var m1 = promptMatrix.apply(null);
						var m2 = promptMatrix.apply(null);
						printResult.apply(m1.add(m2));
					}
					case 3 -> {
						var m1 = promptMatrix.apply(null);
						var m2 = promptMatrix.apply(null);
						printResult.apply(m1.mul(m2));
					}
					case 4 -> {
						var matrix = promptMatrix.apply(null);
						printResult.apply(matrix.transpose());
					}
					case 5 -> System.exit(0);
				}
			}
		}
	}
}
