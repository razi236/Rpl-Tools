package org.rpl.frontend.analyser;

import java.util.*;

/**
 * A bit vec specialized for data flow analysis. Instead of mutating the vector, operations create a new
 * vector. This ensures correct analysis.
 * Adapted from https://bitbucket.org/jastadd/jastaddj-intraflow/src/master/src/jastadd/Sets.jrag
 *
 * @param <E> - The type of the elements in the vector
 */
public class BitVec<E> extends AbstractSet<E> implements Iterable<E> {
    /**
     * Maps elements to the position in `elements`
     */
    protected HashMap<E, Integer> elementMap;
    /**
     * Keeps all elements in the vec
     */
    protected ArrayList<E> elements;
    /**
     * The collection of bits
     */
    int[] bits;

    public BitVec() {
        this(new HashMap<>(), new ArrayList<>(), new int[1]);
    }

    public BitVec(E e) {
        this();
        add(e);
    }

    protected BitVec(HashMap<E, Integer> elementMap, ArrayList<E> elements, int[] bits) {
        this.elementMap = elementMap;
        this.elements = elements;
        this.bits = bits;
    }

    protected BitVec(BitVec<E> vec) {
        this(vec, vec.bits.length);
    }

    protected BitVec(BitVec<E> set, int size) {
        this(set.elementMap, set.elements, new int[size]);
        System.arraycopy(set.bits, 0, bits, 0, set.bits.length);
    }

    protected int addIndex(E e) {
        if (!elementMap.containsKey(e)) {
            int idx = elements.size();
            elementMap.put(e, idx);
            elements.add(e);
            return idx;
        }
        return elementMap.get(e);
    }

    protected E get(int idx) {
        return elements.get(idx);
    }

