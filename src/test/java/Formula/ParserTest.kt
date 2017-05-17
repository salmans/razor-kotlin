package Formula

import org.junit.Test


internal class ParserTest {
    @Test
    fun parse() {
        assertTheoriesEqual(TRUE
                , actual = "TRUE".parseTheory())
        assertTheoriesEqual(FALSE
                , actual = "FALSE".parseTheory())
        assertTheoriesEqual(TRUE
                , actual = "((((TRUE))))".parseTheory())
        assertTheoriesEqual(P()
                , actual = "P()".parseTheory())
        assertTheoriesEqual(P(x)
                , actual = "P(x)".parseTheory())
        assertTheoriesEqual(P(x, y)
                , actual = "P(x,y)".parseTheory())
        assertTheoriesEqual(P(x, y)
                , actual = "P(x, y)".parseTheory())
        assertTheoriesEqual(P(x, y)
                , actual = "P(x,            y     \n)".parseTheory())
        assertTheoriesEqual(P(f(x))
                , actual = "P(f(x))".parseTheory())
        assertTheoriesEqual(P(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(x)))))))))))))))))))))
                , actual = "P(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(x)))))))))))))))))))))".parseTheory())
        assertTheoriesEqual(P(f(x, g(y)), g(f(g(y))))
                , actual = "P(f(x, g(y)), g(f(g(y))))".parseTheory())
        assertTheoriesEqual(x equals x
                , actual = "x = x".parseTheory())
        assertTheoriesEqual(f() equals x
                , actual = "f() = x".parseTheory())
        assertTheoriesEqual(f(x) equals x
                , actual = "f(x) = x".parseTheory())
        assertTheoriesEqual(f(x) equals g(h(g(f(x)), y))
                , actual = "f(x) = g(h(g(f(x)), y))".parseTheory())
        assertTheoriesEqual(P(x) implies Q(x)
                , actual = "P(x) implies Q(x)".parseTheory())
        assertTheoriesEqual((P(x) implies Q(x)) implies R(x)
                , actual = "P(x) implies Q(x) -> R(x)".parseTheory())
        assertTheoriesEqual(P(x) implies (Q(x) implies R(x))
                , actual = "P(x) implies (Q(x) -> R(x))".parseTheory())
        assertTheoriesEqual(P(x) implies ((Q(x) implies R(x)) implies Q(z))
                , actual = "P(x) implies (Q(x) -> R(x) -> Q(z))".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) }
                , actual = "exists x . P(x)".parseTheory())
        assertTheoriesEqual(exists(x, y) { P(x, y) }
                , actual = "exists x,y . P(x, y)".parseTheory())
        assertTheoriesEqual(exists(x) { exists(y, z) { P(x, y, z) } }
                , actual = "exists x . exists y, z. P(x, y, z)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) } implies Q(x)
                , actual = "exists x . P(x) implies Q(x)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) implies Q(x) }
                , actual = "exists x . (P(x) implies Q(x))".parseTheory())
        assertTheoriesEqual(forall(x) { P(x) }
                , actual = "forall x . P(x)".parseTheory())
        assertTheoriesEqual(forall(x, y) { P(x, y) }
                , actual = "forall x,y . P(x, y)".parseTheory())
        assertTheoriesEqual(forall(x) { forall(y, z) { P(x, y, z) } }
                , actual = "forall x . forall y, z. P(x, y, z)".parseTheory())
        assertTheoriesEqual(forall(x) { P(x) } implies Q(x)
                , actual = "forall x . P(x) implies Q(x)".parseTheory())
        assertTheoriesEqual(forall(x) { P(x) implies Q(x) }
                , actual = "forall x . (P(x) implies Q(x))".parseTheory())
        assertTheoriesEqual(forall(x) { exists(y) { P(x, y) } }
                , actual = "forall x . exists y . P(x, y)".parseTheory())
        assertTheoriesEqual(P(x) or Q(y)
                , actual = "P(x) or Q(y)".parseTheory())
        assertTheoriesEqual((P(x) or Q(y)) or R(z)
                , actual = "P(x) or Q(y) or R(z)".parseTheory())
        assertTheoriesEqual((P(x) or Q(y)) or R(z)
                , actual = "(P(x) or Q(y)) or R(z)".parseTheory())
        assertTheoriesEqual((P(x) or Q(y)) or (R(z) or S(z))
                , actual = "P(x) or Q(y) or (R(z) or S(z))".parseTheory())
        assertTheoriesEqual(P(x) implies (Q(x) or R(x))
                , actual = "P(x) implies Q(x) or R(x)".parseTheory())
        assertTheoriesEqual((P(x) or Q(x)) implies R(x)
                , actual = "P(x) or Q(x) implies R(x)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) or Q(x) }
                , actual = "exists x . P(x) or Q(x)".parseTheory())
        assertTheoriesEqual(P(x) or exists(y) { Q(y) }
                , actual = "P(x) or exists y . Q(y)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) or exists(y) { Q(y) } }
                , actual = "exists x . P(x) or exists y . Q(y)".parseTheory())
        assertTheoriesEqual(P(x) or forall(y) { Q(y) }
                , actual = "P(x) or forall y . Q(y)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) or forall(y) { Q(y) } }
                , actual = "exists x . P(x) or forall y . Q(y)".parseTheory())
        assertTheoriesEqual((P(x) and Q(y)) or R(z)
                , actual = "P(x) and Q(y) or R(z)".parseTheory())
        assertTheoriesEqual(P(x) and (Q(y) or R(z))
                , actual = "P(x) and (Q(y) or R(z))".parseTheory())
        assertTheoriesEqual(P(x) or (Q(y) and R(z))
                , actual = "P(x) or Q(y) and R(z)".parseTheory())
        assertTheoriesEqual((P(x) and Q(y)) and R(z)
                , actual = "P(x) and Q(y) and R(z)".parseTheory())
        assertTheoriesEqual((P(x) and Q(y)) and R(z)
                , actual = "(P(x) and Q(y)) and R(z)".parseTheory())
        assertTheoriesEqual((P(x) and Q(y)) implies R(z)
                , actual = "P(x) and Q(y) implies R(z)".parseTheory())
        assertTheoriesEqual(P(x) and exists(y) { Q(y) }
                , actual = "P(x) and exists y . Q(y)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) and exists(y) { Q(y) } }
                , actual = "exists x . P(x) and exists y . Q(y)".parseTheory())
        assertTheoriesEqual(P(x) and forall(y) { Q(y) }
                , actual = "P(x) and forall y . Q(y)".parseTheory())
        assertTheoriesEqual(exists(x) { P(x) and forall(y) { Q(y) } }
                , actual = "exists x . P(x) and forall y . Q(y)".parseTheory())
        assertTheoriesEqual((!TRUE) implies FALSE
                , actual = "not TRUE -> FALSE".parseTheory())
        assertTheoriesEqual(!(x equals y)
                , actual = "~x=y".parseTheory())
        assertTheoriesEqual(TRUE implies (!FALSE)
                , actual = "TRUE -> not FALSE".parseTheory())
        assertTheoriesEqual((!P(x, y)) or Q(z)
                , actual = "not P(x, y) or Q(z)".parseTheory())
        assertTheoriesEqual((!P(x, y)) and Q(z)
                , actual = "not P(x, y) and Q(z)".parseTheory())
        assertTheoriesEqual(!(!R(x))
                , actual = "not not R(x)".parseTheory())
        assertTheoriesEqual((!(!(!(!(!R(x)))))) and S(y)
                , actual = "not not not not not R(x) and S(y)".parseTheory())
        assertTheoriesEqual(!(exists(y) { Q(y) })
                , actual = "not exists y . Q(y)".parseTheory())
        assertTheoriesEqual(exists(x) { !exists(y) { Q(y) } }
                , actual = "exists x . not exists y . Q(y)".parseTheory())
        assertTheoriesEqual(P(x) implies (Q(y) and exists(z){(f(z) equals g(f(z))) or (forall(y, z){S(y,z)} implies FALSE)})
                , actual = "P(x) implies Q(y) and exists z . f(z) = g(f(z)) or (forall y, z . S(y,z) implies FALSE)".parseTheory())
        assertTheoriesEqual((!forall(x, y){P(x) and Q(y)}) implies (h(z) equals z)
                , actual = "not forall x, y . P(x) and Q(y) implies h(z) = z".parseTheory())
        assertTheoriesEqual(E(x, x)
                , E(x, y) implies E(y, x)
                , (E(x, y) and E(y, z)) implies E(x, z)
                , actual = ("E(x,x)\n" +
                "E(x,y) -> E(y,x)\n" +
                "E(x,y) & E(y,z) -> E(x,z)").parseTheory())
        assertTheoriesEqual(x equals x
                , (x equals y) implies (y equals x)
                , ((x equals y) and (y equals z)) implies (x equals z)
                , actual = ("x = x\n" +
                "x = y -> y = x\n" +
                "x = y & y = z -> x = z").parseTheory())
        assertTheoriesEqual("∀ x. (∃ y. (((x = y) ∧ ¬P(y)) ∨ (Q(x) → R(y))))".parseTheory()!!.formulas.first()
                , actual = "forall x . exists y . (x = y and not P(y)) or (Q(x) implies R(y))".parseTheory())
    }

    @Test
    fun parseError() {
        assertFailure("Parse error at (1, 2): expecting '(' but '<End of Input>' is found.", { "T".parseTheory() })
        assertFailure("Parse error at (1, 8): expecting '<Lowercase Identifier>' but '.' is found.", { "forall . P(x)".parseTheory() })
        assertFailure("Parse error at (1, 10): expecting '.' but 'P' is found.", { "forall x P(x)".parseTheory() })
        assertFailure("Parse error at (1, 10): expecting '<Lowercase Identifier>' but '.' is found.", { "forall x,. P(x)".parseTheory() })
        assertFailure("Parse error at (1, 15): expecting '.' but 'Q' is found.", { "P(x) forall x Q(x)".parseTheory() })
        assertFailure("Parse error at (1, 9): expecting '=' but '<End of Input>' is found.", { "P(x) | x".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting '=' but '|' is found.", { "x | P(x)".parseTheory() })
        assertFailure("Parse error at (1, 9): expecting '(' but '<End of Input>' is found.", { "P(x) | Q".parseTheory() })
        assertFailure("Parse error at (1, 2): expecting '(' but '|' is found.", { "Q|P(x)".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting '=' but '<End of Input>' is found.", { "~x".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but '<End of Input>' is found.", { "~(".parseTheory() })
        assertFailure("Parse error at (1, 2): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but ')' is found.", { "()".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but ')' is found.", { ")".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but 'or' is found.", { "~ or".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but '|' is found.", { "~ |".parseTheory() })
        assertFailure("Parse error at (1, 9): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but '<End of Input>' is found.", { "P(x) or ".parseTheory() })
        assertFailure("Parse error at (1, 10): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but '<End of Input>' is found.", { "P(x) and ".parseTheory() })
        assertFailure("Parse error at (1, 5): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but '<End of Input>' is found.", { "not ".parseTheory() })
        assertFailure("Parse error at (1, 2): expecting '∃', '∀', '¬', '⊤', '⟘', '<Lowercase Identifier>', '<Uppercase Identifier>', '(' but 'and' is found.", { " and Q(x)".parseTheory() })
        assertFailure("Parse error at (1, 12): expecting '<Lowercase Identifier>' but '=' is found.", { "f(x, g(y), = h(x)".parseTheory() })
        assertFailure("Parse error at (1, 11): expecting ')' but '=' is found.", { "f(x, g(y) = h(x)".parseTheory() })
    }
}