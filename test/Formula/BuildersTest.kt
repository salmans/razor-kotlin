package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class BuildersTest {
    @Test
    fun func_invoke() {
        assertEquals(App(f), f())
        assertEquals(App(f, listOf(x)), f(x))
        assertEquals(App(f, listOf(x, y)), f(x, y))
        assertEquals(App(f, listOf(x, App(g, listOf(y)))), f(x, g(y)))
    }

    @Test
    fun rel_invoke() {
        assertEquals(Atom(R), R())
        assertEquals(Atom(R, listOf(x)), R(x))
        assertEquals(Atom(R, listOf(x, y)), R(x, y))
        assertEquals(Atom(R, listOf(x, App(g, listOf(y)))), R(x, g(y)))
    }

    @Test
    fun top() {
        assertEquals(Top, TRUE)
    }

    @Test
    fun bottom() {
        assertEquals(Bottom, FALSE)
    }

    @Test
    fun not() {
        assertEquals(Not(R()), !R())
    }

    @Test
    fun and() {
        assertEquals(And(R(), Q()), R() and Q())
    }

    @Test
    fun or() {
        assertEquals(Or(R(), Q()), R() or Q())
    }

    @Test
    fun implies() {
        assertEquals(Implies(R(), Q()), R() implies Q())
    }

    @Test
    fun equals() {
        assertEquals(Equals(x, y), x equals y)
        assertEquals(Equals(g(x, x), f(y)), g(x, x) equals f(y))
    }

    @Test
    fun exists() {
        assertEquals(Exists(listOf(x), P(x)), exists(x){P(x)})
        assertEquals(Exists(listOf(x, y), P(x, y)), exists(x, y){P(x, y)})
    }

    @Test
    fun forall() {
        assertEquals(Forall(listOf(x), P(x)), forall(x){P(x)})
        assertEquals(Forall(listOf(x, y), P(x, y)), forall(x, y){P(x, y)})
    }
}