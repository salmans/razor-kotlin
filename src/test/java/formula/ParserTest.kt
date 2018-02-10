package formula

import org.junit.Assert
import org.junit.Test

internal class TokenizerTest {
    @Test
    fun loc() {
        Assert.assertEquals(loc(1, 2), Token.Location(1, 2))
        Assert.assertEquals(loc(101, 102), Token.Location(101, 102))
    }

    @Test
    fun token() {
        Assert.assertEquals(token(TokenType.LOWER, "x"), Token(TokenType.LOWER, "x", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.UPPER, "P"), Token(TokenType.UPPER, "P", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.COMMA, ","), Token(TokenType.COMMA, ",", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.DOT, "."), Token(TokenType.DOT, ".", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.LPAREN, "("), Token(TokenType.LPAREN, "(", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.RPAREN, ")"), Token(TokenType.RPAREN, ")", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.EQUALS, "="), Token(TokenType.EQUALS, "=", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.TRUE, "TRUE"), Token(TokenType.TRUE, "TRUE", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.TRUE, "FALSE"), Token(TokenType.TRUE, "FALSE", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.NOT, "not"), Token(TokenType.NOT, "not", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.AND, "and"), Token(TokenType.AND, "and", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.OR, "or"), Token(TokenType.OR, "or", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.IFF, "iff"), Token(TokenType.IFF, "iff", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.FORALL, "forall"), Token(TokenType.FORALL, "forall", Token.Location(0, 0)))
        Assert.assertEquals(token(TokenType.EXISTS, "exists"), Token(TokenType.EXISTS, "exists", Token.Location(0, 0)))
    }

