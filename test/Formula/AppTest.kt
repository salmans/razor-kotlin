package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class AppTest {
    @Test
    fun print() {
        assertEquals("f()", f().print())
        assertEquals("f(x, y)", f(x,y).print())
        assertEquals("f(g(x, y))", f(g(x,y)).print())
        assertEquals("f(f(f(f(f(f(f(x)))))))", f(f(f(f(f(f(f(x))))))).print())
    }
}