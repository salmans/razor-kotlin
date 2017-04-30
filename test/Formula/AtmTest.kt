package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class AtmTest {
    @Test
    fun print() {
        assertEquals("R()", R().print())
        assertEquals("R(x, y)", R(x,y).print())
        assertEquals("R(g(x, y))", R(g(x,y)).print())
        assertEquals("R(f(f(f(f(f(f(x)))))))", R(f(f(f(f(f(f(x))))))).print())
    }
}