    @Test
    fun tokenize() {
        Assert.assertEquals(listOf(token(TokenType.LOWER, "x", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("x"))
        Assert.assertEquals(listOf(token(TokenType.UPPER, "P", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("P"))
        Assert.assertEquals(listOf(token(TokenType.COMMA, ",", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize(","))
        Assert.assertEquals(listOf(token(TokenType.DOT, ".", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("."))
        Assert.assertEquals(listOf(token(TokenType.LPAREN, "(", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("("))
        Assert.assertEquals(listOf(token(TokenType.RPAREN, ")", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize(")"))
        Assert.assertEquals(listOf(token(TokenType.EQUALS, "=", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("="))
        Assert.assertEquals(listOf(token(TokenType.TRUE, "TRUE", 1, 1)
                , token(TokenType.END, "end of input", 1, 5)), tokenize("TRUE"))
        Assert.assertEquals(listOf(token(TokenType.TRUE, "⊤", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("⊤"))
        Assert.assertEquals(listOf(token(TokenType.FALSE, "FALSE", 1, 1)
                , token(TokenType.END, "end of input", 1, 6)), tokenize("FALSE"))
        Assert.assertEquals(listOf(token(TokenType.FALSE, "⟘", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("⟘"))
        Assert.assertEquals(listOf(token(TokenType.NOT, "not", 1, 1)
                , token(TokenType.END, "end of input", 1, 4)), tokenize("not"))
        Assert.assertEquals(listOf(token(TokenType.NOT, "~", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("~"))
        Assert.assertEquals(listOf(token(TokenType.NOT, "¬", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("¬"))
        Assert.assertEquals(listOf(token(TokenType.AND, "and", 1, 1)
                , token(TokenType.END, "end of input", 1, 4)), tokenize("and"))
        Assert.assertEquals(listOf(token(TokenType.AND, "&", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("&"))
        Assert.assertEquals(listOf(token(TokenType.AND, "∧", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("∧"))
        Assert.assertEquals(listOf(token(TokenType.OR, "or", 1, 1)
                , token(TokenType.END, "end of input", 1, 3)), tokenize("or"))
        Assert.assertEquals(listOf(token(TokenType.OR, "|", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("|"))
        Assert.assertEquals(listOf(token(TokenType.OR, "∨", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("∨"))
        Assert.assertEquals(listOf(token(TokenType.IMPLIES, "implies", 1, 1)
                , token(TokenType.END, "end of input", 1, 8)), tokenize("implies"))
        Assert.assertEquals(listOf(token(TokenType.IMPLIES, "->", 1, 1)
                , token(TokenType.END, "end of input", 1, 3)), tokenize("->"))
        Assert.assertEquals(listOf(token(TokenType.IMPLIES, "→", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("→"))
        Assert.assertEquals(listOf(token(TokenType.IFF, "iff", 1, 1)
                , token(TokenType.END, "end of input", 1, 4)), tokenize("iff"))
        Assert.assertEquals(listOf(token(TokenType.IFF, "<=>", 1, 1)
                , token(TokenType.END, "end of input", 1, 4)), tokenize("<=>"))
        Assert.assertEquals(listOf(token(TokenType.IFF, "⇔", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("⇔"))
        Assert.assertEquals(listOf(token(TokenType.FORALL, "forall", 1, 1)
                , token(TokenType.END, "end of input", 1, 7)), tokenize("forall"))
        Assert.assertEquals(listOf(token(TokenType.FORALL, "∀", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("∀"))
        Assert.assertEquals(listOf(token(TokenType.EXISTS, "exists", 1, 1)
                , token(TokenType.END, "end of input", 1, 7)), tokenize("exists"))
        Assert.assertEquals(listOf(token(TokenType.EXISTS, "∃", 1, 1)
                , token(TokenType.END, "end of input", 1, 2)), tokenize("∃"))

        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 1),
                token(TokenType.END, "end of input", 2, 2)
        ), tokenize("P(x,            y     \n)"))
        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 3),
                token(TokenType.END, "end of input", 2, 4)
        ), tokenize("P(x,            y     \n  )"))
        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 3),
                token(TokenType.END, "end of input", 2, 4)
        ), tokenize("P(x,            y     \r\n  )"))
        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 3),
                token(TokenType.END, "end of input", 2, 4)
        ), tokenize("P(x,            y     \r  )"))
        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 4, 3),
                token(TokenType.END, "end of input", 4, 4)
        ), tokenize("P(x,            y     \n\n  \n  )"))
        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.COMMA, ",", 1, 18),
                token(TokenType.LOWER, "z", 3, 1),
                token(TokenType.RPAREN, ")", 5, 3),
                token(TokenType.END, "end of input", 5, 4)
        ), tokenize("P(x,            y,    \n\nz \r\r\n  )"))

        Assert.assertEquals(listOf(
                token(TokenType.FORALL, "forall", 1, 1),
                token(TokenType.LOWER, "x", 1, 8),
                token(TokenType.DOT, ".", 1, 9),
                token(TokenType.LPAREN, "(", 1, 11),
                token(TokenType.EXISTS, "exists", 1, 12),
                token(TokenType.LOWER, "y", 1, 19),
                token(TokenType.DOT, ".", 1, 20),
                token(TokenType.UPPER, "P", 1, 22),
                token(TokenType.LPAREN, "(", 1, 23),
                token(TokenType.LOWER, "x", 1, 24),
                token(TokenType.COMMA, ",", 1, 25),
                token(TokenType.LOWER, "y", 1, 27),
                token(TokenType.RPAREN, ")", 1, 28),
                token(TokenType.RPAREN, ")", 1, 29),
                token(TokenType.END, "end of input", 1, 30)
        ), tokenize("forall x. (exists y. P(x, y))"))
        Assert.assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.RPAREN, ")", 1, 4),
                token(TokenType.IMPLIES, "->", 1, 6),
                token(TokenType.UPPER, "Q", 1, 9),
                token(TokenType.LPAREN, "(", 1, 10),
                token(TokenType.LOWER, "y", 1, 11),
                token(TokenType.RPAREN, ")", 1, 12),
                token(TokenType.IMPLIES, "→", 1, 14),
                token(TokenType.UPPER, "R", 1, 16),
                token(TokenType.LPAREN, "(", 1, 17),
                token(TokenType.LOWER, "z", 1, 18),
                token(TokenType.RPAREN, ")", 1, 19),
                token(TokenType.END, "end of input", 1, 20)
        ), tokenize("P(x) -> Q(y) → R(z)"))
        Assert.assertEquals(listOf(
                token(TokenType.NOT, "¬", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.EXISTS, "∃", 1, 3),
                token(TokenType.LOWER, "x", 1, 4),
                token(TokenType.DOT, ".", 1, 5),
                token(TokenType.FORALL, "∀", 1, 6),
                token(TokenType.LOWER, "y", 1, 7),
                token(TokenType.DOT, ".", 1, 8),
                token(TokenType.LPAREN, "(", 1, 9),
                token(TokenType.UPPER, "P", 1, 10),
                token(TokenType.LPAREN, "(", 1, 11),
                token(TokenType.LOWER, "x", 1, 12),
                token(TokenType.RPAREN, ")", 1, 13),
                token(TokenType.AND, "∧", 1, 15),
                token(TokenType.UPPER, "Q", 1, 17),
                token(TokenType.LPAREN, "(", 1, 18),
                token(TokenType.LOWER, "y", 1, 19),
                token(TokenType.RPAREN, ")", 1, 20),
                token(TokenType.RPAREN, ")", 1, 21),
                token(TokenType.IMPLIES, "→", 1, 23),
                token(TokenType.FORALL, "∀", 1, 25),
                token(TokenType.LOWER, "x", 1, 26),
                token(TokenType.DOT, ".", 1, 27),
                token(TokenType.EXISTS, "∃", 1, 28),
                token(TokenType.LOWER, "y", 1, 29),
                token(TokenType.DOT, ".", 1, 30),
                token(TokenType.LPAREN, "(", 1, 31),
                token(TokenType.NOT, "¬", 1, 32),
                token(TokenType.UPPER, "P", 1, 33),
                token(TokenType.LPAREN, "(", 1, 34),
                token(TokenType.LOWER, "x", 1, 35),
                token(TokenType.RPAREN, ")", 1, 36),
                token(TokenType.OR, "∨", 1, 38),
                token(TokenType.NOT, "¬", 1, 40),
                token(TokenType.UPPER, "Q", 1, 41),
                token(TokenType.LPAREN, "(", 1, 42),
                token(TokenType.LOWER, "y", 1, 43),
                token(TokenType.RPAREN, ")", 1, 44),
                token(TokenType.RPAREN, ")", 1, 45),
                token(TokenType.END, "end of input", 1, 46)
        ), tokenize("¬(∃x.∀y.(P(x) ∧ Q(y)) → ∀x.∃y.(¬P(x) ∨ ¬Q(y))"))
    }
}

internal class ParserTest {
    @Test
    fun tempTest() {
    }

    @Test
    fun parse() {
        assertTheoriesEqual(actual = "".parseTheory())
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
        assertTheoriesEqual(P(a)
                , actual = "P('a)".parseTheory())
        assertTheoriesEqual(P(x, y)
                , actual = "P(x,y)".parseTheory())
        assertTheoriesEqual(P(a, b)
                , actual = "P('a,'b)".parseTheory())
        assertTheoriesEqual(P(a, x)
                , actual = "P('a,x)".parseTheory())
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
        assertTheoriesEqual(a equals b
                , actual = "'a = 'b".parseTheory())
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
        assertTheoriesEqual(P(x) iff Q(x)
                , actual = "P(x) iff Q(x)".parseTheory())
        assertTheoriesEqual((P(x) iff Q(x)) iff R(x)
                , actual = "P(x) iff Q(x) <=> R(x)".parseTheory())
        assertTheoriesEqual(P(x) iff (Q(x) iff R(x))
                , actual = "P(x) iff (Q(x) <=> R(x))".parseTheory())
        assertTheoriesEqual(P(x) iff ((Q(x) iff R(x)) iff Q(z))
                , actual = "P(x) iff (Q(x) <=> R(x) <=> Q(z))".parseTheory())
        assertTheoriesEqual((P(x) iff Q(x)) implies R(x)
                , actual = "P(x) iff Q(x) implies R(x)".parseTheory())
        assertTheoriesEqual((P(x) implies Q(x)) iff R(x)
                , actual = "P(x) implies Q(x) iff R(x)".parseTheory())
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
        assertTheoriesEqual(P(x) or (Q(y) or R(z))
                , actual = "P(x) or Q(y) or R(z)".parseTheory())
        assertTheoriesEqual((P(x) or Q(y)) or R(z)
                , actual = "(P(x) or Q(y)) or R(z)".parseTheory())
        assertTheoriesEqual(P(x) or (Q(y) or (R(z) or S(z)))
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
        assertTheoriesEqual(P(x) and (Q(y) and R(z))
                , actual = "P(x) and Q(y) and R(z)".parseTheory())
        assertTheoriesEqual(P(w) and (Q(x) and (R(y) and S(z)))
                , actual = "P(w) and Q(x) and R(y) and S(z)".parseTheory())
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
        assertTheoriesEqual("∀ x. (∃ y. (((x = y) ∧ ¬P(y)) ∨ (Q(x) → R(y))))".parseTheory().formulas.first()
                , actual = "forall x . exists y . (x = y and not P(y)) or (Q(x) implies R(y))".parseTheory())
        assertTheoriesEqual(actual = "// test comment\n".parseTheory())
        assertTheoriesEqual(P(x), actual = "// P(x)\nP(x)".parseTheory())
        assertTheoriesEqual(P(x), actual = "// // P(x)\nP(x)".parseTheory())
        assertTheoriesEqual(P(x), P(x), actual = "P(x)// P(x)\nP(x)".parseTheory())

        assertTheoriesEqual(actual = "/* test comment*/\n".parseTheory())
        assertTheoriesEqual(actual = "/* test comment\n".parseTheory())
        assertTheoriesEqual(P(x), actual = "/* P(x)\n\n\nP(x)*/ P(x)".parseTheory())
        assertTheoriesEqual(P(x), actual = "/* /* P(x)\n*/P(x)".parseTheory())
        assertTheoriesEqual(P(x), P(x), actual = "P(x)/* P(x)\nP(x) */ P(x)".parseTheory())
    }

    @Test
    fun parseError() {
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','(' but 'T' is found.", { "T".parseTheory() })
        assertFailure("Parse error at (1, 8): expecting 'lowercase identifier' but '.' is found.", { "forall . P(x)".parseTheory() })
        assertFailure("Parse error at (1, 10): expecting '.' but 'P' is found.", { "forall x P(x)".parseTheory() })
        assertFailure("Parse error at (1, 10): expecting 'lowercase identifier' but '.' is found.", { "forall x,. P(x)".parseTheory() })
        assertFailure("Parse error at (1, 15): expecting '.' but 'Q' is found.", { "P(x) forall x Q(x)".parseTheory() })
        assertFailure("Parse error at (1, 6): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but '∨' is found.", { "P(x) | x".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','=','uppercase identifier','(' but 'x' is found.", { "x | P(x)".parseTheory() })
        assertFailure("Parse error at (1, 6): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but '∨' is found.", { "P(x) | Q".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','(' but 'Q' is found.", { "Q|P(x)".parseTheory() })
        assertFailure("Parse error at (1, 2): expecting one of '¬','⊤','⟘','=','uppercase identifier','(','∃','∀' but 'x' is found.", { "~x".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''',')','(' but 'P' is found.", { "P(')".parseTheory() })
        assertFailure("Parse error at (1, 2): expecting one of '¬','⊤','⟘','lowercase identifier',''','uppercase identifier','∃','∀','(' but '(' is found.", { "~(".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but '(' is found.", { "()".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but ')' is found.", { ")".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting one of '¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(','∃','∀' but '∨' is found.", { "~ or".parseTheory() })
        assertFailure("Parse error at (1, 3): expecting one of '¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(','∃','∀' but '∨' is found.", { "~ |".parseTheory() })
        assertFailure("Parse error at (1, 6): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but '∨' is found.", { "P(x) or ".parseTheory() })
        assertFailure("Parse error at (1, 6): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but '∧' is found.", { "P(x) and ".parseTheory() })
        assertFailure("Parse error at (1, 5): expecting one of '¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(','∃','∀' but 'end of input' is found.", { "not ".parseTheory() })
        assertFailure("Parse error at (1, 2): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','uppercase identifier','(' but '∧' is found.", { " and Q(x)".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','=','uppercase identifier','(' but 'f' is found.", { "f(x, g(y), = h(x)".parseTheory() })
        assertFailure("Parse error at (1, 1): expecting one of 'end of input','∃','∀','¬','⊤','⟘','=','uppercase identifier','(' but 'f' is found.", { "f(x, g(y) = h(x)".parseTheory() })
        assertFailure("Parse error at (2, 2): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','(' but 'T' is found.", { "\r\n T".parseTheory() })
        assertFailure("Parse error at (2, 2): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','(' but 'T' is found.", { "//\n T".parseTheory() })
        assertFailure("Parse error at (3, 10): expecting one of 'end of input','∃','∀','¬','⊤','⟘','lowercase identifier',''','(' but 'T' is found.", { "/*\n\n test */ T".parseTheory() })
    }
}