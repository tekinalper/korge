@file:Suppress("USELESS_CAST")

package com.soywiz.kds

import com.soywiz.kds.annotations.Template
import com.soywiz.kds.internal.arraycopy
import com.soywiz.kds.internal.contentHashCode
import com.soywiz.kds.internal.umod
import kotlin.math.min

typealias Deque<TGen> = TGenDeque<TGen>

typealias CircularList<TGen> = TGenDeque<TGen>

fun <T> Collection<T>.toDeque(): Deque<T> = Deque(this)
fun <T> Deque(other: Collection<T>): Deque<T> = Deque<T>(other.size).also { it.addAll(other) }

// @NOTE: AUTOGENERATED: ONLY MODIFY FROM  GENERIC TEMPLATE to END OF GENERIC TEMPLATE
// Then use ./gradlew generate to regenerate the rest of the file.

// GENERIC TEMPLATE //////////////////////////////////////////

/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
@Template
open class TGenDeque<TGen>(initialCapacity: Int) : MutableCollection<TGen> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: Array<TGen> = arrayOfNulls<Any>(initialCapacity) as Array<TGen>
    private val _data: Array<Any?> get() = data.fastCastTo()
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val _o = arrayOfNulls<Any>(maxOf(this.data.size + 7, maxOf(size + count, this.data.size * 2)))
            val o = _o as Array<TGen>
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: Array<TGen>, istart: Int, o: Array<TGen>, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addLast(item: TGen) {
        resizeIfRequiredFor(1)
        _addLast(item)
    }

    fun addFirst(item: TGen) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addAll(array: Array<TGen>): Boolean = _addAll(array.size) { array[it] }
    fun addAll(list: List<TGen>): Boolean = _addAll(list.size) { list[it] }
    fun addAll(items: Iterable<TGen>): Boolean = addAll(items.toList())
    override fun addAll(elements: Collection<TGen>): Boolean = addAll(elements.toList())

    fun addAllFirst(items: Array<TGen>): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: List<TGen>): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: Iterable<TGen>): Boolean = addAllFirst(items.toList())
    fun addAllFirst(items: Collection<TGen>): Boolean = addAllFirst(items.toList())

    private inline fun _addAll(count: Int, block: (Int) -> TGen): Boolean {
        resizeIfRequiredFor(count)
        val base = _start + _size
        for (n in 0 until count) data[(base + n) % capacity] = block(n)
        _size += count
        return true
    }

    private inline fun _addAllFirst(count: Int, block: (Int) -> TGen): Boolean {
        resizeIfRequiredFor(count)
        _start = (_start - count) umod capacity
        _size += count
        var pos = _start
        for (n in 0 until count) data[pos++ umod capacity] = block(n)
        return true
    }

    private fun _addLast(item: TGen) {
        data[(_start + _size) % capacity] = item
        _size++
    }

    private fun nullify(index: Int) {
        _data[index] = null/*TGen*/ // Prevent leaks
    }

    fun removeFirst(): TGen {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = first
        nullify(_start)
        _start = (_start + 1) % capacity;
        _size--
        return out
    }

    fun removeLast(): TGen {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = last
        nullify(internalIndex(size - 1))
        _size--
        return out
    }

    fun removeAt(index: Int): TGen {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: TGen): Boolean = true.apply { addLast(element) }
    override fun clear() { _size = 0 }
    override fun remove(element: TGen): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<TGen>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<TGen>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<TGen>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val _temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                _temp[tsize++] = c
            }
        }
        this.data = _temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: TGen get() = data[_start]
    val last: TGen get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: TGen) { data[internalIndex(index)] = value }
    operator fun get(index: Int): TGen = data[internalIndex(index)]

    fun getOrNull(index: Int): TGen? = if (index in indices) get(index) else null

    override fun contains(element: TGen): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: TGen): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<TGen>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<TGen> {
        val that = this
        return object : MutableIterator<TGen> {
            var index = 0
            override fun next(): TGen = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is TGenDeque<*/*_TGen_*/>) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}

// END OF GENERIC TEMPLATE ///////////////////////////////////

// AUTOGENERATED: DO NOT MODIFY MANUALLY STARTING FROM HERE!

// Int


