import java.util.Optional;

/**
 * Single-writer, multiple-reader ring buffer.
 * Writer may overwrite old data when capacity is exceeded.
 *
 * @param <T> type of stored items
 */
public class RingBuffer<T> {

    private final Object[] data;
    private final int capacity;

    // next sequence to write (monotonic increasing)
    private volatile long writeSeq = 0;

    public RingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.data = new Object[capacity];
    }

    /**
     * Single writer writes item.
     * Overwrites oldest data automatically when full.
     */
    public synchronized void write(T item) {
        long seq = writeSeq;
        int index = indexOf(seq);
        data[index] = item;
        writeSeq = seq + 1;
    }

    /**
     * Create a new independent reader.
     * New reader starts from the oldest currently available item
     * (or from 0 if buffer is still empty).
     */
    public Reader<T> createReader() {
        long startSeq = oldestSeq();
        return new Reader<>(this, startSeq);
    }

    int indexOf(long seq) {
        return (int) (seq % capacity);
    }

    long getWriteSeq() {
        return writeSeq;
    }

    long oldestSeq() {
        long ws = writeSeq;
        long oldest = ws - capacity;
        return Math.max(0, oldest);
    }

    @SuppressWarnings("unchecked")
    T getAt(long seq) {
        return (T) data[indexOf(seq)];
    }

    public int capacity() {
        return capacity;
    }
}
