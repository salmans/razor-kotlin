package Transform

import Formula.*
import org.junit.Assert.assertEquals
import org.junit.Test


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
        assertEquals(INVALID_TERM, try {
            BAD_TERM.renameVar { it }
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

        assertEquals(INVALID_FORMULA, try {
            BAD_FORMULA.renameVar { it }
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
        assertEquals(INVALID_TERM, try {
            BAD_TERM.substitute { it }
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
        assertEquals(INVALID_FORMULA, try {
            BAD_FORMULA.substitute { it }
        } catch (e: Exception) {
            e.message
        })
    }

    @Test
    fun pnf() {
        assertEquals(TRUE, TRUE.pnf())
        assertEquals(FALSE, FALSE.pnf())
        assertEquals(P(x), P(x).pnf())
        assertEquals(x equals y, (x equals y).pnf())
        assertEquals(!P(x), (!P(x)).pnf())
        assertEquals(P(x) and Q(y), (P(x) and Q(y)).pnf())
        assertEquals(P(x) or Q(y), (P(x) or Q(y)).pnf())
        assertEquals(P(x) implies Q(y), (P(x) implies Q(y)).pnf())
        assertEquals(exists(x) { P(x) and !Q(y) or R(z) }, exists(x) { P(x) and !Q(y) or R(z) }.pnf())
        assertEquals(forall(x) { P(x) and !Q(y) or R(z) }, forall(x) { P(x) and !Q(y) or R(z) }.pnf())
        // sanity checking
        assertEquals(forall(x) { !P(x) }, (!exists(x) { P(x) }).pnf())
        assertEquals(forall(x) { P(x) and Q(y) }, (forall(x) { P(x) } and Q(y)).pnf())
        assertEquals(exists(x) { P(x) and Q(y) }, (exists(x) { P(x) } and Q(y)).pnf())
        assertEquals(forall(x_1) { P(x_1) and Q(x) }, (forall(x) { P(x) } and Q(x)).pnf())
        assertEquals(exists(x_1) { P(x_1) and Q(x) }, (exists(x) { P(x) } and Q(x)).pnf())
        assertEquals(exists(x_1, y_1) { P(x_1, y_1) and Q(x, y) }, (exists(x, y) { P(x, y) } and Q(x, y)).pnf())
        assertEquals(forall(x) { Q(y) and P(x) }, (Q(y) and forall(x) { P(x) }).pnf())
        assertEquals(exists(x) { Q(y) and P(x) }, (Q(y) and exists(x) { P(x) }).pnf())
        assertEquals(forall(x_1) { Q(x) and P(x_1) }, (Q(x) and forall(x) { P(x) }).pnf())
        assertEquals(exists(x_1) { Q(x) and P(x_1) }, (Q(x) and exists(x) { P(x) }).pnf())
        assertEquals(exists(x_1, y_1) { Q(x, y) and P(x_1, y_1) }, (Q(x, y) and exists(x, y) { P(x, y) }).pnf())
        assertEquals(forall(x) { P(x) or Q(y) }, (forall(x) { P(x) } or Q(y)).pnf())
        assertEquals(exists(x) { P(x) or Q(y) }, (exists(x) { P(x) } or Q(y)).pnf())
        assertEquals(forall(x_1) { P(x_1) or Q(x) }, (forall(x) { P(x) } or Q(x)).pnf())
        assertEquals(exists(x_1) { P(x_1) or Q(x) }, (exists(x) { P(x) } or Q(x)).pnf())
        assertEquals(exists(x_1, y_1) { P(x_1, y_1) or Q(x, y) }, (exists(x, y) { P(x, y) } or Q(x, y)).pnf())
        assertEquals(forall(x) { Q(y) or P(x) }, (Q(y) or forall(x) { P(x) }).pnf())
        assertEquals(exists(x) { Q(y) or P(x) }, (Q(y) or exists(x) { P(x) }).pnf())
        assertEquals(forall(x_1) { Q(x) or P(x_1) }, (Q(x) or forall(x) { P(x) }).pnf())
        assertEquals(exists(x_1) { Q(x) or P(x_1) }, (Q(x) or exists(x) { P(x) }).pnf())
        assertEquals(exists(x_1, y_1) { Q(x, y) or P(x_1, y_1) }, (Q(x, y) or exists(x, y) { P(x, y) }).pnf())
        assertEquals(exists(x) { P(x) implies Q(y) }, (forall(x) { P(x) } implies Q(y)).pnf())
        assertEquals(forall(x) { P(x) implies Q(y) }, (exists(x) { P(x) } implies Q(y)).pnf())
        assertEquals(exists(x_1) { P(x_1) implies Q(x) }, (forall(x) { P(x) } implies Q(x)).pnf())
        assertEquals(forall(x_1) { P(x_1) implies Q(x) }, (exists(x) { P(x) } implies Q(x)).pnf())
        assertEquals(forall(x_1, y_1) { P(x_1, y_1) implies Q(x, y) }, (exists(x, y) { P(x, y) } implies Q(x, y)).pnf())
        assertEquals(forall(x) { Q(y) implies P(x) }, (Q(y) implies forall(x) { P(x) }).pnf())
        assertEquals(exists(x) { Q(y) implies P(x) }, (Q(y) implies exists(x) { P(x) }).pnf())
        assertEquals(forall(x_1) { Q(x) implies P(x_1) }, (Q(x) implies forall(x) { P(x) }).pnf())
        assertEquals(exists(x_1) { Q(x) implies P(x_1) }, (Q(x) implies exists(x) { P(x) }).pnf())
        assertEquals(exists(x_1, y_1) { Q(x, y) implies P(x_1, y_1) }, (Q(x, y) implies exists(x, y) { P(x, y) }).pnf())
        //renaming tests
        assertEquals(forall(x_2, x_1) { P(x_2) and Q(x) }, (forall(x, x_1) { P(x) } and Q(x)).pnf())
        assertEquals(exists(x_2, x_1) { P(x_2) and Q(x) }, (exists(x, x_1) { P(x) } and Q(x)).pnf())
        assertEquals(exists(x_2) { P(x_2) and Q(x, x_1) }, (exists(x) { P(x) } and Q(x, x_1)).pnf())
        assertEquals(exists(x_2) { P(x_2, x_1) and Q(x) }, (exists(x) { P(x, x_1) } and Q(x)).pnf())
        assertEquals(forall(x_2, x_1) { Q(x) and P(x_2) }, (Q(x) and forall(x, x_1) { P(x) }).pnf())
        assertEquals(exists(x_2, x_1) { Q(x) and P(x_2) }, (Q(x) and exists(x, x_1) { P(x) }).pnf())
        assertEquals(exists(x_2) { Q(x, x_1) and P(x_2) }, (Q(x, x_1) and exists(x) { P(x) }).pnf())
        assertEquals(exists(x_2) { Q(x) and P(x_2, x_1) }, (Q(x) and exists(x) { P(x, x_1) }).pnf())
        assertEquals(forall(x_2, x_1) { P(x_2) or Q(x) }, (forall(x, x_1) { P(x) } or Q(x)).pnf())
        assertEquals(exists(x_2, x_1) { P(x_2) or Q(x) }, (exists(x, x_1) { P(x) } or Q(x)).pnf())
        assertEquals(exists(x_2) { P(x_2) or Q(x, x_1) }, (exists(x) { P(x) } or Q(x, x_1)).pnf())
        assertEquals(exists(x_2) { P(x_2, x_1) or Q(x) }, (exists(x) { P(x, x_1) } or Q(x)).pnf())
        assertEquals(forall(x_2, x_1) { Q(x) or P(x_2) }, (Q(x) or forall(x, x_1) { P(x) }).pnf())
        assertEquals(exists(x_2, x_1) { Q(x) or P(x_2) }, (Q(x) or exists(x, x_1) { P(x) }).pnf())
        assertEquals(exists(x_2) { Q(x, x_1) or P(x_2) }, (Q(x, x_1) or exists(x) { P(x) }).pnf())
        assertEquals(exists(x_2) { Q(x) or P(x_2, x_1) }, (Q(x) or exists(x) { P(x, x_1) }).pnf())
        assertEquals(exists(x_2, x_1) { P(x_2) implies Q(x) }, (forall(x, x_1) { P(x) } implies Q(x)).pnf())
        assertEquals(forall(x_2, x_1) { P(x_2) implies Q(x) }, (exists(x, x_1) { P(x) } implies Q(x)).pnf())
        assertEquals(forall(x_2) { P(x_2) implies Q(x, x_1) }, (exists(x) { P(x) } implies Q(x, x_1)).pnf())
        assertEquals(forall(x_2) { P(x_2, x_1) implies Q(x) }, (exists(x) { P(x, x_1) } implies Q(x)).pnf())
        assertEquals(forall(x_2, x_1) { Q(x) implies P(x_2) }, (Q(x) implies forall(x, x_1) { P(x) }).pnf())
        assertEquals(exists(x_2, x_1) { Q(x) implies P(x_2) }, (Q(x) implies exists(x, x_1) { P(x) }).pnf())
        assertEquals(exists(x_2) { Q(x, x_1) implies P(x_2) }, (Q(x, x_1) implies exists(x) { P(x) }).pnf())
        assertEquals(exists(x_2) { Q(x) implies P(x_2, x_1) }, (Q(x) implies exists(x) { P(x, x_1) }).pnf())
        // both sides of binary formulas
        assertEquals(forall(x) { forall(x_1) { P(x) and Q(x_1) } }, (forall(x) { P(x) } and forall(x) { Q(x) }).pnf())
        assertEquals(forall(x) { exists(x_1) { P(x) and Q(x_1) } }, (forall(x) { P(x) } and exists(x) { Q(x) }).pnf())
        assertEquals(exists(x) { forall(x_1) { P(x) and Q(x_1) } }, (exists(x) { P(x) } and forall(x) { Q(x) }).pnf())
        assertEquals(exists(x) { exists(x_1) { P(x) and Q(x_1) } }, (exists(x) { P(x) } and exists(x) { Q(x) }).pnf())
        assertEquals(forall(x) { forall(x_1) { P(x) or Q(x_1) } }, (forall(x) { P(x) } or forall(x) { Q(x) }).pnf())
        assertEquals(forall(x) { exists(x_1) { P(x) or Q(x_1) } }, (forall(x) { P(x) } or exists(x) { Q(x) }).pnf())
        assertEquals(exists(x) { forall(x_1) { P(x) or Q(x_1) } }, (exists(x) { P(x) } or forall(x) { Q(x) }).pnf())
        assertEquals(exists(x) { exists(x_1) { P(x) or Q(x_1) } }, (exists(x) { P(x) } or exists(x) { Q(x) }).pnf())
        assertEquals(exists(x) { forall(x_1) { P(x) implies Q(x_1) } }, (forall(x) { P(x) } implies forall(x) { Q(x) }).pnf())
        assertEquals(exists(x) { exists(x_1) { P(x) implies Q(x_1) } }, (forall(x) { P(x) } implies exists(x) { Q(x) }).pnf())
        assertEquals(forall(x) { forall(x_1) { P(x) implies Q(x_1) } }, (exists(x) { P(x) } implies forall(x) { Q(x) }).pnf())
        assertEquals(forall(x) { exists(x_1) { P(x) implies Q(x_1) } }, (exists(x) { P(x) } implies exists(x) { Q(x) }).pnf())
        // multiple steps
        assertEquals(exists(x) { !!P(x) }, (!(!exists(x) { P(x) })).pnf())
        assertEquals(forall(x) { !!P(x) }, (!(!forall(x) { P(x) })).pnf())
        assertEquals(forall(x_1) { P(x) and (Q(x_1) and R(x)) }, (P(x) and (forall(x) { Q(x) } and R(x))).pnf())
        assertEquals(exists(x_1) { P(x) and (Q(x_1) and R(x)) }, (P(x) and (exists(x) { Q(x) } and R(x))).pnf())
        assertEquals(forall(x_1) { P(x) or (Q(x_1) or R(x)) }, (P(x) or (forall(x) { Q(x) } or R(x))).pnf())
        assertEquals(exists(x_1) { P(x) or (Q(x_1) or R(x)) }, (P(x) or (exists(x) { Q(x) } or R(x))).pnf())
        assertEquals(exists(x_1) { P(x) implies (Q(x_1) implies R(x)) }, (P(x) implies (forall(x) { Q(x) } implies R(x))).pnf())
        assertEquals(forall(x_1) { P(x) implies (Q(x_1) implies R(x)) }, (P(x) implies (exists(x) { Q(x) } implies R(x))).pnf())
        // random formulas
        assertEquals(forall(x) { exists(y) { P(x) implies (P(y) and Q(x, y)) } }, (forall(x) { P(x) implies exists(y) { P(y) and Q(x, y) } }).pnf())
        assertEquals(exists(x) { forall(y) { P(x) and (P(y) implies Q(x, y)) } }, exists(x) { P(x) and forall(y) { P(y) implies Q(x, y) } }.pnf())
        assertEquals(forall(x) { exists(y) { P(x) implies !(P(y) implies Q(x, y)) } }, forall(x) { P(x) implies !forall(y) { P(y) implies Q(x, y) } }.pnf())
        assertEquals(exists(x) { exists(y_1) { P(y_1) implies (P(x) implies Q(x, y)) } }, exists(x) { forall(y) { P(y) } implies (P(x) implies Q(x, y)) }.pnf())
        assertEquals(forall(x) { forall(z) { (P() or Q(x)) implies R(z) } }, ((P() or exists(x) { Q(x) }) implies forall(z) { R(z) }).pnf())
        assertEquals(forall(x) { exists(y) { forall(z) { forall(x_1) { forall(w) { (Q(x) and !R(x_1)) or ((!Q(y) implies R(y))) } } } } }
                , (forall(x) { exists(y) { (forall(z) { Q(x) and !exists(x) { R(x) } }) or (!Q(y) implies forall(w) { R(y) }) } }).pnf())
        assertEquals(forall(x) { exists(y) { exists(y_1) { P(y, x) implies Q(x, y_1) } } }, forall(x) { forall(y) { P(y, x) } implies exists(y) { Q(x, y) } }.pnf())

        assertEquals(INVALID_FORMULA, try {
            BAD_FORMULA.pnf()
        } catch (e: Exception) {
            e.message
        })
    }

    @Test
    fun skolem() {
        assertEquals(P(sk_0()), exists(x) { P(x) }.snf())
        assertEquals(forall(x) { P(x, sk_0(x)) }, forall(x) { exists(y) { P(x, y) } }.snf())
        assertEquals(forall(x) { P(x, f(g(sk_0(x)), h(sk_0(x)))) }, forall(x) { exists(y) { P(x, f(g(y), h(y))) } }.snf())
        assertEquals((sk_0() equals sk_1()) and (sk_1() equals sk_2()), exists(x, y, z) { (x equals y) and (y equals z) }.snf())
        assertEquals(forall(y) { Q(sk_0(), y) or P(sk_1(y), y, sk_2(y)) }, exists(x) { forall(y) { Q(x, y) or exists(x, z) { P(x, y, z) } } }.snf())
        // random formulas
        assertEquals(forall(x) { forall(z) { P(x, sk_0(x), z) } }, forall(x) { exists(y) { forall(z) { P(x, y, z) } } }.snf())
        assertEquals(forall(x) { R(g(x)) or R(x, sk_0(x)) }, forall(x) { R(g(x)) or exists(y) { R(x, y) } }.snf())
        assertEquals(forall(y) { forall(z) { forall(v) { P(sk_0(), y, z, sk_1(y, z), v, sk_2(y, z, v)) } } },
                exists(x) { forall(y) { forall(z) { exists(u) { forall(v) { exists(w) { P(x, y, z, u, v, w) } } } } } }.snf())
        // test generator across multiple Skolem calls
        assertEquals(listOf(P(sk_0()), Q(sk_1())), {
            val generator = SkolemGenerator()
            listOf(exists(x) { P(x) }.snf(generator), exists(x) { Q(x) }.snf(generator)
            )
        }.invoke())
    }

    @Test
    fun nnf() {
        // non-changing formulas
        assertEquals(Top, Top.nnf())
        assertEquals(Bottom, Bottom.nnf())
        assertEquals(P(x), P(x).nnf())
        assertEquals(x equals y, (x equals y).nnf())
        assertEquals(!P(x), (!P(x)).nnf())
        assertEquals(P(x) and Q(y), (P(x) and Q(y)).nnf())
        assertEquals(P(x) or Q(y), (P(x) or Q(y)).nnf())
        assertEquals(!P(x) or Q(y), (P(x) implies Q(y)).nnf())
        assertEquals(exists(x) { P(x) }, exists(x) { P(x) }.nnf())
        assertEquals(forall(x) { P(x) }, forall(x) { P(x) }.nnf())
        // sanity checking
        assertEquals(Bottom, (!Top).nnf())
        assertEquals(Top, (!Bottom).nnf())
        assertEquals(P(x), (!!P(x)).nnf())
        assertEquals(x equals y, (!!(x equals y)).nnf())
        assertEquals(!P(x) or !Q(y), (!(P(x) and Q(y))).nnf())
        assertEquals(!P(x) and !Q(y), (!(P(x) or Q(y))).nnf())
        assertEquals(P(x) and !Q(y), (!(P(x) implies Q(y))).nnf())
        assertEquals((!P(x) and !Q(y)) or R(z), ((P(x) or Q(y)) implies R(z)).nnf())
        assertEquals(forall(x) { !P(x) }, (!exists(x) { P(x) }).nnf())
        assertEquals(exists(x) { !P(x) }, (!forall(x) { P(x) }).nnf())
        // recursive application
        assertEquals(P(x) and Q(y), (!!P(x) and !!Q(y)).nnf())
        assertEquals(P(x) or Q(y), (!!P(x) or !!Q(y)).nnf())
        assertEquals(!P(x) or Q(y), (!!P(x) implies !!Q(y)).nnf())
        assertEquals(exists(x) { P(x) }, (exists(x) { !!P(x) }).nnf())
        assertEquals(forall(x) { P(x) }, (forall(x) { !!P(x) }).nnf())
        assertEquals(!P(x), (!!!P(x)).nnf())
        assertEquals(P(x) or Q(y), (!(!P(x) and !Q(y))).nnf())
        assertEquals(P(x) and Q(y), (!(!P(x) or !Q(y))).nnf())
        assertEquals(!P(x) and Q(y), (!(!P(x) implies !Q(y))).nnf())
        assertEquals((P(x) and Q(x)) or (P(y) and Q(y)), (!(!(P(x) and Q(x)) and !(P(y) and Q(y)))).nnf())
        assertEquals((P(x) and Q(x)) and (P(y) and Q(y)), (!(!(P(x) and Q(x)) or !(P(y) and Q(y)))).nnf())
        assertEquals((!P(x) or !Q(x)) and (P(y) and Q(y)), (!(!(P(x) and Q(x)) implies !(P(y) and Q(y)))).nnf())
        assertEquals(forall(x) { exists(y) { P(x) and !Q(y) } }, (!exists(x) { forall(y) { P(x) implies Q(y) } }).nnf())
        assertEquals((forall(x) { !P(x) }) or (exists(y) { !Q(y) }), (!((exists(x) { P(x) }) and (forall(y) { Q(y) }))).nnf())
    }

    @Test
    fun cnf() {
        // sanity checking
        assertEquals(Top, Top.cnf())
        assertEquals(Bottom, Bottom.cnf())
        assertEquals(P(f(), g(f(), f())), P(f(), g(f(), f())).cnf())
        assertEquals(P(x), P(x).cnf())
        assertEquals(x equals y, (x equals y).cnf())
        assertEquals(P(x) and Q(y), (P(x) and Q(y)).cnf())
        assertEquals(P(x) or Q(y), (P(x) or Q(y)).cnf())
        assertEquals(!P(x) or Q(y), (P(x) implies Q(y)).cnf())
        assertEquals(P(x), forall(x) { P(x) }.cnf())
        assertEquals(P(f(), g(f(), x)), forall(x) { P(f(), g(f(), x)) }.cnf())
        // quantifier-free formulas
        assertEquals((!P(x1) or !Q(y)) and (!P(x2) or !Q(y)), (!((P(x1) or P(x2)) and Q(y))).cnf())
        assertEquals((P(x) or Q(x)) and (P(x) or !Q(y)), (P(x) or !(Q(x) implies Q(y))).cnf())
        assertEquals((!P(x) or R(z)) and (!Q(y) or R(z)), ((P(x) or Q(y)) implies R(z)).cnf())
        assertEquals((P(x) or (Q(x) or R(y))) and (P(x) or (Q(x) or R(z))), (P(x) or (Q(x) or (R(y) and R(z)))).cnf())
        assertEquals(((Q(x) or R(y)) or P(x)) and ((Q(x) or R(z)) or P(x)), ((Q(x) or (R(y) and R(z))) or P(x)).cnf())
        assertEquals(((P(x1) or Q(x1)) and (P(x1) or Q(x2))) and ((P(x2) or Q(x1)) and (P(x2) or Q(x2))), ((P(x1) and P(x2)) or (Q(x1) and Q(x2))).cnf())
        // random formulas
        assertEquals(P(sk_0()), exists(x) { P(x) }.cnf())
        assertEquals(P(sk_0()) and Q(f(), sk_0()), exists(x) { P(x) and Q(f(), x) }.cnf())
        assertEquals(!P(y) or !Q(x, y) or R(x), forall(x) { exists(y) { P(y) and Q(x, y) } implies R(x) }.cnf())
        assertEquals(!P(x) or (!Q(y) or !R(x, y)), forall(x) { P(x) implies forall(y) { Q(y) implies !R(x, y) } }.cnf())
        assertEquals((!P(y, sk_0(y)) or Q(y)) and (!Q(sk_0(y)) or Q(y)), forall(y) { forall(x) { P(y, x) or Q(x) } implies Q(y) }.cnf())
        assertEquals(P(sk_0(), sk_1()), exists(x) { exists(y) { P(x, y) } }.cnf())
        assertEquals(P(sk_0(), sk_1()), exists(x, y) { P(x, y) }.cnf())
        assertEquals(P(x, sk_0(x)), forall(x) { exists(y) { P(x, y) } }.cnf())
        assertEquals(((!R(z)) or P(sk_0(), x)) and (((!R(z)) or (!Q(u_1, x_1, y))) and ((!R(z)) or (!(w equals f(u_1))))),
                (R(z) implies exists(u) { forall(x, y) { P(u, x) and !exists(u, x, w) { Q(u, x, y) or (w equals f(u)) } } }).cnf())
        assertEquals((P(sk_0(x)) or Q(sk_1(x), x)) and (!Q(x, sk_0(x)) or Q(sk_1(x), x)), (forall(x) { forall(y) { P(y) implies Q(x, y) } implies exists(y) { Q(y, x) } }).cnf())
        assertEquals(P(sk_0()) and (!Q(sk_0(), y) or (y equals z) or !Q(sk_0(), z)), exists(x) { forall(y, z) { P(x) and ((Q(x, y) and !(y equals z)) implies !Q(x, z)) } }.cnf())
        assertEquals((!P(x) or (!P(y) or P(f(x, y)))) and ((!P(x) or Q(x, sk_0(x, y))) and (!P(x) or !P(sk_0(x, y)))), forall(x) { P(x) implies (forall(y) { P(y) implies P(f(x, y)) } and !forall(y) { Q(x, y) implies P(y) }) }.cnf())
    }

    @Test
    fun simplify() {
        // sanity checking
        assertEquals(FALSE, (!TRUE).simplify())
        assertEquals(TRUE, (!FALSE).simplify())
        assertEquals(!P(x), (!P(x)).simplify())
        assertEquals(TRUE, (TRUE and TRUE).simplify())
        assertEquals(FALSE, (FALSE and FALSE).simplify())
        assertEquals(FALSE, (FALSE and TRUE).simplify())
        assertEquals(FALSE, (TRUE and FALSE).simplify())
        assertEquals(P(x), (P(x) and TRUE).simplify())
        assertEquals(FALSE, (FALSE and P(x)).simplify())
        assertEquals(FALSE, (P(x) and FALSE).simplify())
        assertEquals(P(x), (TRUE and P(x)).simplify())
        assertEquals(P(x) and Q(x), (P(x) and Q(x)).simplify())
        assertEquals(TRUE, (TRUE or TRUE).simplify())
        assertEquals(FALSE, (FALSE or FALSE).simplify())
        assertEquals(TRUE, (FALSE or TRUE).simplify())
        assertEquals(TRUE, (TRUE or FALSE).simplify())
        assertEquals(TRUE, (P(x) or TRUE).simplify())
        assertEquals(P(x), (FALSE or P(x)).simplify())
        assertEquals(P(x), (P(x) or FALSE).simplify())
        assertEquals(TRUE, (TRUE or P(x)).simplify())
        assertEquals(P(x) or Q(x), (P(x) or Q(x)).simplify())
        assertEquals(TRUE, (TRUE implies TRUE).simplify())
        assertEquals(TRUE, (FALSE implies FALSE).simplify())
        assertEquals(TRUE, (FALSE implies TRUE).simplify())
        assertEquals(FALSE, (TRUE implies FALSE).simplify())
        assertEquals(TRUE, (P(x) implies TRUE).simplify())
        assertEquals(TRUE, (FALSE implies P(x)).simplify())
        assertEquals(!P(x), (P(x) implies FALSE).simplify())
        assertEquals(P(x), (TRUE implies P(x)).simplify())
        assertEquals(P(x) implies Q(x), (P(x) implies Q(x)).simplify())
        assertEquals(P(y), (exists(x) { P(y) }).simplify())
        assertEquals(exists(y) { P(y, z) }, (exists(x, y) { P(y, z) }).simplify())
        assertEquals(exists(x) { P(x) }, (exists(x) { P(x) }).simplify())
        assertEquals(P(y), (forall(x) { P(y) }).simplify())
        assertEquals(forall(y) { P(y, z) }, (forall(x, y) { P(y, z) }).simplify())
        assertEquals(forall(x) { P(x) }, (forall(x) { P(x) }).simplify())
        // random formulas
        assertEquals(P(x), (!!P(x)).simplify())
        assertEquals(!P(x), (!!!P(x)).simplify())
        assertEquals(TRUE, (!(TRUE implies FALSE)).simplify())
        assertEquals(P(x), (FALSE or (P(x) and TRUE)).simplify())
        assertEquals(TRUE, (exists(x) { P(x) or TRUE }).simplify())
        assertEquals(!P(x), exists(y) { (P(x) implies FALSE) and (FALSE implies Q(x)) }.simplify())
        assertEquals(TRUE, forall(x) { exists(y) { P(x, y) or TRUE } }.simplify())
        assertEquals(x equals y, (((((x equals y) implies FALSE) implies FALSE) implies FALSE) implies FALSE).simplify())
        assertEquals(forall(x) { P(x) or (w equals x) }, forall(x, y, z) { exists(z) { P(x) or (w equals x) } }.simplify())
        assertEquals(TRUE, ((P(x) or FALSE) or (P(x) or TRUE)).simplify())
        assertEquals(FALSE, ((P(x) and FALSE) and (P(x) and TRUE)).simplify())
    }

    @Test
    fun geometric() {
        // sanity checking
        assertEquals(setOf(TRUE implies TRUE), (TRUE).geometric())
        assertEquals(setOf(TRUE implies FALSE), (FALSE).geometric())
        assertEquals(setOf(TRUE implies P(x)), (P(x)).geometric())
        assertEquals(setOf(TRUE implies (x equals y)), (x equals y).geometric())
        assertEquals(setOf(P(x) implies FALSE), (!P(x)).geometric())
        assertEquals(setOf(P(x) implies Q(x)), (P(x) implies Q(x)).geometric())
        assertEquals(setOf(
                TRUE implies P(x),
                TRUE implies Q(x)
        ), (P(x) and Q(x)).geometric())
        assertEquals(setOf(
                TRUE implies (P(x) or Q(x))
        ), (P(x) or Q(x)).geometric())
        assertEquals(setOf(
                TRUE implies P(x)
        ), (forall(x) { P(x) }).geometric())
        assertEquals(setOf(
                TRUE implies P(sk_0())
        ), (exists(x) { P(x) }).geometric())

        assertEquals(setOf(
                P(x) and Q(x) implies (P(y) or Q(y))
        ), (P(x) and Q(x) implies (P(y) or Q(y))).geometric())
        assertEquals(setOf(
                P(x) implies P(y),
                P(x) implies Q(y),
                Q(x) implies P(y),
                Q(x) implies Q(y)
        ), (P(x) or Q(x) implies (P(y) and Q(y))).geometric())
        assertEquals(setOf(
                P(x) implies P(y),
                P(x) implies Q(y),
                Q(x) implies P(y),
                Q(x) implies Q(y)
        ), (P(x) or Q(x) implies (P(y) and Q(y))).geometric())
        assertEquals(setOf(
                P(x) implies Q(x, sk_0(x))
        ), (forall(x) { P(x) implies (exists(y) { Q(x, y) }) }).geometric())
        assertEquals(setOf(
                P(x) implies (Q(sk_0(x)) or P(sk_1(x))),
                P(x) implies (Q(sk_0(x)) or S(x, sk_1(x))),
                P(x) implies (R(x, sk_0(x)) or S(x, sk_1(x))),
                P(x) implies (R(x, sk_0(x)) or P(sk_1(x)))
        ), (forall(x) { P(x) implies (exists(y) { Q(y) and R(x, y) } or exists(y) { P(y) and S(x, y) }) }).geometric())
        assertEquals(setOf(
                ((P(x) and Q(y)) and R(x, y)) implies S(x, y)
        ), (forall(x, y) { (P(x) and Q(y)) implies (R(x, y) implies S(x, y)) }).geometric())
        assertEquals(setOf(
                P(x_1) implies Q(x)
        ), (exists(x) { P(x) } implies Q(x)).geometric())
        assertEquals(setOf(
                P(sk_0()) implies Q(sk_0())
        ), (exists(x) { P(x) implies Q(x) }).geometric())
    }
}