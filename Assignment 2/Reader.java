import java.util.Optional;

/**
 * Independent reader cursor for a RingBuffer.
 * Each reader has its own reading position (nextSeq).
 */
public class Reader<T> {

    private final RingBuffer<T> buffer;

    // next sequence this reader will read
    private long nextSeq;

    // optional: how many items were missed due to overwrite
    private long missedCount = 0;

    Reader(RingBuffer<T> buffer, long startSeq) {
        this.buffer = buffer;
        this.nextSeq = startSeq;
    }

    /**
     * Non-blocking read:
     * - returns Optional.empty() if there is nothing new for this reader
     * - if reader fell behind (overwritten), it skips to oldest available
     */
    public synchronized Optional<T> read() {
        long writeSeq = buffer.getWriteSeq();
        long oldest = buffer.oldestSeq();

        // Reader is too slow -> data overwritten -> jump forward
        if (nextSeq < oldest) {
            missedCount += (oldest - nextSeq);
            nextSeq = oldest;
        }

        // nothing new available
        if (nextSeq >= writeSeq) {
            return Optional.empty();
        }

        // read available item
        T value = buffer.getAt(nextSeq);
        nextSeq++;
        return Optional.ofNullable(value);
    }

    public synchronized long getMissedCount() {
        return missedCount;
    }

    public synchronized long getNextSeq() {
        return nextSeq;
    }
}
