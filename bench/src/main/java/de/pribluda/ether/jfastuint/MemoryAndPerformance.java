package de.pribluda.ether.jfastuint;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class MemoryAndPerformance {

  @State(Scope.Thread)
  public static class BenchmarkState {
    // 256-bit operands
    public UInt256 val1 = UInt256.mutable(new int[]{-1, Short.MAX_VALUE, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 77, 3, -1});
    public UInt256 val2 = UInt256.mutable(new int[]{Short.MAX_VALUE, -1, -1, Integer.MAX_VALUE, Short.MIN_VALUE, 13, 2, 0});
    public UInt256 temp = UInt256.mutable(new int[8]);

    public BigInteger big1 = val1.toBigInteger();
    public BigInteger big2 = val2.toBigInteger();
  }

  // Helper method for BigInteger square root (for Java 8 compatibility)
  public static BigInteger sqrtBigInteger(BigInteger val) {
    if (val.signum() < 0) {
      throw new ArithmeticException("Negative BigInteger");
    }
    if (val.equals(BigInteger.ZERO) || val.equals(BigInteger.ONE)) {
      return val;
    }
    BigInteger y = val.shiftRight(val.bitLength() / 2);
    while (true) {
      BigInteger nextY = y.add(val.divide(y)).shiftRight(1);
      if (nextY.equals(y) || nextY.equals(y.subtract(BigInteger.ONE))) {
        if (nextY.multiply(nextY).compareTo(val) > 0) {
          return nextY.subtract(BigInteger.ONE);
        }
        return nextY;
      }
      y = nextY;
    }
  }

  // --- Addition ---

  @Benchmark
  public BigInteger add_biginteger(BenchmarkState s) {
    return s.big1.add(s.big2);
  }

  @Benchmark
  public UInt256 add_uint256_immutable(BenchmarkState s) {
    return s.val1.add(s.val2);
  }

  @Benchmark
  public UInt256 add_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mAdd(s.val2);
  }

  // --- Subtraction ---

  @Benchmark
  public BigInteger subtract_biginteger(BenchmarkState s) {
    return s.big1.subtract(s.big2);
  }

  @Benchmark
  public UInt256 subtract_uint256_immutable(BenchmarkState s) {
    return s.val1.subtract(s.val2);
  }

  @Benchmark
  public UInt256 subtract_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mSubtract(s.val2);
  }

  // --- Multiplication ---

  @Benchmark
  public BigInteger multiply_biginteger(BenchmarkState s) {
    return s.big1.multiply(s.big2);
  }

  @Benchmark
  public UInt256 multiply_uint256_immutable(BenchmarkState s) {
    return s.val1.multiply(s.val2);
  }

  @Benchmark
  public UInt256 multiply_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mMultiply(s.val2);
  }

  // --- Division ---

  @Benchmark
  public BigInteger divide_biginteger(BenchmarkState s) {
    return s.big1.divide(s.big2);
  }

  @Benchmark
  public UInt256 divide_uint256_immutable(BenchmarkState s) {
    return s.val1.divide(s.val2);
  }

  @Benchmark
  public UInt256 divide_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mDivide(s.val2);
  }

  // --- XOR ---

  @Benchmark
  public BigInteger xor_biginteger(BenchmarkState s) {
    return s.big1.xor(s.big2);
  }

  @Benchmark
  public UInt256 xor_uint256_immutable(BenchmarkState s) {
    return s.val1.xor(s.val2);
  }

  @Benchmark
  public UInt256 xor_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mXor(s.val2);
  }

  // --- Shift Left ---

  @Benchmark
  public BigInteger shiftLeft_biginteger(BenchmarkState s) {
    return s.big1.shiftLeft(10);
  }

  @Benchmark
  public UInt256 shiftLeft_uint256_immutable(BenchmarkState s) {
    return s.val1.shiftLeft(10);
  }

  @Benchmark
  public UInt256 shiftLeft_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mShiftLeft(10);
  }

  // --- Square Root ---

  @Benchmark
  public BigInteger sqrt_biginteger(BenchmarkState s) {
    return sqrtBigInteger(s.big1);
  }

  @Benchmark
  public UInt256 sqrt_uint256_immutable(BenchmarkState s) {
    return s.val1.sqrt();
  }

  @Benchmark
  public UInt256 sqrt_uint256_mutable(BenchmarkState s) {
    return s.temp.set(s.val1).mSqrt();
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
            .include(MemoryAndPerformance.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opt).run();
  }
}
