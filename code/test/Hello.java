package test;
import java.util.*;
import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

public class Hello {
    private static final Random random = new Random();
  public static void main(String[] args) {
    Benchmark.alloc(1);
        A b = new A();
        Benchmark.alloc(2);
        A a = new A();
        Benchmark.alloc(3);
        a.g = new B();
        if (random.nextInt() < random.nextInt()) {
           b.f = a.g;
        }
        Benchmark.alloc(4);
        B e = new B();
        //assign(a, c);
        B d = b.f;
        a.g = e;

        Benchmark.test(1, d); // expected: 1 2 3
        Benchmark.test(2, a.g); // expected: 2 4
        Benchmark.test(3, a); // expected: 1 2 3
        Benchmark.test(4, b.f);
  }
}
