package test;

import java.util.*;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

public class MyTest4 {

    public static void main(String[] args) {
        int j = 10;
        Benchmark.alloc(1);
        B b = new B();
        Benchmark.alloc(2);
        A a = new A(b);
        Benchmark.alloc(3);
        A c = new A();
        B b1 = new B();
        do {
            a.f = new B();
            j--;
        } while (j > 0);
        Benchmark.test(1, a.f);
        Benchmark.test(2, a);

        for (int i = 10; i > 0; i--) {
            B b2 = new B();
        }
    }
}