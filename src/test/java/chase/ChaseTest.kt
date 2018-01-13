package chase

import formula.R
import formula.c
import formula.f
import formula.g
import org.junit.Test
import kotlin.test.assertEquals

class ChaseTest {
    @Test
    fun witnessConst() {
        assertEquals(_c, WitnessConst(c))
        assertEquals("'c", _c.toString())
    }

    @Test
    fun element() {
        assertEquals("e#0", e_0.toString())
        assertEquals(true, e_0 == e_0)
        assertEquals(false, e_0 == e_1)
        assertEquals(true, e_0 < e_1)
        assertEquals(false, e_0 > e_1)
        assertEquals(false, e_1 > e_1)
        assertEquals(false, e_1 < e_1)
        assertEquals(true, Element(1) == Element(1))
        assertEquals(0, e_0.hashCode())
        run {
            val e = Element(0)
            e.collapse(e_1)
            assertEquals(e_1, e)
            assertEquals(Element(1), e_1)
        }
    }

    @Test
    fun witnessApp() {
        assertEquals("f[]", WitnessApp(f).toString())
        assertEquals("f['c]", WitnessApp(f, listOf(_c)).toString())
        assertEquals("f[g[]]", WitnessApp(f, listOf(WitnessApp(g))).toString())
        assertEquals("f['c, g['d]]", WitnessApp(f, listOf(_c, WitnessApp(g, listOf(_d)))).toString())
    }

    @Test
    fun rel() {
        assertEquals(_R, Rel(R))
        assertEquals("R", _R.toString())
    }

    @Test
    fun obs() {
        assertEquals("<R()>", _R().toString())
        assertEquals("<R(e#0)>", _R(e_0).toString())
        assertEquals("<R(e#0, e#1, e#2)>", _R(e_0, e_1, e_2).toString())
        assertEquals("<e#0 = e#1>", (e_0 equals e_1).toString())
    }
}