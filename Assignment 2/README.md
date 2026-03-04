# Ring Buffer (Single Writer, Multiple Readers)

## Project Overview

This project implements a **Ring Buffer data structure** that supports a **single writer and multiple readers**.  
The buffer has a **fixed capacity N**, and when the buffer becomes full, the writer is allowed to **overwrite the oldest data**.

The key challenge in this assignment is supporting **multiple readers that can read independently**. This means that when one reader reads an item from the buffer, the item **must not be removed for other readers**. Each reader must maintain its own reading position.

Because the buffer allows overwriting, **slow readers may miss some items** if those items are overwritten before the reader accesses them.

The solution is implemented using **Object-Oriented Design principles**, where responsibilities are clearly separated between classes instead of putting all logic in a single class.

---

## Design Explanation

The system is designed around two main classes:

### RingBuffer\<T\>

The `RingBuffer` class represents the shared circular buffer that stores the data.

Responsibilities of this class include:

- Maintaining the **fixed-size storage array** where items are stored.
- Managing the **capacity of the buffer**.
- Keeping track of the **writer sequence number (`writeSeq`)**.
- Writing items into the buffer using circular indexing.
- Allowing data to be **overwritten when the buffer becomes full**.
- Providing helper methods that allow readers to safely access buffer data.
- Creating reader objects using the `createReader()` method.

When the writer calls `write(item)`, the buffer:

1. Retrieves the current `writeSeq`.
2. Calculates the correct position in the circular array using `seq % capacity`.
3. Writes the item into the buffer.
4. Increments `writeSeq` to move the writer forward.

Only **one writer** is allowed to call this method.

---

### Reader\<T\>

The `Reader` class represents an **independent reader** of the ring buffer.

Each reader has its own internal state and does not interfere with other readers.

Responsibilities of this class include:

- Maintaining its own **reading position (`nextSeq`)**.
- Reading items from the buffer without removing them.
- Detecting when it has become **too slow and data has been overwritten**.
- Keeping track of the number of **missed items (`missedCount`)**.
- Returning read results using `Optional<T>`.

When a reader calls `read()`:

1. It first checks the latest write position using `getWriteSeq()`.
2. It checks the **oldest available sequence** using `oldestSeq()`.
3. If the reader is too slow (`nextSeq < oldestSeq`), the reader skips forward and counts missed items.
4. If there is **no new data available**, the method returns `Optional.empty()`.
5. If data is available, the reader retrieves the value using `getAt(nextSeq)`, increments `nextSeq`, and returns `Optional.of(value)`.

Because each reader maintains its own `nextSeq`, **multiple readers can read independently**.

---

## UML Class Diagram

The UML Class Diagram illustrates the structure of the system and the relationships between the main classes.

The diagram shows:

- The `RingBuffer` class, which manages the shared buffer and writing operations.
- The `Reader` class, which represents independent readers of the buffer.
- The relationship where `RingBuffer` **creates** reader instances using `createReader()`.
- The association where `Reader` **reads from** the `RingBuffer`.

<img width="650" height="549" alt="Final Class Diagram" src="https://github.com/user-attachments/assets/ae74de65-ce0e-404a-a076-57874918269c" />


---

## UML Sequence Diagram for write()

The write sequence diagram shows how the **writer interacts with the ring buffer when inserting a new item**.

The process is as follows:

1. The `Writer` calls `write(item)` on the `RingBuffer`.
2. The buffer retrieves the current `writeSeq`.
3. The correct index is calculated using `seq % capacity`.
4. The item is stored in the buffer.
5. The `writeSeq` is incremented.

This process ensures that writing follows the **circular buffer logic** and automatically overwrites the oldest elements when necessary.

<img width="498" height="420" alt="Final Sequence Diagram For Write" src="https://github.com/user-attachments/assets/4c7d5118-5955-4247-ade6-5f3e84a5ca3c" />


---

## UML Sequence Diagram for read()

The read sequence diagram illustrates how a **reader retrieves data from the buffer**.

The sequence includes several conditions:

1. The `ReaderClient` calls `read()` on the `Reader`.
2. The reader checks the current write position using `getWriteSeq()`.
3. The reader checks the oldest available data using `oldestSeq()`.

Two possible situations may occur:

### Reader is too slow

If `nextSeq < oldestSeq`, the reader has fallen behind and some data has been overwritten.

In this case:

- The reader increments `missedCount`.
- The reader jumps forward to the `oldestSeq` position.

### No new data

If `nextSeq >= writeSeq`, there is no new data available for the reader.  
The method returns `Optional.empty()`.

### Data available

If new data exists:

- The reader retrieves the value using `getAt(nextSeq)`.
- The reader increments `nextSeq`.
- The method returns `Optional.of(value)`.

<img width="780" height="671" alt="Final Sequence Diagram For Read" src="https://github.com/user-attachments/assets/3961bda1-aec8-4a6d-9061-7408854c26a9" />



---

## How to Run and Test the Project

To run the project, Java must be installed on your system.

### Step 1: Compile the project

Open a terminal in the project folder and run:
javac *.java

This command compiles all Java source files.

---

### Step 2: Run the program

Execute the main class using:
java Main

This will run the demonstration program which writes values to the buffer and allows readers to read them.

---

## Summary

This project demonstrates how a **ring buffer can support one writer and multiple independent readers** using object-oriented design.

Key features of the implementation include:

- Fixed capacity circular buffer
- Independent readers with their own reading positions
- Overwrite behavior when the buffer becomes full
- Detection of missed items for slow readers
- Clear separation of responsibilities between classes

This design ensures that the system is **scalable, maintainable, and aligned with OO design principles**.
