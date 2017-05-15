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
        assertEquals("Internal Error: Invalid Term", try {
            INVALID_TERM.renameVar { it }
        } catch (e: Exception) {
            e.message
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

        assertEquals("Internal Error: Invalid Formula", try {
            INVALID_FORMULA.renameVar { it }
        } catch (e: Exception) {
            e.message
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
        assertEquals("Internal Error: Invalid Term", try {
            INVALID_TERM.substitute { it }
        } catch (e: Exception) {
            e.message
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
        assertEquals("Internal Error: Invalid Formula", try {
            INVALID_FORMULA.substitute { it }
        } catch (e: Exception) {
            e.message
        })
    }

    @Test
    fun prenex() {
        assertEquals(TRUE, TRUE.prenex())
        assertEquals(FALSE, FALSE.prenex())
        assertEquals(P(x), P(x).prenex())
        assertEquals(x equals y, (x equals y).prenex())
        assertEquals(!P(x), (!P(x)).prenex())
        assertEquals(P(x) and Q(y), (P(x) and Q(y)).prenex())
        assertEquals(P(x) or Q(y), (P(x) or Q(y)).prenex())
        assertEquals(P(x) implies Q(y), (P(x) implies Q(y)).prenex())
        assertEquals(exists(x) { P(x) and !Q(y) or R(z) }, exists(x) { P(x) and !Q(y) or R(z) }.prenex())
        assertEquals(forall(x) { P(x) and !Q(y) or R(z) }, forall(x) { P(x) and !Q(y) or R(z) }.prenex())

        assertEquals(forall(x) { !P(x) }, (!exists(x) { P(x) }).prenex())
        assertEquals(forall(x) { P(x) and Q(y) }, (forall(x) { P(x) } and Q(y)).prenex())
        assertEquals(exists(x) { P(x) and Q(y) }, (exists(x) { P(x) } and Q(y)).prenex())
        assertEquals(forall(x_1) { P(x_1) and Q(x) }, (forall(x) { P(x) } and Q(x)).prenex())
        assertEquals(exists(x_1) { P(x_1) and Q(x) }, (exists(x) { P(x) } and Q(x)).prenex())
        assertEquals(exists(x_1, y_1) { P(x_1, y_1) and Q(x, y) }, (exists(x, y) { P(x, y) } and Q(x, y)).prenex())
        assertEquals(forall(x) { Q(y) and P(x) }, (Q(y) and forall(x) { P(x) }).prenex())
        assertEquals(exists(x) { Q(y) and P(x) }, (Q(y) and exists(x) { P(x) }).prenex())
        assertEquals(forall(x_1) { Q(x) and P(x_1) }, (Q(x) and forall(x) { P(x) }).prenex())
        assertEquals(exists(x_1) { Q(x) and P(x_1) }, (Q(x) and exists(x) { P(x) }).prenex())
        assertEquals(exists(x_1, y_1) { Q(x, y) and P(x_1, y_1) }, (Q(x, y) and exists(x, y) { P(x, y) }).prenex())
        assertEquals(forall(x) { P(x) or Q(y) }, (forall(x) { P(x) } or Q(y)).prenex())
        assertEquals(exists(x) { P(x) or Q(y) }, (exists(x) { P(x) } or Q(y)).prenex())
        assertEquals(forall(x_1) { P(x_1) or Q(x) }, (forall(x) { P(x) } or Q(x)).prenex())
        assertEquals(exists(x_1) { P(x_1) or Q(x) }, (exists(x) { P(x) } or Q(x)).prenex())
        assertEquals(exists(x_1, y_1) { P(x_1, y_1) or Q(x, y) }, (exists(x, y) { P(x, y) } or Q(x, y)).prenex())
        assertEquals(forall(x) { Q(y) or P(x) }, (Q(y) or forall(x) { P(x) }).prenex())
        assertEquals(exists(x) { Q(y) or P(x) }, (Q(y) or exists(x) { P(x) }).prenex())
        assertEquals(forall(x_1) { Q(x) or P(x_1) }, (Q(x) or forall(x) { P(x) }).prenex())
        assertEquals(exists(x_1) { Q(x) or P(x_1) }, (Q(x) or exists(x) { P(x) }).prenex())
        assertEquals(exists(x_1, y_1) { Q(x, y) or P(x_1, y_1) }, (Q(x, y) or exists(x, y) { P(x, y) }).prenex())
        assertEquals(exists(x) { P(x) implies Q(y) }, (forall(x) { P(x) } implies Q(y)).prenex())
        assertEquals(forall(x) { P(x) implies Q(y) }, (exists(x) { P(x) } implies Q(y)).prenex())
        assertEquals(exists(x_1) { P(x_1) implies Q(x) }, (forall(x) { P(x) } implies Q(x)).prenex())
        assertEquals(forall(x_1) { P(x_1) implies Q(x) }, (exists(x) { P(x) } implies Q(x)).prenex())
        assertEquals(forall(x_1, y_1) { P(x_1, y_1) implies Q(x, y) }, (exists(x, y) { P(x, y) } implies Q(x, y)).prenex())
        assertEquals(forall(x) { Q(y) implies P(x) }, (Q(y) implies forall(x) { P(x) }).prenex())
        assertEquals(exists(x) { Q(y) implies P(x) }, (Q(y) implies exists(x) { P(x) }).prenex())
        assertEquals(forall(x_1) { Q(x) implies P(x_1) }, (Q(x) implies forall(x) { P(x) }).prenex())
        assertEquals(exists(x_1) { Q(x) implies P(x_1) }, (Q(x) implies exists(x) { P(x) }).prenex())
        assertEquals(exists(x_1, y_1) { Q(x, y) implies P(x_1, y_1) }, (Q(x, y) implies exists(x, y) { P(x, y) }).prenex())
//        //renaming tests
        assertEquals(forall(x_2, x_1) { P(x_2) and Q(x) }, (forall(x, x_1) { P(x) } and Q(x)).prenex())
        assertEquals(exists(x_2, x_1) { P(x_2) and Q(x) }, (exists(x, x_1) { P(x) } and Q(x)).prenex())
        assertEquals(exists(x_2) { P(x_2) and Q(x, x_1) }, (exists(x) { P(x) } and Q(x, x_1)).prenex())
        assertEquals(exists(x_2) { P(x_2, x_1) and Q(x) }, (exists(x) { P(x, x_1) } and Q(x)).prenex())
        assertEquals(forall(x_2, x_1) { Q(x) and P(x_2) }, (Q(x) and forall(x, x_1) { P(x) }).prenex())
        assertEquals(exists(x_2, x_1) { Q(x) and P(x_2) }, (Q(x) and exists(x, x_1) { P(x) }).prenex())
        assertEquals(exists(x_2) { Q(x, x_1) and P(x_2) }, (Q(x, x_1) and exists(x) { P(x) }).prenex())
        assertEquals(exists(x_2) { Q(x) and P(x_2, x_1) }, (Q(x) and exists(x) { P(x, x_1) }).prenex())
        assertEquals(forall(x_2, x_1) { P(x_2) or Q(x) }, (forall(x, x_1) { P(x) } or Q(x)).prenex())
        assertEquals(exists(x_2, x_1) { P(x_2) or Q(x) }, (exists(x, x_1) { P(x) } or Q(x)).prenex())
        assertEquals(exists(x_2) { P(x_2) or Q(x, x_1) }, (exists(x) { P(x) } or Q(x, x_1)).prenex())
        assertEquals(exists(x_2) { P(x_2, x_1) or Q(x) }, (exists(x) { P(x, x_1) } or Q(x)).prenex())
        assertEquals(forall(x_2, x_1) { Q(x) or P(x_2) }, (Q(x) or forall(x, x_1) { P(x) }).prenex())
        assertEquals(exists(x_2, x_1) { Q(x) or P(x_2) }, (Q(x) or exists(x, x_1) { P(x) }).prenex())
        assertEquals(exists(x_2) { Q(x, x_1) or P(x_2) }, (Q(x, x_1) or exists(x) { P(x) }).prenex())
        assertEquals(exists(x_2) { Q(x) or P(x_2, x_1) }, (Q(x) or exists(x) { P(x, x_1) }).prenex())
        assertEquals(exists(x_2, x_1) { P(x_2) implies Q(x) }, (forall(x, x_1) { P(x) } implies Q(x)).prenex())
        assertEquals(forall(x_2, x_1) { P(x_2) implies Q(x) }, (exists(x, x_1) { P(x) } implies Q(x)).prenex())
        assertEquals(forall(x_2) { P(x_2) implies Q(x, x_1) }, (exists(x) { P(x) } implies Q(x, x_1)).prenex())
        assertEquals(forall(x_2) { P(x_2, x_1) implies Q(x) }, (exists(x) { P(x, x_1) } implies Q(x)).prenex())
        assertEquals(forall(x_2, x_1) { Q(x) implies P(x_2) }, (Q(x) implies forall(x, x_1) { P(x) }).prenex())
        assertEquals(exists(x_2, x_1) { Q(x) implies P(x_2) }, (Q(x) implies exists(x, x_1) { P(x) }).prenex())
        assertEquals(exists(x_2) { Q(x, x_1) implies P(x_2) }, (Q(x, x_1) implies exists(x) { P(x) }).prenex())
        assertEquals(exists(x_2) { Q(x) implies P(x_2, x_1) }, (Q(x) implies exists(x) { P(x, x_1) }).prenex())

        assertEquals(forall(z) { forall(x) { (P() or Q(x)) implies R(z) } }, ((P() or exists(x) { Q(x) }) implies forall(z) { R(z) }).prenex())
        //assertEquals("", ((P() or exists(y) { Q(y) }) implies forall(z) { R(z) }).prenex().print())

        assertEquals("Internal Error: Invalid Formula", try {
            INVALID_FORMULA.prenex()
        } catch (e: Exception) {
            e.message
        })
    }
}