    /**
     * Adds all elements from `vec` to this
     * @param vec - The vector to add
     */
    public void add(BitVec<E> vec) {
        vec = convert(vec);
        if (vec.bits.length > bits.length) {
            int[] newBits = new int[vec.bits.length];
            for (int i = 0; i < bits.length; i++)
                newBits[i] = bits[i] | vec.bits[i];
            System.arraycopy(vec.bits, bits.length, newBits, bits.length, vec.bits.length - bits.length);
            bits = newBits;
        } else {
            for (int i = 0; i < vec.bits.length; i++)
                bits[i] |= vec.bits[i];
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (E e : this) {
            if (!c.contains(e)) {
                changed = remove(e);
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed = remove(o);
        }
        return changed;
    }

    @Override
    public void clear() {
        Arrays.fill(bits, 0);
    }

    @Override
    public boolean add(E e) {
        int idx = addIndex(e);
        int offset = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        if (offset >= bits.length) {
            int[] newBits = new int[offset + 1];
            System.arraycopy(bits, 0, newBits, 0, bits.length);
            bits = newBits;
        }
        boolean changed = (bits[offset] & bit) == 0;
        bits[offset] |= bit;
        return changed;
    }

    @Override
    public boolean remove(Object o) {
        E e = (E) o;
        int idx = addIndex(e);
        int offset = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        if (offset >= bits.length) {
            int[] newBits = new int[offset + 1];
            System.arraycopy(bits, 0, newBits, 0, bits.length);
            bits = newBits;
        }
        boolean changed = (bits[offset] & bit) != 0;
        bits[offset] &= ~bit;
        return changed;
    }

    /**
     *
     * @return The number of elements in this vector
     */
    public int size() {
        int size = 0;
        for (int bit : bits) {
            size += Integer.bitCount(bit);
        }
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int nextElement;

            {
                // make nextElement refer to the first element
                nextElement = 0;
                hasNext();
            }

            @Override
            public boolean hasNext() {
                while ((nextElement >> 5) < bits.length) {   // while (nextElement / 32 < bits.length)
                    int offset = nextElement >> 5;            //   nextElement / 32
                    int bit = 1 << (nextElement & 0x1f);      //   nextElement % 32
                    if ((bits[offset] & bit) == bit)
                        return true;
                    nextElement++;
                }
                return false;
            }

            @Override
            public E next() {
                return get(nextElement++);
            }
        };
    }

    /**
     * Creates a new vector that uses the same map and list as `this`
     * Allows further operations
     * @param vec - The vector to convert
     * @return - A new vector with converted elementMap and elements
     */
    private BitVec<E> convert(BitVec<E> vec) {
        if (vec.elementMap == elementMap) return vec;
        BitVec<E> newVec = new BitVec<>(elementMap, elements, new int[bits.length + 1]);
        newVec.addAll(vec);
        return newVec;
    }

    /**
     * Creates a new vector that is the union of `this` and `vec`
     * @param vec - The other vector
     * @return - A union vector
     */
    public BitVec<E> union(BitVec<E> vec) {
        vec = convert(vec);
        if (vec.isEmpty() || this.equals(vec)) return this;

        BitVec<E> min, max;
        if (bits.length >= vec.bits.length) {
            max = this;
            min = vec;
        } else {
            max = vec;
            min = this;
        }
        int length = min.bits.length;
        int i = 0;
        while (i < length && (max.bits[i] & min.bits[i]) == min.bits[i]) {
            i++;
        }
        if (i != length) {
            max = new BitVec<>(max);
            for (; i < length; i++)
                max.bits[i] |= min.bits[i];
        }
        return max;
    }

    /**
     * Creates a new vector with `e` added
     * @param e - The element to add
     * @return - A union vector
     */
    public BitVec<E> union(E e) {
        int idx = addIndex(e);
        int offset = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        if (bits.length > offset && (bits[offset] & bit) == bit) return this;
        BitVec<E> vec = new BitVec<>(this, Math.max(offset + 1, bits.length));
        vec.bits[offset] |= bit;
        return vec;
    }

    /**
     * Creates a new vector that has all elements of `vec` removed
     * @param vec - The other vector
     * @return - A complement vector
     */
    public BitVec<E> compl(BitVec<E> vec) {
        vec = convert(vec);
        if (vec.isEmpty()) return this;
        BitVec<E> res = new BitVec<>(this);
        int i = 0;
        int length = Math.min(bits.length, vec.bits.length);
        while (i < length && (bits[i] & vec.bits[i]) == 0)
            i++;
        if (i != length) {
            for (; i < length; i++) {
                res.bits[i] &= ~vec.bits[i];
            }
        }
        return res;
    }

    /**
     * Creates a new vector with `e` removed
     * @param e - The element to remove
     * @return - A complement vector
     */
    public BitVec<E> compl(E e) {
        int idx = addIndex(e);
        int offset = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        if (bits.length > offset && (bits[offset] & bit) == 0)
            return this;
        BitVec<E> vec = new BitVec<>(this, Math.max(offset + 1, bits.length));
        vec.bits[offset] &= ~bit;
        return vec;
    }

    /**
     * Creates a new vector that only has elements both in `this` and `vec`
     * @param vec - The other vector
     * @return - An intersection vector
     */
    public BitVec<E> intersect(BitVec<E> vec) {
        vec = convert(vec);
        if (this.equals(vec) || vec.isFullVec()) return this;
        int length = Math.min(bits.length, vec.bits.length);
        BitVec<E> res = new BitVec<>(elementMap, elements, new int[length]);
        for (int i = 0; i < length; i++) {
            res.bits[i] = this.bits[i] & vec.bits[i];
        }
        return res;
    }

    /**
     * Checks whether `o` is in this vector
     * @param o - The object to test
     * @return whether `o` is in this vector
     */
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        E e = (E) o;
        int idx = addIndex(e);
        int offset = idx >> 5;
        int bit = 1 << (idx & 0x1f);
        return offset < bits.length && (bits[offset] & bit) != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof BitVec) {
            BitVec<E> vec = convert((BitVec<E>) o);
            if (elementMap != vec.elementMap)
                return false;
            int length = Math.min(vec.bits.length, bits.length);
            int i = 0;
            for (; i < length; i++)
                if (bits[i] != vec.bits[i])
                    return false;
            for (; i < bits.length; i++)
                if (bits[i] != 0)
                    return false;
            for (; i < vec.bits.length; i++)
                if (vec.bits[i] != 0)
                    return false;
            return true;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(elementMap, elements);
        result = 31 * result + Arrays.hashCode(bits);
        return result;
    }

    /**
     *
     * @return Whether the current vector is empty
     */
    public boolean isEmpty() {
        for (int bit : bits)
            if (bit != 0)
                return false;
        return true;
    }

    protected boolean isFullVec() {
        return false;
    }

    public static <E> BitVec<E> full() {
        return new BitVec<E>(null, null, null) {
            public BitVec<E> union(BitVec<E> vec) {
                return this;
            }

            public BitVec<E> union(E e) {
                return this;
            }

            public BitVec<E> compl(BitVec<E> vec) {
                throw new Error("compl not supported for the full vec");
            }

            public BitVec<E> compl(E e) {
                throw new Error("compl not supported for the full vec");
            }

            public BitVec<E> intersect(BitVec<E> vec) {
                return vec;
            }

            public boolean isEmpty() {
                return false;
            }

            protected boolean isFullVec() {
                return true;
            }
        };
    }

    public String toString() {
        if (isFullVec()) {
            return "<FULL>";
        }

        StringBuilder s = new StringBuilder("{");

        for (E e : this) {
            s.append(", ").append(e.toString());
        }

        return s + "}";
    }
}
