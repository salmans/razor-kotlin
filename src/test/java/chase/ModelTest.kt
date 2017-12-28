package chase

import formula.R
import formula.c
import formula.f
import formula.g
import org.junit.Test
import kotlin.test.assertEquals

class ModelTest {
    @Test
    fun witnessConst() {
        assertEquals(_c, WitnessConst(c))
    }

    @Test
    fun toStringWitnessConst() {
        assertEquals("'c", _c.toString())
    }

    @Test
    fun toStringElement() {
        assertEquals("e#0", e_0.toString())
    }

    @Test
    fun toStringWitnessApp() {
        assertEquals("f[]", WitnessApp(f).toString())
        assertEquals("f['c]", WitnessApp(f, listOf(_c)).toString())
        assertEquals("f[g[]]", WitnessApp(f, listOf(WitnessApp(g))).toString())
        assertEquals("f['c, g['d]]", WitnessApp(f, listOf(_c, WitnessApp(g, listOf(_d)))).toString())
    }

    @Test
    fun rel() {
        assertEquals(_R, Rel(R))
    }

    @Test
    fun toStringRel() {
        assertEquals("R", _R.toString())
    }

    @Test
    fun toStringObservation() {
        assertEquals("<R()>", _R().toString())
        assertEquals("<R(e#0)>", _R(e_0).toString())
        assertEquals("<R(e#0, e#1, e#2)>", _R(e_0, e_1, e_2).toString())
        assertEquals("<e#0 = e#1>", (e_0 equals e_1).toString())
    }
}