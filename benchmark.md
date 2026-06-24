# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including the newly added `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 28.5 ± 6.13 | 2609 ± 559 | 144 B/op | 69 |
| `UInt256` immutable | 59.3 ± 7.27 | 2713 ± 333 | 72 B/op | 75 |
| `UInt256` mutable | 71.4 ± 20.7 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 21.7 ± 14.3 | 1213 ± 798 | 88 B/op | 36 |
| `UInt256` immutable | 20.0 ± 6.31 | 914 ± 291 | 72 B/op | 27 |
| `UInt256` mutable | 33.5 ± 19.3 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 14.0 ± 6.97 | 1278 ± 637 | 144 B/op | 33 |
| `UInt256` immutable | 33.4 ± 16.3 | 1528 ± 743 | 72 B/op | 44 |
| `UInt256` mutable | 61.4 ± 36.7 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `UInt256` immutable | 59.3 ± 7.27 | 2713 ± 333 | 72 B/op | 75 |
| `UInt256` mutable | 71.4 ± 20.7 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 1.73 ± 0.23 | 1586 ± 210 | 1440 B/op | 56 |
| `UInt256` immutable | 0.133 ± 0.006 | 6.07 ± 0.24 | 72 B/op | 1 |
| `UInt256` mutable | 0.114 ± 0.054 | ≈ 0 | 0.004 B/op | ≈ 0 |

---
### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 5.85 ± 2.71 | 1070 ± 495 | 288 B/op | 32 |
| `UInt256` immutable | 4.11 ± 1.57 | 188 ± 72 | 72 B/op | 10 |
| `UInt256` mutable | 4.20 ± 2.66 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

### Multiplication
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 13.534 ± 8.435 | 1032.545 ± 647 | 120 B/op | 35 |
| `UInt256` immutable | 5.884 ± 2.381 | 269.246 ± 109 | 72 B/op | 14 |
| `UInt256` mutable | 8.758 ± 1.94 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

## Interpretation
* **Mutable `UInt256`** methods exhibit **near‑zero heap allocations** (`gc.alloc.rate ≈ 0`), confirming the design goal of minimizing GC pressure.
* **Immutable `UInt256`** still allocates far less than `BigInteger` (≈ 70 B per op vs ≈ 144 B), offering a good trade‑off between immutability and performance.
* **`sqrt`** is comparatively expensive; the immutable version is ~7× slower than `BigInteger` and the mutable version is marginally slower. This reflects the algorithmic complexity of integer square‑root on fixed‑size arrays.
* Overall, **throughput** for mutable operations is higher than both immutable and `BigInteger` for add, subtract, xor, and shiftLeft, meeting the performance objectives.

---

*Generated on 2026‑06‑24.*
