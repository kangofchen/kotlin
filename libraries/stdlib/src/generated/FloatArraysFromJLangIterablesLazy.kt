// NOTE this file is auto-generated from src/kotlin/JLangIterablesLazy.kt
package kotlin

import kotlin.util.*

import java.util.ArrayList
import java.util.Collection
import java.util.List

//
// This file contains methods which could have a lazy implementation for things like
// Iterator<Float> or java.util.Iterator<Float>
//
// See [[GenerateStandardLib.kt]] for more details
//

/**
 * Returns a list containing all elements which match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filter
 */
public inline fun FloatArray.filter(predicate: (Float) -> Boolean) : List<Float> = filterTo(ArrayList<Float>(), predicate)

/**
 * Returns a list containing all elements which do not match the given predicate
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNot
 */
public inline fun FloatArray.filterNot(predicate: (Float)-> Boolean) : List<Float> = filterNotTo(ArrayList<Float>(), predicate)

/**
 * Returns a list containing all the non-*null* elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotNull
 */
public inline fun FloatArray?.filterNotNull() : List<Float> = filterNotNullTo<ArrayList<Float>>(java.util.ArrayList<Float>())

/**
 * Returns the result of transforming each element to one or more values which are concatenated together into a single collection
 *
 * @includeFunctionBody ../../test/CollectionTest.kt flatMap
 */
public inline fun <R> FloatArray.flatMap(transform: (Float)-> Collection<R>) : Collection<R> = flatMapTo(ArrayList<R>(), transform)

/**
 * Creates a copy of this collection as a [[List]] with the element added at the end
 *
 * @includeFunctionBody ../../test/CollectionTest.kt plus
 */
public inline fun  FloatArray.plus(element: Float): List<Float> {
    val list = toCollection(ArrayList<Float>())
    list.add(element)
    return list
}


/**
 * Creates a copy of this collection as a [[List]] with all the elements added at the end
 *
 * @includeFunctionBody ../../test/CollectionTest.kt plusCollection
 */
public inline fun  FloatArray.plus(elements: FloatArray): List<Float> {
    val list = toCollection(ArrayList<Float>())
    list.addAll(elements.toCollection())
    return list
}

/**
 * Returns a list containing all the non-*null* elements, throwing an [[IllegalArgumentException]] if there are any null elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt requireNoNulls
 */
public inline fun  FloatArray?.requireNoNulls() : List<Float> {
    val list = ArrayList<Float>()
    for (element in this) {
        if (element == null) {
            throw IllegalArgumentException("null element found in $this")
        } else {
            list.add(element)
        }
    }
    return list
}

/**
 * Returns a list containing everything but the first *n* elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt drop
 */
public inline fun FloatArray.drop(n: Int): List<Float> {
    fun countTo(n: Int): (Float) -> Boolean {
      var count = 0
      return { ++count; count <= n }
    }
    return dropWhile(countTo(n))
}

/**
 * Returns a list containing the everything but the first elements that satisfy the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt dropWhile
 */
public inline fun FloatArray.dropWhile(predicate: (Float) -> Boolean): List<Float> = dropWhileTo(ArrayList<Float>(), predicate)

/**
 * Returns a list containing the first *n* elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt take
 */
public inline fun FloatArray.take(n: Int): List<Float> {
    fun countTo(n: Int): (Float) -> Boolean {
      var count = 0
      return { ++count; count <= n }
    }
    return takeWhile(countTo(n))
}

/**
 * Returns a list containing the first elements that satisfy the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt takeWhile
 */
public inline fun FloatArray.takeWhile(predicate: (Float) -> Boolean): List<Float> = takeWhileTo(ArrayList<Float>(), predicate)
