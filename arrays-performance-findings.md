# Arrays Performance Findings

Source reviewed: [Arrays.java](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java)

This note summarizes the main performance bottlenecks in `Arrays`, with emphasis on the paths most likely to matter for 128-bit and 256-bit workloads.

## Highest-impact findings

### 1. `mDivmod` performs two full division passes

`mDivmod` delegates to `mDivide` and then immediately to `mMod`, so a quotient-plus-remainder call runs division twice over the same operands. That is the clearest algorithmic waste in the file.

Relevant code:
- [mDivmod](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1100)
- [mDivide](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L956)
- [mModInPlace](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1110)

Impact:
- Doubles the cost of `divmod`
- Repeats the same normalization, copy, and scratch clearing work
- Likely the largest avoidable hotspot if division is used in modular arithmetic

### 2. `square()` allocates and copies more than necessary

`square(a, offset, length, overflowHolder)` first allocates `padded`, copies the input into it, and then calls `multiply(...)`, which allocates and copies again before performing the multiply. The overloads also allocate a throwaway `boolean[1]` for overflow reporting.

Relevant code:
- [square](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1261)
- [pow](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1288)

Impact:
- Extra allocation pressure on a path that is commonly nested inside `pow`
- Redundant memory traffic before the actual arithmetic starts

### 3. `mPow()` is memory-heavy and repeatedly resets scratch buffers

`mPow` does repeated `fill` and `arraycopy` operations inside the exponentiation loop. For each set bit in the exponent it copies buffers, calls multiplication, and often clears buffers again. For higher exponents, the cost is dominated by memory movement rather than arithmetic.

Relevant code:
- [mPow](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1307)

Impact:
- Many full-width buffer clears
- Multiple copies per loop iteration
- Most visible when exponentiation is chained or used in modular exponentiation

### 4. `mMultiply(int[], int[])` has extra overflow-scanning overhead

The array-by-array multiplication uses two `long[]` accumulators and also scans the unused tail of the multiplier to detect overflow. That adds extra `O(n^2)` work on top of the core multiply.

Relevant code:
- [mMultiply(int[], int[])](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L889)

Impact:
- Acceptable for tiny fixed widths, but still expensive for repeated 256-bit use
- Overflow detection is intertwined with the main multiply loop and adds branchy work

### 5. Division and modulo paths clear and copy entire scratch arrays

`mDivide` and `mModInPlace` both clear scratch buffers and copy active slices into them before delegating to `Division`. That is correct, but for fixed-width integers it becomes a visible cost because the data movement is close in size to the math itself.

Relevant code:
- [mDivide](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L956)
- [mModInPlace](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1110)
- [Division.div](juint/src/main/java/de/pribluda/ether/jfastuint/Division.java#L21)

Impact:
- Full-width `fill` and `arraycopy` calls on the hot path
- Division already has substantial arithmetic cost, so extra memory traffic is expensive

## Secondary findings

### 6. `mAddMod` and `mMulMod` normalize operands repeatedly

Both modular helpers reduce inputs modulo `mod` before doing the main operation, and `mMulMod` reduces again after multiplication. That is correct but costly when callers already keep values normalized.

Relevant code:
- [mAddMod](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1191)
- [mMulMod](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1224)

### 7. `compare(int[], BigInteger)` allocates a temporary array

`compare(int[], BigInteger)` converts `BigInteger` into an array before comparing. That makes it unsuitable for tight loops or repeated comparisons.

Relevant code:
- [compare(int[], BigInteger)](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L253)

### 8. Immutable wrappers allocate even for very small operations

The immutable variants are intentionally allocating, but some of them still allocate more than needed for trivial work, for example `new boolean[1]` in `square` and `pow`.

Relevant code:
- [square](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1267)
- [pow](juint/src/main/java/de/pribluda/ether/jfastuint/Arrays.java#L1288)

## Practical order of attack

1. Collapse `mDivmod` into a single quotient/remainder pass.
2. Remove the extra allocations from `square` and the `pow` fast paths.
3. Reduce scratch clearing and copying in `mPow`, `mDivide`, and `mModInPlace`.
4. Benchmark `mMultiply(int[], int[])` with overflow detection separated from the main product accumulation.
5. Only then look at smaller wins like `compare(BigInteger)` conversion costs.

## Bottom line

The arithmetic kernels are mostly reasonable for fixed 128-bit and 256-bit widths, but the file still pays a lot of overhead in buffer management, repeated normalization, and duplicate division work. The biggest performance gains are likely to come from reducing passes over the same arrays, not from changing the arithmetic formulas themselves.
