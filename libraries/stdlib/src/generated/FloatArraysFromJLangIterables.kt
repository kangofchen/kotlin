// NOTE this file is auto-generated from src/kotlin/JLangIterables.kt
package kotlin

import kotlin.util.*

import java.util.*

/**
 * Returns *true* if all elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt all
 */
public inline fun FloatArray.all(predicate: (Float) -> Boolean) : Boolean {
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns *true* if any elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt any
 */
public inline fun FloatArray.any(predicate: (Float) -> Boolean) : Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Appends the string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied
 *
 * If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
 * a special *truncated* separator (which defaults to "..."
 *
 * @includeFunctionBody ../../test/CollectionTest.kt appendString
 */
public inline fun FloatArray.appendString(buffer: Appendable, separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): Unit {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            val text = if (element == null) "null" else element.toString()
            buffer.append(text)
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
}

/**
 * Returns the number of elements which match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt count
 */
public inline fun FloatArray.count(predicate: (Float) -> Boolean) : Int {
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

/**
 * Returns the first element which matches the given *predicate* or *null* if none matched
 *
 * @includeFunctionBody ../../test/CollectionTest.kt find
 */
public inline fun FloatArray.find(predicate: (Float) -> Boolean) : Float? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Filters all elements which match the given predicate into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterIntoLinkedList
 */
public inline fun <C: Collection<Float>> FloatArray.filterTo(result: C, predicate: (Float) -> Boolean) : C {
    for (element in this) if (predicate(element)) result.add(element)
    return result
}

/**
 * Returns a list containing all elements which do not match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotIntoLinkedList
 */
public inline fun <L: List<Float>> FloatArray.filterNotTo(result: L, predicate: (Float) -> Boolean) : L {
    for (element in this) if (!predicate(element)) result.add(element)
    return result
}

/**
 * Filters all non-*null* elements into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotNullIntoLinkedList
 */
public inline fun <L: List<Float>> FloatArray?.filterNotNullTo(result: L) : L {
    if (this != null) {
        for (element in this) if (element != null) result.add(element)
    }
    return result
}

/**
 * Returns the result of transforming each element to one or more values which are concatenated together into a single list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt flatMap
 */
public inline fun <R> FloatArray.flatMapTo(result: Collection<R>, transform: (Float) -> Collection<R>) : Collection<R> {
    for (element in this) {
        val list = transform(element)
        if (list != null) {
            for (r in list) result.add(r)
        }
    }
    return result
}

/**
 * Performs the given *operation* on each element
 *
 * @includeFunctionBody ../../test/CollectionTest.kt forEach
 */
public inline fun FloatArray.forEach(operation: (Float) -> Unit) : Unit = for (element in this) operation(element)

/**
 * Folds all elements from from left to right with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt fold
 */
public inline fun FloatArray.fold(initial: Float, operation: (Float, Float) -> Float): Float {
    var answer = initial
    for (element in this) answer = operation(answer, element)
    return answer
}

/**
 * Folds all elements from right to left with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt foldRight
 */
public inline fun FloatArray.foldRight(initial: Float, operation: (Float, Float) -> Float): Float = reverse().fold(initial, operation)

/**
 * Groups the elements in the collection into a new [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> FloatArray.groupBy(toKey: (Float) -> K) : Map<K, List<Float>> = groupByTo<K>(HashMap<K, List<Float>>(), toKey)

/**
 * Groups the elements in the collection into the given [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> FloatArray.groupByTo(result: Map<K, List<Float>>, toKey: (Float) -> K) : Map<K, List<Float>> {
    for (element in this) {
        val key = toKey(element)
        val list = result.getOrPut(key) { ArrayList<Float>() }
        list.add(element)
    }
    return result
}

/**
 * Creates a string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied.
 *
 * If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
 * a special *truncated* separator (which defaults to "..."
 *
 * @includeFunctionBody ../../test/CollectionTest.kt makeString
 */
public inline fun FloatArray.makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
    val buffer = StringBuilder()
    appendString(buffer, separator, prefix, postfix, limit, truncated)
    return buffer.toString().sure()
}

/** Returns a list containing the everything but the first elements that satisfy the given *predicate* */
public inline fun <L: List<Float>> FloatArray.dropWhileTo(result: L, predicate: (Float) -> Boolean) : L {
    var start = true
    for (element in this) {
        if (start && predicate(element)) {
            // ignore
        } else {
            start = false
            result.add(element)
        }
    }
    return result
}

/** Returns a list containing the first elements that satisfy the given *predicate* */
public inline fun <L: List<Float>> FloatArray.takeWhileTo(result: L, predicate: (Float) -> Boolean) : L {
    for (element in this) if (predicate(element)) result.add(element) else break
    return result
}

/**
 * Reverses the order the elements into a list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt reverse
 */
public inline fun FloatArray.reverse() : List<Float> {
    val answer = LinkedList<Float>()
    for (element in this) answer.addFirst(element)
    return answer
}

/** Copies all elements into the given collection */
public inline fun <C: Collection<Float>> FloatArray.toCollection(result: C) : C {
    for (element in this) result.add(element)
    return result
}

/** Copies all elements into a [[LinkedList]]  */
public inline fun  FloatArray.toLinkedList() : LinkedList<Float> = toCollection(LinkedList<Float>())

/** Copies all elements into a [[List]] */
public inline fun  FloatArray.toList() : List<Float> = toCollection(ArrayList<Float>())

/** Copies all elements into a [[List] */
public inline fun  FloatArray.toCollection() : Collection<Float> = toCollection(ArrayList<Float>())

/** Copies all elements into a [[Set]] */
public inline fun  FloatArray.toSet() : Set<Float> = toCollection(HashSet<Float>())

/** Copies all elements into a [[SortedSet]] */
public inline fun  FloatArray.toSortedSet() : SortedSet<Float> = toCollection(TreeSet<Float>())

/**
  TODO figure out necessary variance/generics ninja stuff... :)
public inline fun  FloatArray.toSortedList(transform: fun(Float) : java.lang.Comparable<*>) : List<Float> {
    val answer = this.toList()
    answer.sort(transform)
    return answer
}
*/
