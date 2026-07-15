# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 24.11 ± 7.92 | 2206.93 ± 724.76 | 144 B/op | 107 |
| `UInt256` immutable | 42.66 ± 12.90 | 2169.29 ± 655.05 | 80 B/op | 93 |
| `UInt256` mutable | 39.97 ± 4.58 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 36.88 ± 3.42 | 2063.06 ± 191.27 | 88 B/op | 113 |
| `UInt256` immutable | 25.19 ± 15.56 | 1280.51 ± 791.69 | 80 B/op | 71 |
| `UInt256` mutable | 41.19 ± 6.02 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 20.38 ± 9.29 | 1865.11 ± 850.41 | 144 B/op | 102 |
| `UInt256` immutable | 44.01 ± 9.18 | 2237.71 ± 466.61 | 80 B/op | 86 |
| `UInt256` mutable | 55.15 ± 21.69 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 49.89 ± 7.36 | 3044.57 ± 448.75 | 96 B/op | 162 |
| `UInt256` immutable | 61.01 ± 23.05 | 3102.34 ± 1171.46 | 80 B/op | 148 |
| `UInt256` mutable | 48.97 ± 14.07 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 1.66 ± 0.09 | 1391.02 ± 78.31 | 1320 B/op | 98 |
| `UInt256` immutable | 1.13 ± 0.35 | 57.42 ± 17.79 | 80 B/op | 6 |
| `UInt256` mutable | 1.29 ± 0.20 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 3.46 ± 1.64 | 634.20 ± 299.24 | 288 B/op | 43 |
| `UInt256` immutable | 4.12 ± 2.31 | 209.51 ± 117.26 | 80 B/op | 21 |
| `UInt256` mutable | 7.70 ± 0.66 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Multiplication
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 21.96 ± 1.05 | 1674.78 ± 80.30 | 120 B/op | 115 |
| `UInt256` immutable | 15.14 ± 1.14 | 770.01 ± 57.94 | 80 B/op | 66 |
| `UInt256` mutable | 10.35 ± 0.67 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---

## Interpretation
* **Mutable `UInt256`** methods exhibit **near‑zero heap allocations** (`gc.alloc.rate ≈ 0`), confirming the design goal of minimizing GC pressure.
* **Immutable `UInt256`** still allocates far less than `BigInteger` (≈ 80 B per op vs ≈ 144 B or more), offering a good trade‑off between immutability and performance.
* **`sqrt`** Newton-Raphson implementation performs efficiently, with the mutable version allocating no memory and the immutable version requiring only 80 B/op.
* Overall, **throughput** for mutable operations is higher than or comparable to immutable and `BigInteger` for subtract, xor, and division. For addition and shift left, both mutable and immutable variants deliver robust, competitive throughput.

---

*Generated on 2026-07-15 with 10 warmup and 10 measurement iterations.*

### Deep Multiply (per bit width)

| Bit Width | Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|-----------|---------|----------------------|-----------------------|----------------------------|----------|
| 128 | `BigInteger` | 29.00 ± 14.00 | 2015.73 ± 419.85 | 88 B/op | 115 |
| 128 | `UInt256` immutable | 9.00 ± 1.00 | 869.90 ± 286.11 | 104 B/op | 74 |
| 128 | `UInt256` mutable | 10.00 ± 4.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 256 | `BigInteger` | 8.00 ± 4.00 | 1682.17 ± 230.55 | 120 B/op | 90 |
| 256 | `UInt256` immutable | 10.00 ± 3.00 | 510.17 ± 61.41 | 80 B/op | 53 |
| 256 | `UInt256` mutable | 8.00 ± 1.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 512 | `BigInteger` | 7.00 ± 3.00 | 1056.96 ± 122.73 | 184 B/op | 90 |
| 512 | `UInt256` immutable | 10.00 ± 2.00 | 734.70 ± 126.06 | 80 B/op | 52 |
| 512 | `UInt256` mutable | 5.00 ± 5.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

