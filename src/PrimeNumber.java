import java.util.Scanner;
public class PrimeNumber {
    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }

        for (int i = 2; i < n; i++) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a number: ");
            String input = args.length > 0 ? args[0] : scanner.nextLine();
            try {
                int n = Integer.parseInt(input.trim());
                System.out.println(n + (isPrime(n) ? " is a prime number." : " is not a prime number."));
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid 32-bit whole number.");
            }
        }
    }
}
