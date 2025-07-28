# Shamir's Secret Sharing Implementation

A robust Java implementation of Shamir's Secret Sharing scheme that reconstructs secrets from polynomial shares using Lagrange interpolation and matrix methods.

## Overview

Shamir's Secret Sharing is a cryptographic algorithm that divides a secret into multiple shares, where a minimum threshold of shares (k) is required to reconstruct the original secret. This implementation processes JSON test cases containing polynomial shares and recovers the secret using mathematical interpolation techniques.

## Features

- **Lagrange Interpolation**: Primary method for secret reconstruction
- **Matrix Method**: Alternative implementation for verification using Gaussian elimination
- **Multi-base Support**: Handles shares encoded in different number bases (binary, octal, decimal, hexadecimal, etc.)
- **BigInteger Precision**: Uses Java's BigInteger for handling large numbers without precision loss
- **JSON Parsing**: Custom lightweight JSON parser for test case files
- **Error Detection**: Warns about non-exact divisions that might indicate corrupted shares

## Algorithm Details

### Mathematical Foundation

The scheme is based on polynomial interpolation over finite fields:
- A secret `S` is encoded as the constant term of a polynomial `f(x)` of degree `k-1`
- `n` shares are generated as points `(x, f(x))` on this polynomial
- Any `k` shares can reconstruct the secret using Lagrange interpolation

### Lagrange Interpolation Formula

For points `(x₀, y₀), (x₁, y₁), ..., (xₖ₋₁, yₖ₋₁)`, the secret is:

```
S = f(0) = Σᵢ yᵢ * Lᵢ(0)
```

Where `Lᵢ(0)` is the Lagrange basis polynomial:

```
Lᵢ(0) = Πⱼ≠ᵢ (-xⱼ) / (xᵢ - xⱼ)
```

## Project Structure

```
ShamirSecret.java
├── ShamirSecret (Main class)
│   ├── Point (Inner class for coordinate pairs)
│   ├── TestCase (Data structure for JSON parsing)
│   └── Root (Data structure for individual shares)
└── MatrixMethod (Alternative implementation)
```

## Input Format

The program expects JSON files with the following structure:

```json
{
    "keys": {
        "n": 4,
        "k": 3
    },
    "1": {
        "base": "10",
        "value": "4"
    },
    "2": {
        "base": "2",
        "value": "111"
    },
    "3": {
        "base": "10",
        "value": "12"
    }
}
```

Where:
- `n`: Total number of shares
- `k`: Minimum threshold of shares needed
- Each numbered key represents a share with its base and encoded value

## Usage

### Compilation

```bash
javac ShamirSecret.java
```

### Execution

```bash
java ShamirSecret
```

The program automatically processes `testcase1.json` and `testcase2.json` files in the current directory.

### Sample Output

```
Processing testcase1.json:
n = 4, k = 3
Polynomial degree m = 2
Root: x=1, y=4 (base 10) = 4 (decimal)
Root: x=2, y=111 (base 2) = 7 (decimal)
Root: x=3, y=12 (base 10) = 12 (decimal)

Using Lagrange Interpolation:
Point 1: (1, 4) contributes: 1
  Numerator: 6
  Denominator: 2
Point 2: (2, 7) contributes: -9
  Numerator: -3
  Denominator: 1
Point 3: (3, 12) contributes: 11
  Numerator: 2
  Denominator: 2

Secret for testcase 1: 3
```

## Key Components

### 1. Point Class
Represents coordinate pairs `(x, y)` for polynomial evaluation points.

### 2. JSON Parser
Custom lightweight parser that handles the specific JSON structure without external dependencies.

### 3. Base Conversion
Automatically converts share values from various number bases to decimal using `BigInteger(value, base)`.

### 4. Lagrange Interpolation
Core algorithm that reconstructs the polynomial's constant term (the secret) using the mathematical formula.

### 5. Matrix Method (Alternative)
Implements Gaussian elimination to solve the linear system formed by the Vandermonde matrix for verification purposes.

## Error Handling

- **File I/O Errors**: Gracefully handles missing or unreadable test files
- **JSON Parsing**: Robust parsing with error reporting for malformed JSON
- **Division Validation**: Checks for exact divisions to detect potentially corrupted shares
- **Numerical Precision**: Uses BigInteger to avoid overflow issues with large numbers

## Mathematical Verification

The implementation includes both Lagrange interpolation and matrix-based approaches:

1. **Lagrange Method**: Direct application of interpolation formula
2. **Matrix Method**: Solves the linear system `Ax = y` where `A` is the Vandermonde matrix

Both methods should yield identical results for valid input.

## Dependencies

- Java 8 or higher
- No external libraries required (uses only standard Java libraries)

## Security Considerations

This implementation is designed for educational and demonstration purposes. For production cryptographic applications, consider:

- Using finite field arithmetic instead of integer arithmetic
- Implementing proper error correction codes
- Adding cryptographic random number generation
- Validating share authenticity

## Contributing

Feel free to submit issues, fork the repository, and create pull requests for any improvements.

## License

This project is open source and available under the MIT License.

---

*This implementation demonstrates the mathematical elegance of Shamir's Secret Sharing while providing practical tools for secret reconstruction from polynomial shares.*