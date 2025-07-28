import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecret {
    
    static class Point {
        BigInteger x;
        BigInteger y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    public static void main(String[] args) {
        ShamirSecret solver = new ShamirSecret();
        
        // Process both test cases
        String[] testFiles = {"testcase1.json", "testcase2.json"};
        
        for (int i = 0; i < testFiles.length; i++) {
            try {
                System.out.println("Processing " + testFiles[i] + ":");
                BigInteger secret = solver.processTestCase(testFiles[i]);
                System.out.println("Secret for testcase " + (i + 1) + ": " + secret);
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error processing " + testFiles[i] + ": " + e.getMessage());
            }
        }
    }
    
    public BigInteger processTestCase(String filename) throws IOException {
        // Read JSON from file
        String jsonContent = readFile(filename);
        
        // Parse the JSON
        TestCase testCase = parseJSON(jsonContent);
        
        System.out.println("n = " + testCase.n + ", k = " + testCase.k);
        System.out.println("Polynomial degree m = " + (testCase.k - 1));
        
        // Decode Y values and create points
        List<Point> points = new ArrayList<>();
        for (Root root : testCase.roots) {
            BigInteger x = new BigInteger(root.key);
            BigInteger y = new BigInteger(root.value, root.base);
            points.add(new Point(x, y));
            
            System.out.println("Root: x=" + x + ", y=" + root.value + 
                             " (base " + root.base + ") = " + y + " (decimal)");
        }
        
        // Select first k points (since we don't need error detection)
        List<Point> selectedPoints = points.subList(0, testCase.k);
        
        // Find the secret using Lagrange interpolation
        BigInteger secret = lagrangeInterpolation(selectedPoints);
        
        return secret;
    }
    
    private String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    private BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger secret = BigInteger.ZERO;
        int k = points.size();
        
        System.out.println("\nUsing Lagrange Interpolation:");
        
        for (int i = 0; i < k; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    // For f(0): numerator *= (0 - x_j) = -x_j
                    numerator = numerator.multiply(points.get(j).x.negate());
                    // denominator *= (x_i - x_j)
                    denominator = denominator.multiply(
                        points.get(i).x.subtract(points.get(j).x)
                    );
                }
            }
            
            // Check if division is exact (should be for valid Shamir's shares)
            if (!points.get(i).y.multiply(numerator).remainder(denominator).equals(BigInteger.ZERO)) {
                System.err.println("Warning: Non-exact division detected at point " + (i+1));
            }
            
            // L_i(0) = numerator / denominator
            // term = y_i * L_i(0) = y_i * numerator / denominator
            BigInteger term = points.get(i).y.multiply(numerator).divide(denominator);
            secret = secret.add(term);
            
            System.out.println("Point " + (i+1) + ": " + points.get(i) + 
                             " contributes: " + term);
            System.out.println("  Numerator: " + numerator);
            System.out.println("  Denominator: " + denominator);
        }
        
        return secret;
    }
    
    // Simple JSON parser for the specific format
    private TestCase parseJSON(String json) {
        TestCase testCase = new TestCase();
        
        // Remove whitespace and outer braces
        json = json.replaceAll("\\s+", "");
        json = json.substring(1, json.length() - 1);
        
        // Split into main parts
        List<String> parts = splitJSON(json);
        
        for (String part : parts) {
            if (part.startsWith("\"keys\":")) {
                // Parse keys object
                String keysStr = part.substring(8); // Remove "keys":
                keysStr = keysStr.substring(1, keysStr.length() - 1); // Remove braces
                
                String[] keyParts = keysStr.split(",");
                for (String keyPart : keyParts) {
                    String[] kv = keyPart.split(":");
                    String key = kv[0].replaceAll("\"", "");
                    int value = Integer.parseInt(kv[1]);
                    
                    if (key.equals("n")) {
                        testCase.n = value;
                    } else if (key.equals("k")) {
                        testCase.k = value;
                    }
                }
            } else {
                // Parse individual roots
                String[] mainSplit = part.split(":", 2);
                String key = mainSplit[0].replaceAll("\"", "");
                String valueStr = mainSplit[1];
                
                // Parse the root object
                valueStr = valueStr.substring(1, valueStr.length() - 1); // Remove braces
                Root root = new Root();
                root.key = key;
                
                String[] rootParts = valueStr.split(",");
                for (String rootPart : rootParts) {
                    String[] kv = rootPart.split(":", 2);
                    String rootKey = kv[0].replaceAll("\"", "");
                    String rootValue = kv[1].replaceAll("\"", "");
                    
                    if (rootKey.equals("base")) {
                        root.base = Integer.parseInt(rootValue);
                    } else if (rootKey.equals("value")) {
                        root.value = rootValue;
                    }
                }
                
                testCase.roots.add(root);
            }
        }
        
        return testCase;
    }
    
    private List<String> splitJSON(String json) {
        List<String> parts = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            } else if (c == ',' && braceCount == 0) {
                parts.add(json.substring(start, i));
                start = i + 1;
            }
        }
        
        if (start < json.length()) {
            parts.add(json.substring(start));
        }
        
        return parts;
    }
    
    static class TestCase {
        int n;
        int k;
        List<Root> roots = new ArrayList<>();
    }
    
    static class Root {
        String key;
        int base;
        String value;
    }
}

// Alternative implementation using Matrix Method for verification
class MatrixMethod {
    
    public static BigInteger findSecretMatrix(List<ShamirSecret.Point> points) {
        int k = points.size();
        
        // Create the Vandermonde matrix for system Ax = y
        BigInteger[][] matrix = new BigInteger[k][k + 1]; // Augmented matrix
        
        for (int i = 0; i < k; i++) {
            BigInteger x = points.get(i).x;
            BigInteger y = points.get(i).y;
            
            // Fill the row: [x^(k-1), x^(k-2), ..., x^1, x^0] | y
            for (int j = 0; j < k; j++) {
                matrix[i][j] = x.pow(k - 1 - j);
            }
            matrix[i][k] = y; // Augmented part
        }
        
        // Solve using Gaussian elimination
        return gaussianElimination(matrix, k);
    }
    
    private static BigInteger gaussianElimination(BigInteger[][] matrix, int n) {
        // Forward elimination
        for (int i = 0; i < n; i++) {
            // Find pivot
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (matrix[k][i].abs().compareTo(matrix[maxRow][i].abs()) > 0) {
                    maxRow = k;
                }
            }
            
            // Swap rows
            BigInteger[] temp = matrix[maxRow];
            matrix[maxRow] = matrix[i];
            matrix[i] = temp;
            
            // Make all rows below this one 0 in current column
            for (int k = i + 1; k < n; k++) {
                BigInteger factor = matrix[k][i].divide(matrix[i][i]);
                for (int j = i; j <= n; j++) {
                    matrix[k][j] = matrix[k][j].subtract(factor.multiply(matrix[i][j]));
                }
            }
        }
        
        // Back substitution
        BigInteger[] solution = new BigInteger[n];
        for (int i = n - 1; i >= 0; i--) {
            solution[i] = matrix[i][n];
            for (int j = i + 1; j < n; j++) {
                solution[i] = solution[i].subtract(matrix[i][j].multiply(solution[j]));
            }
            solution[i] = solution[i].divide(matrix[i][i]);
        }
        
        // Return the constant term (last coefficient)
        return solution[n - 1];
    }
}