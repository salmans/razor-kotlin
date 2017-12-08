package chase

import formula.R
import formula.c
import formula.f
import org.junit.Test
import kotlin.test.assertEquals

class ModelTest {
    @Test
    fun witnessFunc() {
        assertEquals(_f, WitnessFunc(f))
    }

    @Test
    fun witnessConst() {
        assertEquals(_c, WitnessConst(c))
    }

    @Test
    fun toStringWitnessFunc() {
        assertEquals("f", _f.toString())
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
        assertEquals("f[]", _f().toString())
        assertEquals("f['c]", _f(_c).toString())
        assertEquals("f[g[]]", _f(_g()).toString())
        assertEquals("f['c, g['d]]", _f(_c, _g(_d)).toString())
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
        assertEquals("R<>", _R().toString())
        assertEquals("R<e#0>", _R(e_0).toString())
        assertEquals("R<e#0, e#1, e#2>", _R(e_0, e_1, e_2).toString())
    }
}