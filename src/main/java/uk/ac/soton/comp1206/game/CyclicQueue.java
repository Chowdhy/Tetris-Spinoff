package uk.ac.soton.comp1206.game;


/**
 * An implementation of a Queue that cycles through an array, only queues numbers.
 */
public class CyclicQueue {
  private final int[] queue;
  private int head;
  private int tail;
  private boolean empty;

  /**
   * Creates a queue based on the sie passed
   * @param size The size of the queue
   */
  public CyclicQueue(int size) {
    queue = new int[size];
    empty = true;
  }

  /**
   * Adds the requested integer to the queue if there is space
   * @param integer The number to be added
   * @throws IndexOutOfBoundsException Thrown if the queue is full
   */
  public void enqueue(int integer) throws IndexOutOfBoundsException {
    if (empty) {
      empty = false;
    } else if (isFull()) {
      throw new IndexOutOfBoundsException();
    } else {
      tail = (tail + 1) % queue.length;
    }
    queue[tail] = integer;
  }

  /**
   * Returns and removes a number from the queue if the queue is not empty.
   * @return The number at the head of the queue.
   * @throws IndexOutOfBoundsException Thrown if the queue is empty.
   */
  public int dequeue() throws IndexOutOfBoundsException {
    if (empty) {
      throw new IndexOutOfBoundsException();
    }
    int returnValue = queue[head];
    if (head == tail) {
      empty = true;
    } else {
      head = (head + 1) % queue.length;
    }
    return returnValue;
  }

  /**
   * Returns whether the queue is empty
   * @return A boolean representing whether the queue is empty
   */
  public boolean isEmpty() {
    return empty;
  }

  /**
   * Returns whether the queue is full
   * @return A boolean representing whether the queue is full
   */
  public boolean isFull() {
    return (queue.length != 1 && ((tail + 1) % queue.length) == head) || (queue.length == 1 && !empty);
  }

  /**
   * Returns the size of the queue
   * @return The size of the queue
   */
  public int getSize() {
    return queue.length;
  }
}