/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
@Template
open class IntDeque(initialCapacity: Int) : MutableCollection<Int> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: IntArray = IntArray(initialCapacity) as IntArray
    private val _data: IntArray get() = data.fastCastTo()
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val _o = IntArray(maxOf(this.data.size + 7, maxOf(size + count, this.data.size * 2)))
            val o = _o as IntArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: IntArray, istart: Int, o: IntArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addLast(item: Int) {
        resizeIfRequiredFor(1)
        _addLast(item)
    }

    fun addFirst(item: Int) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addAll(array: IntArray): Boolean = _addAll(array.size) { array[it] }
    fun addAll(list: List<Int>): Boolean = _addAll(list.size) { list[it] }
    fun addAll(items: Iterable<Int>): Boolean = addAll(items.toList())
    override fun addAll(elements: Collection<Int>): Boolean = addAll(elements.toList())

    fun addAllFirst(items: IntArray): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: List<Int>): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: Iterable<Int>): Boolean = addAllFirst(items.toList())
    fun addAllFirst(items: Collection<Int>): Boolean = addAllFirst(items.toList())

    private inline fun _addAll(count: Int, block: (Int) -> Int): Boolean {
        resizeIfRequiredFor(count)
        val base = _start + _size
        for (n in 0 until count) data[(base + n) % capacity] = block(n)
        _size += count
        return true
    }

    private inline fun _addAllFirst(count: Int, block: (Int) -> Int): Boolean {
        resizeIfRequiredFor(count)
        _start = (_start - count) umod capacity
        _size += count
        var pos = _start
        for (n in 0 until count) data[pos++ umod capacity] = block(n)
        return true
    }

    private fun _addLast(item: Int) {
        data[(_start + _size) % capacity] = item
        _size++
    }

    private fun nullify(index: Int) {
        _data[index] = 0 // Prevent leaks
    }

    fun removeFirst(): Int {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = first
        nullify(_start)
        _start = (_start + 1) % capacity;
        _size--
        return out
    }

    fun removeLast(): Int {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = last
        nullify(internalIndex(size - 1))
        _size--
        return out
    }

    fun removeAt(index: Int): Int {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Int): Boolean = true.apply { addLast(element) }
    override fun clear() { _size = 0 }
    override fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Int>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Int>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Int>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val _temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                _temp[tsize++] = c
            }
        }
        this.data = _temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Int get() = data[_start]
    val last: Int get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Int) { data[internalIndex(index)] = value }
    operator fun get(index: Int): Int = data[internalIndex(index)]

    fun getOrNull(index: Int): Int? = if (index in indices) get(index) else null

    override fun contains(element: Int): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Int): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Int> {
        val that = this
        return object : MutableIterator<Int> {
            var index = 0
            override fun next(): Int = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is IntDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}



// Double


/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
@Template
open class DoubleDeque(initialCapacity: Int) : MutableCollection<Double> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: DoubleArray = DoubleArray(initialCapacity) as DoubleArray
    private val _data: DoubleArray get() = data.fastCastTo()
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val _o = DoubleArray(maxOf(this.data.size + 7, maxOf(size + count, this.data.size * 2)))
            val o = _o as DoubleArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: DoubleArray, istart: Int, o: DoubleArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addLast(item: Double) {
        resizeIfRequiredFor(1)
        _addLast(item)
    }

    fun addFirst(item: Double) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addAll(array: DoubleArray): Boolean = _addAll(array.size) { array[it] }
    fun addAll(list: List<Double>): Boolean = _addAll(list.size) { list[it] }
    fun addAll(items: Iterable<Double>): Boolean = addAll(items.toList())
    override fun addAll(elements: Collection<Double>): Boolean = addAll(elements.toList())

    fun addAllFirst(items: DoubleArray): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: List<Double>): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: Iterable<Double>): Boolean = addAllFirst(items.toList())
    fun addAllFirst(items: Collection<Double>): Boolean = addAllFirst(items.toList())

    private inline fun _addAll(count: Int, block: (Int) -> Double): Boolean {
        resizeIfRequiredFor(count)
        val base = _start + _size
        for (n in 0 until count) data[(base + n) % capacity] = block(n)
        _size += count
        return true
    }

    private inline fun _addAllFirst(count: Int, block: (Int) -> Double): Boolean {
        resizeIfRequiredFor(count)
        _start = (_start - count) umod capacity
        _size += count
        var pos = _start
        for (n in 0 until count) data[pos++ umod capacity] = block(n)
        return true
    }

    private fun _addLast(item: Double) {
        data[(_start + _size) % capacity] = item
        _size++
    }

    private fun nullify(index: Int) {
        _data[index] = 0.0 // Prevent leaks
    }

    fun removeFirst(): Double {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = first
        nullify(_start)
        _start = (_start + 1) % capacity;
        _size--
        return out
    }

    fun removeLast(): Double {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = last
        nullify(internalIndex(size - 1))
        _size--
        return out
    }

    fun removeAt(index: Int): Double {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Double): Boolean = true.apply { addLast(element) }
    override fun clear() { _size = 0 }
    override fun remove(element: Double): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Double>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Double>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Double>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val _temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                _temp[tsize++] = c
            }
        }
        this.data = _temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Double get() = data[_start]
    val last: Double get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Double) { data[internalIndex(index)] = value }
    operator fun get(index: Int): Double = data[internalIndex(index)]

    fun getOrNull(index: Int): Double? = if (index in indices) get(index) else null

    override fun contains(element: Double): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Double): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Double>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Double> {
        val that = this
        return object : MutableIterator<Double> {
            var index = 0
            override fun next(): Double = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is DoubleDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}



// Float


/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
@Template
open class FloatDeque(initialCapacity: Int) : MutableCollection<Float> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: FloatArray = FloatArray(initialCapacity) as FloatArray
    private val _data: FloatArray get() = data.fastCastTo()
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val _o = FloatArray(maxOf(this.data.size + 7, maxOf(size + count, this.data.size * 2)))
            val o = _o as FloatArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: FloatArray, istart: Int, o: FloatArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addLast(item: Float) {
        resizeIfRequiredFor(1)
        _addLast(item)
    }

    fun addFirst(item: Float) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addAll(array: FloatArray): Boolean = _addAll(array.size) { array[it] }
    fun addAll(list: List<Float>): Boolean = _addAll(list.size) { list[it] }
    fun addAll(items: Iterable<Float>): Boolean = addAll(items.toList())
    override fun addAll(elements: Collection<Float>): Boolean = addAll(elements.toList())

    fun addAllFirst(items: FloatArray): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: List<Float>): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: Iterable<Float>): Boolean = addAllFirst(items.toList())
    fun addAllFirst(items: Collection<Float>): Boolean = addAllFirst(items.toList())

    private inline fun _addAll(count: Int, block: (Int) -> Float): Boolean {
        resizeIfRequiredFor(count)
        val base = _start + _size
        for (n in 0 until count) data[(base + n) % capacity] = block(n)
        _size += count
        return true
    }

    private inline fun _addAllFirst(count: Int, block: (Int) -> Float): Boolean {
        resizeIfRequiredFor(count)
        _start = (_start - count) umod capacity
        _size += count
        var pos = _start
        for (n in 0 until count) data[pos++ umod capacity] = block(n)
        return true
    }

    private fun _addLast(item: Float) {
        data[(_start + _size) % capacity] = item
        _size++
    }

    private fun nullify(index: Int) {
        _data[index] = 0f // Prevent leaks
    }

    fun removeFirst(): Float {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = first
        nullify(_start)
        _start = (_start + 1) % capacity;
        _size--
        return out
    }

    fun removeLast(): Float {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = last
        nullify(internalIndex(size - 1))
        _size--
        return out
    }

    fun removeAt(index: Int): Float {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Float): Boolean = true.apply { addLast(element) }
    override fun clear() { _size = 0 }
    override fun remove(element: Float): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Float>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Float>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Float>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val _temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                _temp[tsize++] = c
            }
        }
        this.data = _temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Float get() = data[_start]
    val last: Float get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Float) { data[internalIndex(index)] = value }
    operator fun get(index: Int): Float = data[internalIndex(index)]

    fun getOrNull(index: Int): Float? = if (index in indices) get(index) else null

    override fun contains(element: Float): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Float): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Float>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Float> {
        val that = this
        return object : MutableIterator<Float> {
            var index = 0
            override fun next(): Float = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is FloatDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}



// Byte


/**
 * Deque structure supporting constant time of appending/removing from the start or the end of the list
 * when there is room in the underlying array.
 */
@Template
open class ByteDeque(initialCapacity: Int) : MutableCollection<Byte> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: ByteArray = ByteArray(initialCapacity) as ByteArray
    private val _data: ByteArray get() = data.fastCastTo()
    private val capacity: Int get() = data.size

    constructor() : this(initialCapacity = 16)

    override val size: Int get() = _size

    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val _o = ByteArray(maxOf(this.data.size + 7, maxOf(size + count, this.data.size * 2)))
            val o = _o as ByteArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: ByteArray, istart: Int, o: ByteArray, count: Int) {
        val size1 = min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addLast(item: Byte) {
        resizeIfRequiredFor(1)
        _addLast(item)
    }

    fun addFirst(item: Byte) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addAll(array: ByteArray): Boolean = _addAll(array.size) { array[it] }
    fun addAll(list: List<Byte>): Boolean = _addAll(list.size) { list[it] }
    fun addAll(items: Iterable<Byte>): Boolean = addAll(items.toList())
    override fun addAll(elements: Collection<Byte>): Boolean = addAll(elements.toList())

    fun addAllFirst(items: ByteArray): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: List<Byte>): Boolean = _addAllFirst(items.size) { items[it] }
    fun addAllFirst(items: Iterable<Byte>): Boolean = addAllFirst(items.toList())
    fun addAllFirst(items: Collection<Byte>): Boolean = addAllFirst(items.toList())

    private inline fun _addAll(count: Int, block: (Int) -> Byte): Boolean {
        resizeIfRequiredFor(count)
        val base = _start + _size
        for (n in 0 until count) data[(base + n) % capacity] = block(n)
        _size += count
        return true
    }

    private inline fun _addAllFirst(count: Int, block: (Int) -> Byte): Boolean {
        resizeIfRequiredFor(count)
        _start = (_start - count) umod capacity
        _size += count
        var pos = _start
        for (n in 0 until count) data[pos++ umod capacity] = block(n)
        return true
    }

    private fun _addLast(item: Byte) {
        data[(_start + _size) % capacity] = item
        _size++
    }

    private fun nullify(index: Int) {
        _data[index] = 0.toByte() // Prevent leaks
    }

    fun removeFirst(): Byte {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = first
        nullify(_start)
        _start = (_start + 1) % capacity;
        _size--
        return out
    }

    fun removeLast(): Byte {
        if (_size <= 0) throw IndexOutOfBoundsException()
        val out = last
        nullify(internalIndex(size - 1))
        _size--
        return out
    }

    fun removeAt(index: Int): Byte {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // @TODO: We could use two arraycopy per branch to prevent umodding twice per element.
        val old = this[index]
        if (index < size / 2) {
            for (n in index downTo 1) this[n] = this[n - 1]
            _start = (_start + 1) umod capacity
        } else {
            for (n in index until size - 1) this[n] = this[n + 1]
        }

        _size--
        return old
    }

    override fun add(element: Byte): Boolean = true.apply { addLast(element) }
    override fun clear() { _size = 0 }
    override fun remove(element: Byte): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Byte>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Byte>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Byte>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val _temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                _temp[tsize++] = c
            }
        }
        this.data = _temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Byte get() = data[_start]
    val last: Byte get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Byte) { data[internalIndex(index)] = value }
    operator fun get(index: Int): Byte = data[internalIndex(index)]

    fun getOrNull(index: Int): Byte? = if (index in indices) get(index) else null

    override fun contains(element: Byte): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Byte): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Byte>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Byte> {
        val that = this
        return object : MutableIterator<Byte> {
            var index = 0
            override fun next(): Byte = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() { removeAt(--index) }
        }
    }

    override fun hashCode(): Int = contentHashCode(size) { this[it] }

    override fun equals(other: Any?): Boolean {
        if (other !is ByteDeque) return false
        if (other.size != this.size) return false
        for (n in 0 until size) if (this[n] != other[n]) return false
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (n in 0 until size) {
            sb.append(this[n])
            if (n != size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}

