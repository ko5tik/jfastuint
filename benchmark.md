# Benchmark Results

The following JMH benchmarks compare **mutable**, **immutable** `UInt256` implementations against Java `BigInteger` for a set of arithmetic operations, including `sqrt`.

---

## Summary Tables

### Add
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 29.57 ± 3.87 | 2705.78 ± 353.97 | 144 B/op | 144 |
| `UInt256` immutable | 43.58 ± 6.82 | 2215.87 ± 345.67 | 80 B/op | 120 |
| `UInt256` mutable | 57.91 ± 7.79 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Subtract
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 34.87 ± 4.38 | 1950.36 ± 245.00 | 88 B/op | 107 |
| `UInt256` immutable | 34.47 ± 3.57 | 1752.62 ± 181.38 | 80 B/op | 102 |
| `UInt256` mutable | 48.24 ± 4.28 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### XOR
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 22.99 ± 2.14 | 2103.59 ± 195.41 | 144 B/op | 115 |
| `UInt256` immutable | 59.38 ± 11.61 | 3019.56 ± 590.30 | 80 B/op | 151 |
| `UInt256` mutable | 65.22 ± 8.79 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Shift Left
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 45.10 ± 10.17 | 2751.89 ± 620.20 | 96 B/op | 143 |
| `UInt256` immutable | 59.38 ± 12.04 | 3019.35 ± 612.23 | 80 B/op | 142 |
| `UInt256` mutable | 54.93 ± 5.42 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Square Root
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 1.38 ± 0.16 | 1264.99 ± 147.84 | 1440 B/op | 90 |
| `UInt256` immutable | 1.25 ± 0.25 | 63.64 ± 12.71 | 80 B/op | 7 |
| `UInt256` mutable | 1.32 ± 0.18 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Division
| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` | 8.08 ± 2.01 | 1479.13 ± 368.13 | 288 B/op | 87 |
| `UInt256` immutable | 6.33 ± 2.11 | 321.77 ± 107.32 | 80 B/op | 33 |
| `UInt256` mutable | 7.14 ± 1.01 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

### Multiplication
This run uses large operands in the `10^15` to `10^25` range, plus 32-bit scalar multipliers, to better match the library's typical workload.

| Variant | Throughput (ops / µs) | GC Alloc Rate (MB / s) | GC Alloc Rate Norm (B / op) | GC Count |
|--------|----------------------|-----------------------|----------------------------|----------|
| `BigInteger` large x large | 5.92 ± 0.93 | 1325.07 ± 208.33 | 352 B/op | 64 |
| `UInt256` large x large immutable | 5.65 ± 0.81 | 1149.22 ± 165.47 | 320 B/op | 81 |
| `UInt256` large x large mutable | 3.62 ± 0.56 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| `BigInteger` large x int | 6.10 ± 0.94 | 1427.02 ± 220.24 | 368 B/op | 90 |
| `UInt256` large x int immutable | 8.93 ± 2.15 | 1815.37 ± 437.28 | 320 B/op | 82 |
| `UInt256` large x int mutable | 8.99 ± 1.34 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| `BigInteger` large x long | 7.23 ± 2.33 | 1691.62 ± 545.85 | 368 B/op | 85 |
| `UInt256` large x long immutable | 7.89 ± 2.22 | 1603.93 ± 451.66 | 320 B/op | 114 |
| `UInt256` large x long mutable | 9.44 ± 0.54 | ≈ 0 | ≈ 0 B/op | ≈ 0 |

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
| 128 | `BigInteger` | 32.00 ± 3.00 | 1839.53 ± 185.34 | 88 B/op | 100 |
| 128 | `UInt256` immutable | 15.00 ± 1.00 | 584.32 ± 220.27 | 104 B/op | 49 |
| 128 | `UInt256` mutable | 12.00 ± 1.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 256 | `BigInteger` | 19.00 ± 3.00 | 1357.05 ± 373.48 | 120 B/op | 75 |
| 256 | `UInt256` immutable | 9.00 ± 1.00 | 656.49 ± 57.36 | 104 B/op | 56 |
| 256 | `UInt256` mutable | 11.00 ± 1.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
| 512 | `BigInteger` | 9.00 ± 1.00 | 521.15 ± 148.10 | 184 B/op | 41 |
| 512 | `UInt256` immutable | 9.00 ± 1.00 | 479.26 ± 121.00 | 104 B/op | 41 |
| 512 | `UInt256` mutable | 10.00 ± 1.00 | ≈ 0 | ≈ 0 B/op | ≈ 0 |
