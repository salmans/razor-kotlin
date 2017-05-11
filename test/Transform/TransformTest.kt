package Transform

import Formula.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class TransformTest {
    @Test
    fun termRenameVar() {
        assertEquals(x, x.renameVar { it })
        assertEquals(y, x.renameVar {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(x, x.renameVar {
            when (it) {
                y -> z
                else -> it
            }
        })
        assertEquals(f(y), f(x).renameVar {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(f(x), f(x).renameVar {
            when (it) {
                z -> y
                else -> it
            }
        })
        assertEquals(f(z, z), f(x, y).renameVar {
            when (it) {
                x -> z
                y -> z
                else -> it
            }
        })
        assertEquals(f(y, g(y, h(z))), f(x, g(x, h(y))).renameVar {
            when (it) {
                x -> y
                y -> z
                else -> it
            }
        })
    }

    @Test
    fun termRenameFormula() {
        assertEquals(TRUE, TRUE.renameVar {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(FALSE, FALSE.renameVar {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(z equals z, (x equals y).renameVar {
            when (it) {
                x -> z
                y -> z
                else -> it
            }
        })
        assertEquals(P(x), P(x).renameVar {
            when (it) {
                x -> x
                else -> it
            }
        })
        assertEquals(P(y), P(x).renameVar {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(P(y, z, y), P(x, y, x).renameVar {
            when (it) {
                x -> y
                y -> z
                else -> it
            }
        })
        assertEquals(!P(y, z, y), !P(x, y, x).renameVar {
            when (it) {
                x -> y
                y -> z
                else -> it
            }
        })
        assertEquals(P(z) and Q(z), (P(x) and Q(y)).renameVar {
            when (it) {
                x -> z
                y -> z
                else -> it
            }
        })
        assertEquals(P(z) or Q(z), (P(x) or Q(y)).renameVar {
            when (it) {
                x -> z
                y -> z
                else -> it
            }
        })
        assertEquals(P(z) implies Q(z), (P(x) implies Q(y)).renameVar {
            when (it) {
                x -> z
                y -> z
                else -> it
            }
        })
        assertEquals(exists(y, y) { P(y, y, y) }, (exists(x, y) { P(x, y, z) }).renameVar {
            when (it) {
                x -> y
                z -> y
                else -> it
            }
        })
        assertEquals(forall(y, y) { P(y, y, y) }, (forall(x, y) { P(x, y, z) }).renameVar {
            when (it) {
                x -> y
                z -> y
                else -> it
            }
        })
        assertEquals(exists(y) { forall(z) { P(y) or Q(z) and R(z) } and !(z equals z) },
                (exists(x) { forall(y) { P(x) or Q(y) and R(z) } and !(y equals z) }).renameVar {
                    when (it) {
                        x -> y
                        y -> z
                        else -> it
                    }
                })

    }

    @Test
    fun substituteTerm() {
        assertEquals(x, x.substitute { it })
        assertEquals(y, x.substitute {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(f(z), x.substitute {
            when (it) {
                x -> f(z)
                else -> it
            }
        })
        assertEquals(x, x.substitute {
            when (it) {
                y -> z
                else -> it
            }
        })
        assertEquals(f(y), f(x).substitute {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(f(g(h(y, z))), f(x).substitute {
            when (it) {
                x -> g(h(y, z))
                else -> it
            }
        })
        assertEquals(f(x), f(x).substitute {
            when (it) {
                z -> y
                else -> it
            }
        })
        assertEquals(f(g(z), h(z, y)), f(x, y).substitute {
            when (it) {
                x -> g(z)
                y -> h(z, y)
                else -> it
            }
        })
        assertEquals(f(f(f()), g(f(f()), h(z))), f(x, g(x, h(y))).substitute {
            when (it) {
                x -> f(f())
                y -> z
                else -> it
            }
        })
    }

    @Test
    fun substituteFormula() {
        assertEquals(TRUE, TRUE.substitute {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(FALSE, FALSE.substitute {
            when (it) {
                x -> y
                else -> it
            }
        })
        assertEquals(f(g(z)) equals g(f(z)), (x equals y).substitute {
            when (it) {
                x -> f(g(z))
                y -> g(f(z))
                else -> it
            }
        })
        assertEquals(P(h(y)), P(x).substitute {
            when (it) {
                x -> h(y)
                else -> it
            }
        })
        assertEquals(P((g(g(x)))), P(x).substitute {
            when (it) {
                x -> g(g(x))
                else -> it
            }
        })
        assertEquals(P(y, f(z), y), P(x, y, x).substitute {
            when (it) {
                x -> y
                y -> f(z)
                else -> it
            }
        })
        assertEquals(!P(h(), z, h()), !P(x, y, x).substitute {
            when (it) {
                x -> h()
                y -> z
                else -> it
            }
        })
        assertEquals(P(f(g())) and Q(h(z)), (P(x) and Q(y)).substitute {
            when (it) {
                x -> f(g())
                y -> h(z)
                else -> it
            }
        })
        assertEquals(P(f(g())) or Q(h(z)), (P(x) or Q(y)).substitute {
            when (it) {
                x -> f(g())
                y -> h(z)
                else -> it
            }
        })
        assertEquals(P(f()) implies Q(g()), (P(x) implies Q(y)).substitute {
            when (it) {
                x -> f()
                y -> g()
                else -> it
            }
        })
        assertEquals(exists(x, y) { P(f(g(y)), y, y) }, (exists(x, y) { P(x, y, z) }).substitute {
            when (it) {
                x -> f(g(y))
                z -> y
                else -> it
            }
        })
        assertEquals(forall(x, y) { P(f(g(y)), y, y) }, (forall(x, y) { P(x, y, z) }).substitute {
            when (it) {
                x -> f(g(y))
                z -> y
                else -> it
            }
        })
        assertEquals(exists(x) { forall(y) { P(y) or Q(z) and R(z) } and !(z equals z) },
                (exists(x) { forall(y) { P(x) or Q(y) and R(z) } and !(y equals z) }).substitute {
                    when (it) {
                        x -> y
                        y -> z
                        else -> it
                    }
                })
    }
}