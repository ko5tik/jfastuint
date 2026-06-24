package de.pribluda.ether.jfastuint;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class DeepMultiplyBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param({"128", "256", "512"})
        public int bitWidth;

        public UInt256 a;
        public UInt256 b;
        public UInt256 temp;
        public BigInteger bigA;
        public BigInteger bigB;

        private final SecureRandom rnd = new SecureRandom();

        @Setup(Level.Trial)
        public void setUp() {
            int words = bitWidth / 32; // each int holds 32 bits, little‑endian array
            int[] arrA = new int[words];
            int[] arrB = new int[words];
            for (int i = 0; i < words; i++) {
                arrA[i] = rnd.nextInt();
                arrB[i] = rnd.nextInt();
            }
            a = UInt256.mutable(arrA);
            b = UInt256.mutable(arrB);
            temp = UInt256.mutable(new int[words]);
            bigA = new BigInteger(1, toBytes(arrA));
            bigB = new BigInteger(1, toBytes(arrB));
        }

        /** Convert little‑endian int array to a big‑endian byte array for BigInteger */
        private byte[] toBytes(int[] words) {
            byte[] bytes = new byte[words.length * 4];
            for (int i = 0; i < words.length; i++) {
                int v = words[i];
                // little‑endian order per library contract
                bytes[i * 4] = (byte) (v);
                bytes[i * 4 + 1] = (byte) (v >>> 8);
                bytes[i * 4 + 2] = (byte) (v >>> 16);
                bytes[i * 4 + 3] = (byte) (v >>> 24);
            }
            return bytes;
        }
    }

    @Benchmark
    public UInt256 mutableMultiply(BenchmarkState s) {
        // reuse temp to avoid allocations
        return s.temp.set(s.a).mMultiply(s.b);
    }

    @Benchmark
    public UInt256 immutableMultiply(BenchmarkState s) {
        return s.a.multiply(s.b);
    }

    @Benchmark
    public BigInteger bigIntegerMultiply(BenchmarkState s) {
        return s.bigA.multiply(s.bigB);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DeepMultiplyBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
