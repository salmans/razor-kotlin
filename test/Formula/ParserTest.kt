package Formula

import org.junit.jupiter.api.Test

internal class ParserTest {
    @Test
    fun parse() {
        val parser = Parser()
        assertTheoriesEqual(TRUE
                , actual = parser.parse("TRUE"))
        assertTheoriesEqual(FALSE
                , actual = parser.parse("FALSE"))
        assertTheoriesEqual(TRUE
                , actual = parser.parse("((((TRUE))))"))
        assertTheoriesEqual(P(x)
                , actual = parser.parse("P(x)"))
        assertTheoriesEqual(P(x, y)
                , actual = parser.parse("P(x,y)"))
        assertTheoriesEqual(P(x, y)
                , actual = parser.parse("P(x, y)"))
        assertTheoriesEqual(P(x, y)
                , actual = parser.parse("P(x,            y     \n)"))
        assertTheoriesEqual(P(f(x))
                , actual = parser.parse("P(f(x))"))
        assertTheoriesEqual(P(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(x)))))))))))))))))))))
                , actual = parser.parse("P(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(x)))))))))))))))))))))"))
        assertTheoriesEqual(P(f(x, g(y)), g(f(g(y))))
                , actual = parser.parse("P(f(x, g(y)), g(f(g(y))))"))
        assertTheoriesEqual(x equals x
                , actual = parser.parse("x = x"))
        assertTheoriesEqual(f(x) equals x
                , actual = parser.parse("f(x) = x"))
        assertTheoriesEqual(f(x) equals g(h(g(f(x)), y))
                , actual = parser.parse("f(x) = g(h(g(f(x)), y))"))
        assertTheoriesEqual(P(x) implies Q(x)
                , actual = parser.parse("P(x) implies Q(x)"))
        assertTheoriesEqual((P(x) implies Q(x)) implies R(x)
                , actual = parser.parse("P(x) implies Q(x) -> R(x)"))
        assertTheoriesEqual(P(x) implies (Q(x) implies R(x))
                , actual = parser.parse("P(x) implies (Q(x) -> R(x))"))
        assertTheoriesEqual(P(x) implies ((Q(x) implies R(x)) implies Q(z))
                , actual = parser.parse("P(x) implies (Q(x) -> R(x) -> Q(z))"))
        assertTheoriesEqual(exists(x) { P(x) }
                , actual = parser.parse("exists x . P(x)"))
        assertTheoriesEqual(exists(x, y) { P(x, y) }
                , actual = parser.parse("exists x,y . P(x, y)"))
        assertTheoriesEqual(exists(x) { exists(y, z) { P(x, y, z) } }
                , actual = parser.parse("exists x . exists y, z. P(x, y, z)"))
        assertTheoriesEqual(exists(x) { P(x) } implies Q(x)
                , actual = parser.parse("exists x . P(x) implies Q(x)"))
        assertTheoriesEqual(exists(x) { P(x) implies Q(x) }
                , actual = parser.parse("exists x . (P(x) implies Q(x))"))
        assertTheoriesEqual(forall(x) { P(x) }
                , actual = parser.parse("forall x . P(x)"))
        assertTheoriesEqual(forall(x, y) { P(x, y) }
                , actual = parser.parse("forall x,y . P(x, y)"))
        assertTheoriesEqual(forall(x) { forall(y, z) { P(x, y, z) } }
                , actual = parser.parse("forall x . forall y, z. P(x, y, z)"))
        assertTheoriesEqual(forall(x) { P(x) } implies Q(x)
                , actual = parser.parse("forall x . P(x) implies Q(x)"))
        assertTheoriesEqual(forall(x) { P(x) implies Q(x) }
                , actual = parser.parse("forall x . (P(x) implies Q(x))"))
        assertTheoriesEqual(forall(x) { exists(y) { P(x, y) } }
                , actual = parser.parse("forall x . exists y . P(x, y)"))
        assertTheoriesEqual(P(x) or Q(y)
                , actual = parser.parse("P(x) or Q(y)"))
        assertTheoriesEqual((P(x) or Q(y)) or R(z)
                , actual = parser.parse("P(x) or Q(y) or R(z)"))
        assertTheoriesEqual((P(x) or Q(y)) or R(z)
                , actual = parser.parse("(P(x) or Q(y)) or R(z)"))
        assertTheoriesEqual((P(x) or Q(y)) or (R(z) or S(z))
                , actual = parser.parse("P(x) or Q(y) or (R(z) or S(z))"))
        assertTheoriesEqual(P(x) implies (Q(x) or R(x))
                , actual = parser.parse("P(x) implies Q(x) or R(x)"))
        assertTheoriesEqual((P(x) or Q(x)) implies R(x)
                , actual = parser.parse("P(x) or Q(x) implies R(x)"))
        assertTheoriesEqual(exists(x) { P(x) or Q(x) }
                , actual = parser.parse("exists x . P(x) or Q(x)"))
        assertTheoriesEqual(P(x) or exists(y) { Q(y) }
                , actual = parser.parse("P(x) or exists y . Q(y)"))
        assertTheoriesEqual(exists(x) { P(x) or exists(y) { Q(y) } }
                , actual = parser.parse("exists x . P(x) or exists y . Q(y)"))
        assertTheoriesEqual((P(x) and Q(y)) or R(z)
                , actual = parser.parse("P(x) and Q(y) or R(z)"))
        assertTheoriesEqual(P(x) and (Q(y) or R(z))
                , actual = parser.parse("P(x) and (Q(y) or R(z))"))
        assertTheoriesEqual(P(x) or (Q(y) and R(z))
                , actual = parser.parse("P(x) or Q(y) and R(z)"))
        assertTheoriesEqual((P(x) and Q(y)) and R(z)
                , actual = parser.parse("P(x) and Q(y) and R(z)"))
        assertTheoriesEqual((P(x) and Q(y)) and R(z)
                , actual = parser.parse("(P(x) and Q(y)) and R(z)"))
        assertTheoriesEqual((P(x) and Q(y)) implies R(z)
                , actual = parser.parse("P(x) and Q(y) implies R(z)"))
        assertTheoriesEqual(P(x) and exists(y) { Q(y) }
                , actual = parser.parse("P(x) and exists y . Q(y)"))
        assertTheoriesEqual(exists(x) { P(x) and exists(y) { Q(y) } }
                , actual = parser.parse("exists x . P(x) and exists y . Q(y)"))
        assertTheoriesEqual((!TRUE) implies FALSE
                , actual = parser.parse("not TRUE -> FALSE"))
        assertTheoriesEqual(TRUE implies (!FALSE)
                , actual = parser.parse("TRUE -> not FALSE"))
        assertTheoriesEqual((!P(x, y)) or Q(z)
                , actual = parser.parse("not P(x, y) or Q(z)"))
        assertTheoriesEqual((!P(x, y)) and Q(z)
                , actual = parser.parse("not P(x, y) and Q(z)"))
        assertTheoriesEqual(!(!R(x))
                , actual = parser.parse("not not R(x)"))
        assertTheoriesEqual((!(!(!(!(!R(x)))))) and S(y)
                , actual = parser.parse("not not not not not R(x) and S(y)"))
        assertTheoriesEqual(!(exists(y) { Q(y) })
                , actual = parser.parse("not exists y . Q(y)"))
        assertTheoriesEqual(exists(x) { !exists(y) { Q(y) } }
                , actual = parser.parse("exists x . not exists y . Q(y)"))
        assertTheoriesEqual(P(x) implies (Q(y) and exists(z){(f(z) equals g(f(z))) or (forall(y, z){S(y,z)} implies FALSE)})
                , actual = parser.parse("P(x) implies Q(y) and exists z . f(z) = g(f(z)) or (forall y, z . S(y,z) implies FALSE)"))
        assertTheoriesEqual((!forall(x, y){P(x) and Q(y)}) implies (h(z) equals z)
                , actual = parser.parse("not forall x, y . P(x) and Q(y) implies h(z) = z"))
        assertTheoriesEqual(E(x, x)
                , E(x, y) implies E(y, x)
                , (E(x, y) and E(y, z)) implies E(x, z)
                , actual = parser.parse("E(x,x)\n" +
                "E(x,y) -> E(y,x)\n" +
                "E(x,y) & E(y,z) -> E(x,z)"))
        assertTheoriesEqual(x equals x
                , (x equals y) implies (y equals x)
                , ((x equals y) and (y equals z)) implies (x equals z)
                , actual = parser.parse("x = x\n" +
                "x = y -> y = x\n" +
                "x = y & y = z -> x = z"))
        assertTheoriesEqual(parser.parse("∀ x. (∃ y. (((x = y) ∧ ¬P(y)) ∨ (Q(x) → R(y))))")!!.formulas.first()
                , actual = parser.parse("forall x . exists y . (x = y and not P(y)) or (Q(x) implies R(y))"))
    }
}