import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for RingBuffer and Reader.
 * Assignment 2 - Object Oriented Analysis and Design
 *
 * Tests cover:
 *  - RingBuffer construction and validation
 *  - Basic write / read flow
 *  - Overwrite behaviour (slow reader)
 *  - Multiple independent readers
 *  - Edge cases (capacity-1, capacity, capacity+1 writes)
 *  - missedCount tracking
 *  - Reader created on empty buffer
 *  - Reader created after writes
 */
@DisplayName("RingBuffer Tests")
public class RingBufferTest {

    // -----------------------------------------------------------------------
    // 1. RingBuffer Construction
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Constructor: valid capacity creates buffer successfully")
    void testValidCapacityCreation() {
        assertDoesNotThrow(() -> new RingBuffer<Integer>(1));
        assertDoesNotThrow(() -> new RingBuffer<Integer>(4));
        assertDoesNotThrow(() -> new RingBuffer<Integer>(100));
    }

    @Test
    @DisplayName("Constructor: capacity zero throws IllegalArgumentException")
    void testZeroCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(0));
    }

    @Test
    @DisplayName("Constructor: negative capacity throws IllegalArgumentException")
    void testNegativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(-1));
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(-100));
    }

    @Test
    @DisplayName("Constructor: capacity() returns the value given at construction")
    void testCapacityReturned() {
        RingBuffer<String> buf = new RingBuffer<>(5);
        assertEquals(5, buf.capacity());
    }

    // -----------------------------------------------------------------------
    // 2. Initial State
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Initial writeSeq is 0 before any writes")
    void testInitialWriteSeq() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        assertEquals(0L, buf.getWriteSeq());
    }

    @Test
    @DisplayName("Initial oldestSeq is 0 before any writes")
    void testInitialOldestSeq() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        assertEquals(0L, buf.oldestSeq());
    }

    // -----------------------------------------------------------------------
    // 3. Write behaviour
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("write() increments writeSeq by 1 each time")
    void testWriteSeqIncrementsAfterEachWrite() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        buf.write(10);
        assertEquals(1L, buf.getWriteSeq());
        buf.write(20);
        assertEquals(2L, buf.getWriteSeq());
        buf.write(30);
        assertEquals(3L, buf.getWriteSeq());
    }

    @Test
    @DisplayName("oldestSeq stays 0 while buffer is not yet full")
    void testOldestSeqNotFullYet() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        buf.write(1);
        buf.write(2);
        buf.write(3);
        // 3 writes into capacity-4 buffer → no overwrite yet
        assertEquals(0L, buf.oldestSeq());
    }

    @Test
    @DisplayName("oldestSeq advances when buffer overflows (capacity + 1 writes)")
    void testOldestSeqAdvancesOnOverflow() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        // Fill it exactly
        buf.write(1); buf.write(2); buf.write(3); buf.write(4);
        assertEquals(0L, buf.oldestSeq()); // still 0 right after exactly-full

        // One more write overwrites slot 0
        buf.write(5);
        assertEquals(1L, buf.oldestSeq());
    }

    @Test
    @DisplayName("getAt() retrieves the correct item by sequence number")
    void testGetAtReturnsCorrectItem() {
        RingBuffer<String> buf = new RingBuffer<>(4);
        buf.write("A"); // seq 0
        buf.write("B"); // seq 1
        buf.write("C"); // seq 2

        assertEquals("A", buf.getAt(0));
        assertEquals("B", buf.getAt(1));
        assertEquals("C", buf.getAt(2));
    }

    // -----------------------------------------------------------------------
    // 4. Reader – basic read on a single reader
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("read() on empty buffer returns Optional.empty()")
    void testReadEmptyBufferReturnsEmpty() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();
        assertEquals(Optional.empty(), r.read());
    }

    @Test
    @DisplayName("read() returns written values in order")
    void testReadReturnsValuesInOrder() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        buf.write(1);
        buf.write(2);
        buf.write(3);

        assertEquals(Optional.of(1), r.read());
        assertEquals(Optional.of(2), r.read());
        assertEquals(Optional.of(3), r.read());
    }

    @Test
    @DisplayName("read() returns Optional.empty() after all available data is consumed")
    void testReadReturnsEmptyAfterAllConsumed() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        buf.write(42);
        r.read(); // consume 42
        assertEquals(Optional.empty(), r.read());
    }

    @Test
    @DisplayName("read() sees new writes after being empty")
    void testReadSeesNewWritesAfterEmpty() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        assertEquals(Optional.empty(), r.read());

        buf.write(99);
        assertEquals(Optional.of(99), r.read());
    }

    // -----------------------------------------------------------------------
    // 5. Multiple independent readers
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Two readers created before writes both start from the beginning")
    void testTwoReadersReadIndependently() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r1 = buf.createReader();
        Reader<Integer> r2 = buf.createReader();

        buf.write(1);
        buf.write(2);

        // r1 reads first item
        assertEquals(Optional.of(1), r1.read());

        // r2 still starts at 1 (independent cursor)
        assertEquals(Optional.of(1), r2.read());
        assertEquals(Optional.of(2), r2.read());

        // r1 now reads second item
        assertEquals(Optional.of(2), r1.read());
    }

    @Test
    @DisplayName("Readers created at different times have different starting positions")
    void testReadersCreatedAtDifferentTimes() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);

        buf.write(10); // seq 0
        Reader<Integer> r1 = buf.createReader(); // starts at oldest = 0

        buf.write(20); // seq 1
        Reader<Integer> r2 = buf.createReader(); // starts at oldest = 0 (buffer not full)

        // r1 should read 10 first
        assertEquals(Optional.of(10), r1.read());

        // r2 should also read 10 (oldest is still 0 at creation)
        assertEquals(Optional.of(10), r2.read());
    }

    @Test
    @DisplayName("Three readers all read the same data independently")
    void testThreeIndependentReaders() {
        RingBuffer<Integer> buf = new RingBuffer<>(8);
        Reader<Integer> r1 = buf.createReader();
        Reader<Integer> r2 = buf.createReader();
        Reader<Integer> r3 = buf.createReader();

        buf.write(5);
        buf.write(10);
        buf.write(15);

        assertEquals(Optional.of(5),  r1.read());
        assertEquals(Optional.of(10), r1.read());
        assertEquals(Optional.of(15), r1.read());

        assertEquals(Optional.of(5),  r2.read());
        assertEquals(Optional.of(10), r2.read());
        assertEquals(Optional.of(15), r2.read());

        assertEquals(Optional.of(5),  r3.read());
        assertEquals(Optional.of(10), r3.read());
        assertEquals(Optional.of(15), r3.read());
    }

    // -----------------------------------------------------------------------
    // 6. Overwrite / slow-reader behaviour
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Slow reader skips overwritten data and missedCount is updated")
    void testSlowReaderMissedCount() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        // Write 4 items (fills buffer exactly)
        buf.write(1); buf.write(2); buf.write(3); buf.write(4);

        // Reader consumes only the first item
        assertEquals(Optional.of(1), r.read());

        // Write 2 more – this overwrites seq 0 and seq 1 (r already read seq 0)
        buf.write(5); // overwrites slot 0 (seq 0 → seq 4)
        buf.write(6); // overwrites slot 1 (seq 1 → seq 5)

        // r.nextSeq was at 1, oldestSeq is now 2 → missed 1 item (seq 1)
        Optional<Integer> val = r.read();
        assertTrue(val.isPresent());
        assertTrue(r.getMissedCount() >= 1,
                "missedCount should be at least 1 after an overwrite");
    }

    @Test
    @DisplayName("missedCount is 0 when reader keeps up with writes")
    void testMissedCountZeroWhenReaderKeepsUp() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        buf.write(1); r.read();
        buf.write(2); r.read();
        buf.write(3); r.read();

        assertEquals(0L, r.getMissedCount());
    }

    @Test
    @DisplayName("After overwrite, reader resumes from oldest available sequence")
    void testReaderResumesFromOldestAfterOverwrite() {
        RingBuffer<Integer> buf = new RingBuffer<>(3);
        Reader<Integer> r = buf.createReader();

        // Fill buffer
        buf.write(10); buf.write(20); buf.write(30);

        // Don't read anything → r.nextSeq = 0

        // Write 2 more → overwrites seq 0 and seq 1
        buf.write(40); // overwrites slot 0
        buf.write(50); // overwrites slot 1

        // oldestSeq is now 2; r.nextSeq (0) < oldestSeq (2) → jump to 2
        // So first read should return value at seq 2 = 30
        Optional<Integer> first = r.read();
        assertTrue(first.isPresent());
        assertEquals(30, first.get());
    }

    @Test
    @DisplayName("Exactly capacity+1 writes causes exactly one slot overwrite")
    void testExactlyOneSlotOverwrite() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);

        for (int i = 1; i <= 5; i++) buf.write(i); // 5 = capacity + 1

        // oldestSeq should be 1 (seq 0 was overwritten)
        assertEquals(1L, buf.oldestSeq());
        assertEquals(5L, buf.getWriteSeq());
    }

    // -----------------------------------------------------------------------
    // 7. Reader created after writes (late joiner)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Reader created after buffer is partially filled starts at oldest available")
    void testLateReaderStartsAtOldest() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        buf.write(1);
        buf.write(2);
        buf.write(3);

        // Create reader AFTER writes; buffer not yet overwritten → oldest = 0
        Reader<Integer> r = buf.createReader();

        assertEquals(Optional.of(1), r.read());
        assertEquals(Optional.of(2), r.read());
        assertEquals(Optional.of(3), r.read());
        assertEquals(Optional.empty(), r.read());
    }

    @Test
    @DisplayName("Reader created after full overwrite starts at current oldest (not seq 0)")
    void testLateReaderAfterOverwrite() {
        RingBuffer<Integer> buf = new RingBuffer<>(3);
        buf.write(1); buf.write(2); buf.write(3); // full
        buf.write(4); // overwrites slot 0; oldest = 1

        Reader<Integer> r = buf.createReader();

        // Should start at oldestSeq = 1 → values 2, 3, 4
        assertEquals(Optional.of(2), r.read());
        assertEquals(Optional.of(3), r.read());
        assertEquals(Optional.of(4), r.read());
        assertEquals(Optional.empty(), r.read());
    }

    // -----------------------------------------------------------------------
    // 8. getNextSeq
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Reader.getNextSeq() starts at oldestSeq when created on empty buffer")
    void testGetNextSeqInitialOnEmptyBuffer() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();
        assertEquals(buf.oldestSeq(), r.getNextSeq());
    }

    @Test
    @DisplayName("Reader.getNextSeq() advances after each successful read")
    void testGetNextSeqAdvancesOnRead() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        buf.write(100);
        buf.write(200);

        long before = r.getNextSeq(); // 0
        r.read();
        assertEquals(before + 1, r.getNextSeq());
        r.read();
        assertEquals(before + 2, r.getNextSeq());
    }

    // -----------------------------------------------------------------------
    // 9. Buffer with capacity 1 (extreme edge case)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Capacity-1 buffer: every new write overwrites the previous one")
    void testCapacityOneOverwritesEveryTime() {
        RingBuffer<Integer> buf = new RingBuffer<>(1);
        Reader<Integer> r = buf.createReader();

        buf.write(10);
        buf.write(20); // overwrites 10

        // Only 20 should be available; reader missed 10
        Optional<Integer> val = r.read();
        assertTrue(val.isPresent());
        assertEquals(20, val.get());
        assertTrue(r.getMissedCount() >= 1);
    }

    @Test
    @DisplayName("Capacity-1 buffer: sequential write-then-read works without loss")
    void testCapacityOneReadAfterEachWrite() {
        RingBuffer<Integer> buf = new RingBuffer<>(1);
        Reader<Integer> r = buf.createReader();

        buf.write(1);
        assertEquals(Optional.of(1), r.read());

        buf.write(2);
        assertEquals(Optional.of(2), r.read());

        assertEquals(0L, r.getMissedCount());
    }

    // -----------------------------------------------------------------------
    // 10. Generic type support
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("RingBuffer works with String type")
    void testStringType() {
        RingBuffer<String> buf = new RingBuffer<>(4);
        Reader<String> r = buf.createReader();

        buf.write("hello");
        buf.write("world");

        assertEquals(Optional.of("hello"), r.read());
        assertEquals(Optional.of("world"), r.read());
    }

    @Test
    @DisplayName("RingBuffer works with custom object type")
    void testCustomObjectType() {
        RingBuffer<int[]> buf = new RingBuffer<>(4);
        Reader<int[]> r = buf.createReader();

        int[] arr = {1, 2, 3};
        buf.write(arr);

        Optional<int[]> result = r.read();
        assertTrue(result.isPresent());
        assertArrayEquals(arr, result.get());
    }

    // -----------------------------------------------------------------------
    // 11. No missed items when reader stays ahead
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("No data loss when reader always reads before next write overflows")
    void testNoDataLossWithDiligentReader() {
        RingBuffer<Integer> buf = new RingBuffer<>(4);
        Reader<Integer> r = buf.createReader();

        for (int i = 0; i < 20; i++) {
            buf.write(i);
            Optional<Integer> val = r.read();
            assertTrue(val.isPresent());
            assertEquals(i, val.get());
        }

        assertEquals(0L, r.getMissedCount());
    }
}