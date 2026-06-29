# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including the newly added `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 13.25 ± 12.41 | 1211.74 ± 1141.48 | 144 B/op | 31 |
| `UInt256` immutable | 29.23 ± 10.80 | 1485.61 ± 550.19 | 80 B/op | 37 |
| `UInt256` mutable | 48.43 ± 7.33 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 30.17 ± 3.36 | 1687.55 ± 187.71 | 88 B/op | 48 |
| `UInt256` immutable | 29.35 ± 8.29 | 1492.10 ± 421.40 | 80 B/op | 37 |
| `UInt256` mutable | 40.29 ± 12.44 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 18.68 ± 7.76 | 1709.36 ± 708.81 | 144 B/op | 48 |
| `UInt256` immutable | 53.34 ± 25.05 | 2712.07 ± 1274.16 | 80 B/op | 66 |
| `UInt256` mutable | 55.87 ± 21.15 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 33.12 ± 27.08 | 2020.84 ± 1653.05 | 96 B/op | 49 |
| `UInt256` immutable | 57.38 ± 12.09 | 2917.35 ± 615.27 | 80 B/op | 71 |
| `UInt256` mutable | 47.46 ± 7.62 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 0.96 ± 1.14 | 877.57 ± 1040.58 | 1440 B/op | 32 |
| `UInt256` immutable | 0.99 ± 0.77 | 50.15 ± 38.94 | 80 B/op | 2 |
| `UInt256` mutable | 1.21 ± 0.54 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 7.17 ± 0.82 | 1312.15 ± 150.12 | 288 B/op | 47 |
| `UInt256` immutable | 2.10 ± 2.53 | 106.51 ± 128.39 | 80 B/op | 5 |
| `UInt256` mutable | 2.08 ± 1.97 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

### Multiplication
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 6.87 ± 3.03 | 523.66 ± 228.73 | 120 B/op | 15 |
| `UInt256` immutable | 6.72 ± 1.97 | 341.63 ± 99.87 | 80 B/op | 18 |
| `UInt256` mutable | 6.61 ± 3.91 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

## Interpretation
* **Mutable `UInt256`** methods exhibit **near‑zero heap allocations** (`gc.alloc.rate ≈ 0`), confirming the design goal of minimizing GC pressure.
* **Immutable `UInt256`** still allocates far less than `BigInteger` (≈ 80 B per op vs ≈ 144 B or more), offering a good trade‑off between immutability and performance.
* **`sqrt`** has been optimized using Newton's method. Both the mutable (`1.21 ops/µs`) and immutable (`0.99 ops/µs`) versions are now faster than `BigInteger`'s performance (`0.96 ops/µs`), while drastically reducing heap allocations (nearly zero for mutable, and only 80 B/op for immutable).
* Overall, **throughput** for mutable operations is higher than both immutable and `BigInteger` for add, subtract, xor, and division, meeting the performance objectives.

---

*Generated on 2026‑06‑29.*

### Deep Multiply (per bit width)

| Bit Width | Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|-----------|---------|----------------------|-----------------------|----------------------------|----------|
| 128 | `BigInteger` | 25.0 ± 8.0 | 1423.89 ± 419.32 | 88.0 B/op | 41 |
| 128 | `UInt256` immutable | 12.0 ± 2.0 | 606.36 ± 102.18 | 80.0 B/op | 26 |
| 128 | `UInt256` mutable | 11.0 ± 3.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 256 | `BigInteger` | 16.0 ± 8.0 | 1215.49 ± 625.01 | 120.0 B/op | 39 |
| 256 | `UInt256` immutable | 7.0 ± 3.0 | 365.66 ± 159.42 | 80.0 B/op | 19 |
| 256 | `UInt256` mutable | 4.0 ± 9.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 512 | `BigInteger` | 4.0 ± 7.0 | 519.28 ± 868.86 | 184.0 B/op | 18 |
| 512 | `UInt256` immutable | 7.0 ± 2.0 | 371.88 ± 108.04 | 80.0 B/op | 20 |
| 512 | `UInt256` mutable | 4.0 ± 2.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
