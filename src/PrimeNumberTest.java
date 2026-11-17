public class PrimeNumberTest {
    public static void main(String[] args) {
        int[] primes = {2, 3, 17, 97, 2147483647};
        int[] others = {-7, 0, 1, 4, 9, 25, 100, 2147483646};
        for (int n : primes) {
            if (!PrimeNumber.isPrime(n)) throw new AssertionError("Expected prime: " + n);
        }
        for (int n : others) {
            if (PrimeNumber.isPrime(n)) throw new AssertionError("Expected non-prime: " + n);
        }
        System.out.println("PASS: 13 prime-number checks.");
    }
}
