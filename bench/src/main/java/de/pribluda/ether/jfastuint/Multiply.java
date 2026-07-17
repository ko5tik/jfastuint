package de.pribluda.ether.jfastuint;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Multiply {

  @State(Scope.Thread)
  public static class BenchmarkState {
    private static final String[] LARGE_DECIMALS = {
      "1000000000000123",
      "1000000000000000456",
      "1000000000000000000789",
      "10000000000000000000000321"
    };

    private static final int[] SMALL_INT_FACTORS = {
      1,
      3,
      17,
      255,
      65535,
      1_000_000_000,
      Integer.MAX_VALUE
    };

    private static final long[] SMALL_LONG_FACTORS = {
      1L,
      3L,
      17L,
      255L,
      65535L,
      1_000_000_000L,
      4_000_000_000L
    };

    UInt256[] large = new UInt256[LARGE_DECIMALS.length];
    BigInteger[] bigLarge = new BigInteger[LARGE_DECIMALS.length];
    int[] smallInts = SMALL_INT_FACTORS.clone();
    long[] smallLongs = SMALL_LONG_FACTORS.clone();
    BigInteger[] bigSmallInts = new BigInteger[SMALL_INT_FACTORS.length];
    BigInteger[] bigSmallLongs = new BigInteger[SMALL_LONG_FACTORS.length];
    UInt256 temp = UInt256.mutable(new int[8]);

    @Setup(Level.Trial)
    public void setUp() {
      for (int i = 0; i < LARGE_DECIMALS.length; i++) {
        large[i] = new UInt256(LARGE_DECIMALS[i]);
        bigLarge[i] = new BigInteger(LARGE_DECIMALS[i]);
      }
      for (int i = 0; i < SMALL_INT_FACTORS.length; i++) {
        bigSmallInts[i] = BigInteger.valueOf(SMALL_INT_FACTORS[i] & 0xffffffffL);
      }
      for (int i = 0; i < SMALL_LONG_FACTORS.length; i++) {
        bigSmallLongs[i] = BigInteger.valueOf(SMALL_LONG_FACTORS[i]);
      }
    }
  }

  @Benchmark
  public void multiply_largeLarge_immutable(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.large.length; i++) {
      int j = (i + 1) % s.large.length;
      b.consume(s.large[i].multiply(s.large[j]));
    }
  }

  @Benchmark
  public void multiply_largeLarge_mutable(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.large.length; i++) {
      int j = (i + 1) % s.large.length;
      b.consume(s.temp.set(s.large[i]).mMultiply(s.large[j]));
    }
  }

  @Benchmark
  public void multiply_largeLarge_bigInteger(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.bigLarge.length; i++) {
      int j = (i + 1) % s.bigLarge.length;
      b.consume(s.bigLarge[i].multiply(s.bigLarge[j]));
    }
  }

  @Benchmark
  public void multiply_largeInt_immutable(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.large.length; i++) {
      b.consume(s.large[i].multiply(s.smallInts[i % s.smallInts.length]));
    }
  }

  @Benchmark
  public void multiply_largeInt_mutable(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.large.length; i++) {
      b.consume(s.temp.set(s.large[i]).mMultiply(s.smallInts[i % s.smallInts.length]));
    }
  }

  @Benchmark
  public void multiply_largeInt_bigInteger(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.bigLarge.length; i++) {
      b.consume(s.bigLarge[i].multiply(s.bigSmallInts[i % s.bigSmallInts.length]));
    }
  }

  @Benchmark
  public void multiply_largeLong_immutable(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.large.length; i++) {
      b.consume(s.large[i].multiply(s.smallLongs[i % s.smallLongs.length]));
    }
  }

  @Benchmark
  public void multiply_largeLong_mutable(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.large.length; i++) {
      b.consume(s.temp.set(s.large[i]).mMultiply(s.smallLongs[i % s.smallLongs.length]));
    }
  }

  @Benchmark
  public void multiply_largeLong_bigInteger(BenchmarkState s, Blackhole b) {
    for (int i = 0; i < s.bigLarge.length; i++) {
      b.consume(s.bigLarge[i].multiply(s.bigSmallLongs[i % s.bigSmallLongs.length]));
    }
  }
}
