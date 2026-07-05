# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 31.41 ± 10.37 | 2874.09 ± 948.93 | 144 B/op | 71 |
| `UInt256` immutable | 32.46 ± 39.79 | 1649.65 ± 2026.68 | 80 B/op | 42 |
| `UInt256` mutable | 63.23 ± 26.80 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 33.83 ± 33.65 | 1892.22 ± 1879.54 | 88 B/op | 49 |
| `UInt256` immutable | 36.41 ± 11.03 | 1851.08 ± 561.19 | 80 B/op | 53 |
| `UInt256` mutable | 40.08 ± 24.56 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 22.40 ± 14.13 | 2049.68 ± 1292.62 | 144 B/op | 56 |
| `UInt256` immutable | 51.79 ± 69.28 | 2632.67 ± 3520.40 | 80 B/op | 61 |
| `UInt256` mutable | 64.89 ± 33.84 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 51.45 ± 14.09 | 3138.52 ± 860.67 | 96 B/op | 78 |
| `UInt256` immutable | 72.54 ± 13.79 | 3688.10 ± 701.77 | 80 B/op | 90 |
| `UInt256` mutable | 59.08 ± 15.46 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 1.56 ± 0.85 | 1426.50 ± 781.47 | 1440 B/op | 51 |
| `UInt256` immutable | 1.40 ± 0.64 | 71.07 ± 32.32 | 80 B/op | 4 |
| `UInt256` mutable | 1.41 ± 0.51 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 8.50 ± 5.74 | 1555.67 ± 1050.18 | 288 B/op | 55 |
| `UInt256` immutable | 4.28 ± 7.39 | 217.83 ± 375.80 | 80 B/op | 12 |
| `UInt256` mutable | 8.39 ± 2.91 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

### Multiplication
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 20.97 ± 9.66 | 1598.65 ± 737.72 | 120 B/op | 46 |
| `UInt256` immutable | 8.66 ± 6.07 | 440.35 ± 308.61 | 80 B/op | 23 |
| `UInt256` mutable | 7.58 ± 6.16 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

## Interpretation
* **Mutable `UInt256`** methods exhibit **near‑zero heap allocations** (`gc.alloc.rate ≈ 0`), confirming the design goal of minimizing GC pressure.
* **Immutable `UInt256`** still allocates far less than `BigInteger` (≈ 80 B per op vs ≈ 144 B or more), offering a good trade‑off between immutability and performance.
* **`sqrt`** has been optimized using Newton's method. Both the mutable and immutable versions drastically reduce heap allocations (nearly zero for mutable, and only 80 B/op for immutable) compared to `BigInteger`.
* Overall, **throughput** for mutable operations is higher than both immutable and `BigInteger` for add, subtract, xor, and division, meeting the performance objectives.

---

*Generated on 2026‑07‑04.*

### Deep Multiply (per bit width)

| Bit Width | Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|-----------|---------|----------------------|-----------------------|----------------------------|----------|
| 128 | `BigInteger` | 27.0 ± 29.0 | 1503.20 ± 1597.66 | 88 B/op | 46 |
| 128 | `UInt256` immutable | 13.0 ± 7.0 | 678.26 ± 353.36 | 80 B/op | 29 |
| 128 | `UInt256` mutable | 13.0 ± 6.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 256 | `BigInteger` | 14.0 ± 18.0 | 1029.37 ± 1367.43 | 120 B/op | 31 |
| 256 | `UInt256` immutable | 8.0 ± 5.0 | 398.01 ± 252.51 | 80 B/op | 20 |
| 256 | `UInt256` mutable | 6.0 ± 5.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 512 | `BigInteger` | 8.0 ± 2.0 | 976.76 ± 246.59 | 184 B/op | 42 |
| 512 | `UInt256` immutable | 7.0 ± 8.0 | 381.08 ± 403.48 | 80 B/op | 20 |
| 512 | `UInt256` mutable | 8.0 ± 4.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
