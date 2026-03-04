public class Main {
    public static void main(String[] args) {
        RingBuffer<Integer> buffer = new RingBuffer<>(4);

        Reader<Integer> r1 = buffer.createReader();
        Reader<Integer> r2 = buffer.createReader();

        buffer.write(1);
        buffer.write(2);
        buffer.write(3);

        System.out.println("R1 reads: " + r1.read().orElse(null)); // 1
        System.out.println("R1 reads: " + r1.read().orElse(null)); // 2

        System.out.println("R2 reads: " + r2.read().orElse(null)); // 1 (independent)
        System.out.println("R2 reads: " + r2.read().orElse(null)); // 2

        buffer.write(4);
        buffer.write(5);
        buffer.write(6); // overwrites oldest because capacity=4

        // Slow reader example (r2 is slower now)
        System.out.println("R2 reads: " + r2.read().orElse(null));
        System.out.println("R2 missed count: " + r2.getMissedCount());

        // r1 continues
        System.out.println("R1 reads: " + r1.read().orElse(null));
        System.out.println("R1 reads: " + r1.read().orElse(null));
    }
}
