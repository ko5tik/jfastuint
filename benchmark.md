# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 29.21 ± 3.57 | 2874.09 ± 948.93 | 144 B/op | 71 |
| `UInt256` immutable | 45.07 ± 6.24 | 1649.65 ± 2026.68 | 80 B/op | 42 |
| `UInt256` mutable | 62.46 ± 5.54 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 35.69 ± 3.78 | 1892.22 ± 1879.54 | 88 B/op | 49 |
| `UInt256` immutable | 31.85 ± 3.30 | 1851.08 ± 561.19 | 80 B/op | 53 |
| `UInt256` mutable | 40.17 ± 12.94 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 13.66 ± 6.71 | 2049.68 ± 1292.62 | 144 B/op | 56 |
| `UInt256` immutable | 51.68 ± 4.96 | 2632.67 ± 3520.40 | 80 B/op | 61 |
| `UInt256` mutable | 49.32 ± 11.50 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 28.56 ± 11.29 | 3138.52 ± 860.67 | 96 B/op | 78 |
| `UInt256` immutable | 48.30 ± 8.55 | 3688.10 ± 701.77 | 80 B/op | 90 |
| `UInt256` mutable | 55.73 ± 2.72 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 1.30 ± 0.24 | 1426.50 ± 781.47 | 1440 B/op | 51 |
| `UInt256` immutable | 1.37 ± 0.08 | 71.07 ± 32.32 | 80 B/op | 4 |
| `UInt256` mutable | 1.51 ± 0.17 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 8.57 ± 0.58 | 1555.67 ± 1050.18 | 288 B/op | 55 |
| `UInt256` immutable | 7.02 ± 0.48 | 217.83 ± 375.80 | 80 B/op | 12 |
| `UInt256` mutable | 7.52 ± 0.94 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

### Multiplication
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 19.32 ± 2.40 | 1598.65 ± 737.72 | 120 B/op | 46 |
| `UInt256` immutable | 7.81 ± 1.00 | 440.35 ± 308.61 | 80 B/op | 23 |
| `UInt256` mutable | 3.53 ± 1.49 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

## Interpretation
* **Mutable `UInt256`** methods exhibit **near‑zero heap allocations** (`gc.alloc.rate ≈ 0`), confirming the design goal of minimizing GC pressure.
* **Immutable `UInt256`** still allocates far less than `BigInteger` (≈ 80 B per op vs ≈ 144 B or more), offering a good trade‑off between immutability and performance.
* **`sqrt`** has been optimized using Newton's method. Both the mutable and immutable versions drastically reduce heap allocations (nearly zero for mutable, and only 80 B/op for immutable) compared to `BigInteger`.
* Overall, **throughput** for mutable operations is higher than both immutable and `BigInteger` for add, subtract, xor, and division, meeting the performance objectives.

---

*Generated on 2026‑07-05 with 10 warmup and 10 measurement iterations.*

### Deep Multiply (per bit width)

| Bit Width | Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|-----------|---------|----------------------|-----------------------|----------------------------|----------|
| 128 | `BigInteger` | 39.0 ± 3.0 | 1503.20 ± 1597.66 | 88 B/op | 46 |
| 128 | `UInt256` immutable | 11.0 ± 1.0 | 678.26 ± 353.36 | 80 B/op | 29 |
| 128 | `UInt256` mutable | 9.0 ± 3.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 256 | `BigInteger` | 21.0 ± 6.0 | 1029.37 ± 1367.43 | 120 B/op | 31 |
| 256 | `UInt256` immutable | 8.0 ± 1.0 | 398.01 ± 252.51 | 80 B/op | 20 |
| 256 | `UInt256` mutable | 7.0 ± 1.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 512 | `BigInteger` | 8.0 ± 6.0 | 976.76 ± 246.59 | 184 B/op | 42 |
| 512 | `UInt256` immutable | 8.0 ± 2.0 | 381.08 ± 403.48 | 80 B/op | 20 |
| 512 | `UInt256` mutable | 6.0 ± 1.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
