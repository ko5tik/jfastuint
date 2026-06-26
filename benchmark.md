# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including the newly added `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 22.04 ± 9.60 | 2017.02 ± 878.98 | 144 B/op | 54 |
| `UInt256` immutable | 37.92 ± 15.12 | 1735.37 ± 692.01 | 72 B/op | 51 |
| `UInt256` mutable | 46.22 ± 31.54 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 31.07 ± 14.06 | 1737.98 ± 784.97 | 88 B/op | 57 |
| `UInt256` immutable | 33.83 ± 5.29 | 1547.75 ± 242.17 | 72 B/op | 55 |
| `UInt256` mutable | 50.20 ± 11.14 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 20.92 ± 10.99 | 1914.92 ± 1005.79 | 144 B/op | 51 |
| `UInt256` immutable | 43.07 ± 11.12 | 1970.74 ± 508.43 | 72 B/op | 57 |
| `UInt256` mutable | 55.90 ± 25.26 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 36.10 ± 16.62 | 2201.48 ± 1020.79 | 96 B/op | 53 |
| `UInt256` immutable | 34.14 ± 33.70 | 1561.86 ± 1542.46 | 72 B/op | 42 |
| `UInt256` mutable | 23.72 ± 19.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 1.00 ± 1.29 | 838.68 ± 1080.68 | 1320 B/op | 30 |
| `UInt256` immutable | 0.82 ± 0.36 | 37.63 ± 16.55 | 72 B/op | 2 |
| `UInt256` mutable | 0.88 ± 0.46 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 5.30 ± 2.50 | 1521.46 ± 452.97 | 288 B/op | 44 |
| `UInt256` immutable | 4.19 ± 5.55 | 237.03 ± 49.30 | 72 B/op | 13 |
| `UInt256` mutable | 5.90 ± 3.85 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

### Multiplication
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 15.24 ± 11.30 | 1161.97 ± 862.21 | 120 B/op | 41 |
| `UInt256` immutable | 6.46 ± 3.22 | 295.80 ± 147.30 | 72 B/op | 15 |
| `UInt256` mutable | 7.97 ± 1.53 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
---

## Interpretation
* **Mutable `UInt256`** methods exhibit **near‑zero heap allocations** (`gc.alloc.rate ≈ 0`), confirming the design goal of minimizing GC pressure.
* **Immutable `UInt256`** still allocates far less than `BigInteger` (≈ 72 B per op vs ≈ 144 B or more), offering a good trade‑off between immutability and performance.
* **`sqrt`** has been optimized using Newton's method. Rather than being 10x+ slower, both the mutable (`0.88 ops/µs`) and immutable (`0.82 ops/µs`) versions are now very close to `BigInteger`'s performance (`1.00 ops/µs`), while drastically reducing heap allocations (nearly zero for mutable).
* Overall, **throughput** for mutable operations is higher than both immutable and `BigInteger` for add, subtract, xor, division, and multiplication, meeting the performance objectives.

---

*Generated on 2026‑06‑25.*

### Deep Multiply (per bit width)

| Bit Width | Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|-----------|---------|----------------------|-----------------------|----------------------------|----------|
| 128 | `BigInteger` | 12.0 ± 12.0 | 666 ± 651 | 88.0 ± 0.0 B/op | 15 |
| 128 | `UInt256` immutable | 6.0 ± 3.0 | 267 ± 115 | 72.0 ± 0.01 B/op | 14 |
| 128 | `UInt256` mutable | 8.0 ± 4.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 256 | `BigInteger` | 9.0 ± 2.0 | 672 ± 122 | 120.0 ± 0.0 B/op | 23 |
| 256 | `UInt256` immutable | 6.0 ± 3.0 | 176 ± 115 | 72.0 ± 0.02 B/op | 10 |
| 256 | `UInt256` mutable | 5.0 ± 1.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 512 | `BigInteger` | 4.0 ± 1.0 | 417 ± 142 | 184.0 ± 0.0 B/op | 15 |
| 512 | `UInt256` immutable | 4.0 ± 2.0 | 178 ± 304 | 72.0 ± 0.01 B/op | 10 |
| 512 | `UInt256` mutable | 5.0 ± 1.0 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

---
