import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Lab1{

    static class MatrixOperations {
        static int n;
        static double[][] A, A1, A2, B2, C2;
        static double[] b, b1, c1;

        static void generateData(int size) {
            n = size;
            Random rand = new Random();

            A = new double[n][n];
            A1 = new double[n][n];
            A2 = new double[n][n];
            B2 = new double[n][n];
            C2 = new double[n][n];

            b = new double[n];
            b1 = new double[n];
            c1 = new double[n];

            for (int i = 0; i < n; i++) {
                b[i] = 6.0 / (i * i + 1); //b_i = 6/i^2
                b1[i] = 6.0 / (i * i + 1); //також
                c1[i] = rand.nextDouble();

                for (int j = 0; j < n; j++) {
                    A[i][j] = rand.nextDouble();
                    A1[i][j] = rand.nextDouble();
                    A2[i][j] = rand.nextDouble();
                    B2[i][j] = rand.nextDouble();
                    C2[i][j] = 1.0 / Math.pow(i + j + 1, 3); //значення за формулою 1 / (i + j + 1)^3
                }
            }
        }

        static double[] matrixVM(double[][] matrix, double[] vector) {
            double[] result = new double[n];
            for (int i = 0; i < n; i++) {
                result[i] = 0;
                for (int j = 0; j < n; j++) {
                    result[i] += matrix[i][j] * vector[j];
                }
            }
            return result;
        }

        static double[][] matrixAp(double[][] mat1, double[][] mat2, boolean add) {
            double[][] result = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    result[i][j] = add ? (mat1[i][j] + mat2[i][j]) : (mat1[i][j] - mat2[i][j]);
                }
            }
            return result;
        }

        static void parallelComputation() {
            ExecutorService executor = Executors.newFixedThreadPool(3);

            long startTime = System.currentTimeMillis();

            final double[] y1 = new double[n];
            final double[] y2 = new double[n];
            final double[][] Y3 = new double[n][n];

            // Завдання 1: y1 = A * b
            executor.submit(() -> {
                double[] temp_y1 = matrixVM(A, b);
                System.arraycopy(temp_y1, 0, y1, 0, n);
            });

            // Завдання 2: y2 = A1 * (6 * b1 - c1)
            executor.submit(() -> {
                double[] b1_minus_c1 = new double[n];
                for (int i = 0; i < n; i++) {
                    b1_minus_c1[i] = 6 * b1[i] - c1[i];
                }
                double[] temp_y2 = matrixVM(A1, b1_minus_c1);
                System.arraycopy(temp_y2, 0, y2, 0, n);
            });

            // Завдання 3: Y3 = A2 * (10 * B2 + C2)
            executor.submit(() -> {
                double[][] B2_plus_C2 = matrixAp(B2, C2, true);
                double[][] temp_Y3 = matrixAp(A2, B2_plus_C2, false);
                for (int i = 0; i < n; i++) {
                    System.arraycopy(temp_Y3[i], 0, Y3[i], 0, n);
                }
            });

            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // обрахунок фінального результату
            double x = 0;
            for (int i = 0; i < n; i++) {
                x += y2[i] * y1[i] * Y3[i][i] + y1[i] * y2[i] * Y3[i][i] + Y3[i][i];
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Результат x: " + x);
            System.out.println("Час: " + (endTime - startTime) + " ms");
        }
    }

    public static void main(String[] args) {
        int size = 1000;
        MatrixOperations.generateData(size);
        MatrixOperations.parallelComputation();
    }
}
