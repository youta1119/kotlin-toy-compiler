/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.reflect


/**
 * Represents a property, such as a named `val` or `var` declaration.
 * Instances of this class are obtainable by the `::` operator.
 *
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/reflection.html)
 * for more information.
 *
 * @param R the type of the property.
 */
public interface KProperty<out R> : KCallable<R>

public interface KProperty0<out R> : kotlin.reflect.KProperty<R>, () -> R {

    public fun get(): R

    public override abstract operator fun invoke(): R
}

public interface KProperty1<T, out R> : kotlin.reflect.KProperty<R>, (T) -> R {
    public fun get(receiver: T): R

    public override operator fun invoke(p1: T): R
}

public interface KProperty2<T1, T2, out R> : kotlin.reflect.KProperty<R>, (T1, T2) -> R {
    public fun get(receiver1: T1, receiver2: T2): R

    public override operator fun invoke(p1: T1, p2: T2): R
}

/**
 * Represents a property declared as a `var`.
 */
public interface KMutableProperty<R> : KProperty<R>

public interface KMutableProperty0<R> : KProperty0<R>, KMutableProperty<R> {
    public fun set(value: R)
}

public interface KMutableProperty1<T, R> : KProperty1<T, R>, KMutableProperty<R> {
    public fun set(receiver: T, value: R)
}

public interface KMutableProperty2<T1, T2, R> : KProperty2<T1, T2, R>, KMutableProperty<R> {
    public fun set(receiver1: T1, receiver2: T2, value: R)
}