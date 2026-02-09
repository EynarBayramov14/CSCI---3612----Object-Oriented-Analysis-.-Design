# Random Number Generators & Descriptive Statistics (Java)

## Project Summary
This project implements a Java program that generates random double values in the range **[0, 1)** using three built-in Java random number generators and analyzes the generated data using descriptive statistics.

The program is implemented as a **single Java class named `Generator`** and follows the required method structure:
- `populate(int n, int randNumGen)`
- `statistics(ArrayList<Double> randomValues)`
- `display(ArrayList<Double> results, boolean headerOn)`
- `execute()`

The `main` method is minimal and only instantiates `Generator` and calls `execute()`.

---

## Random Number Generators Used
The program generates random values using each of the following approaches:
1. **`java.util.Random`**
2. **`Math.random()`**
3. **`java.util.concurrent.ThreadLocalRandom`**

All generated values are doubles in the interval **[0, 1)**.

---

## Statistics Computed
For each dataset, the program calculates the following descriptive statistics and prints them in tabular form:
- **n**: number of generated values  
- **mean**: average of the values  
- **sample standard deviation**: computed using denominator **(n − 1)**  
- **min**: smallest generated value  
- **max**: largest generated value  

The `statistics()` method returns results in this exact order:
`[n, mean, stddev, min, max]`

---

## Program Execution (9 Total Results)
The program evaluates:
- **3 sample sizes** (n values)
- **3 random number generators**

This produces **3 × 3 = 9** output rows of statistics.

---

## Expected Trends (Theory Check)
Since the values are uniformly distributed in **[0, 1)**, as **n increases**:
- **min** should approach **0**
- **max** should approach **1**
- **mean** should approach **0.5**
- **sample standard deviation** should approach approximately 0.288675 (≈ 0.29)


Small deviations are expected for small sample sizes.

---

## Files Included
- `Generator.java` — complete solution (single class)

---

## How to Compile and Run

### Compile
javac Generator.java

### Run
java Generator


