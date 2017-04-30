package Formula

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TokenizerTest {
    @Test
    fun loc() {
        assertEquals(loc(1, 2), Token.Location(1, 2))
        assertEquals(loc(101, 102), Token.Location(101, 102))
    }

    @Test
    fun token() {
        assertEquals(token(TokenType.LOWER, "x"), Token(TokenType.LOWER, "x", Token.Location(0, 0)))
        assertEquals(token(TokenType.UPPER, "P"), Token(TokenType.UPPER, "P", Token.Location(0, 0)))
        assertEquals(token(TokenType.COMMA, ","), Token(TokenType.COMMA, ",", Token.Location(0, 0)))
        assertEquals(token(TokenType.DOT, "."), Token(TokenType.DOT, ".", Token.Location(0, 0)))
        assertEquals(token(TokenType.LPAREN, "("), Token(TokenType.LPAREN, "(", Token.Location(0, 0)))
        assertEquals(token(TokenType.RPAREN, ")"), Token(TokenType.RPAREN, ")", Token.Location(0, 0)))
        assertEquals(token(TokenType.EQUALS, "="), Token(TokenType.EQUALS, "=", Token.Location(0, 0)))
        assertEquals(token(TokenType.TRUE, "TRUE"), Token(TokenType.TRUE, "TRUE", Token.Location(0, 0)))
        assertEquals(token(TokenType.TRUE, "FALSE"), Token(TokenType.TRUE, "FALSE", Token.Location(0, 0)))
        assertEquals(token(TokenType.NOT, "not"), Token(TokenType.NOT, "not", Token.Location(0, 0)))
        assertEquals(token(TokenType.AND, "and"), Token(TokenType.AND, "and", Token.Location(0, 0)))
        assertEquals(token(TokenType.OR, "or"), Token(TokenType.OR, "or", Token.Location(0, 0)))
        assertEquals(token(TokenType.IMPLIES, "implies"), Token(TokenType.IMPLIES, "implies", Token.Location(0, 0)))
        assertEquals(token(TokenType.FORALL, "forall"), Token(TokenType.FORALL, "forall", Token.Location(0, 0)))
        assertEquals(token(TokenType.EXISTS, "exists"), Token(TokenType.EXISTS, "exists", Token.Location(0, 0)))
    }

    @Test
    fun tokenize() {
        assertEquals(listOf(token(TokenType.LOWER, "x", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("x").tokenize())
        assertEquals(listOf(token(TokenType.UPPER, "P", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("P").tokenize())
        assertEquals(listOf(token(TokenType.COMMA, ",", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer(",").tokenize())
        assertEquals(listOf(token(TokenType.DOT, ".", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer(".").tokenize())
        assertEquals(listOf(token(TokenType.LPAREN, "(", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("(").tokenize())
        assertEquals(listOf(token(TokenType.RPAREN, ")", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer(")").tokenize())
        assertEquals(listOf(token(TokenType.EQUALS, "=", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("=").tokenize())
        assertEquals(listOf(token(TokenType.TRUE, "TRUE", 1, 1)
                , token(TokenType.END, "", 1, 5)), Tokenizer("TRUE").tokenize())
        assertEquals(listOf(token(TokenType.TRUE, "⊤", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("⊤").tokenize())
        assertEquals(listOf(token(TokenType.FALSE, "FALSE", 1, 1)
                , token(TokenType.END, "", 1, 6)), Tokenizer("FALSE").tokenize())
        assertEquals(listOf(token(TokenType.FALSE, "⟘", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("⟘").tokenize())
        assertEquals(listOf(token(TokenType.NOT, "not", 1, 1)
                , token(TokenType.END, "", 1, 4)), Tokenizer("not").tokenize())
        assertEquals(listOf(token(TokenType.NOT, "~", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("~").tokenize())
        assertEquals(listOf(token(TokenType.NOT, "¬", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("¬").tokenize())
        assertEquals(listOf(token(TokenType.AND, "and", 1, 1)
                , token(TokenType.END, "", 1, 4)), Tokenizer("and").tokenize())
        assertEquals(listOf(token(TokenType.AND, "&", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("&").tokenize())
        assertEquals(listOf(token(TokenType.AND, "∧", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("∧").tokenize())
        assertEquals(listOf(token(TokenType.OR, "or", 1, 1)
                , token(TokenType.END, "", 1, 3)), Tokenizer("or").tokenize())
        assertEquals(listOf(token(TokenType.OR, "|", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("|").tokenize())
        assertEquals(listOf(token(TokenType.OR, "∨", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("∨").tokenize())
        assertEquals(listOf(token(TokenType.IMPLIES, "implies", 1, 1)
                , token(TokenType.END, "", 1, 8)), Tokenizer("implies").tokenize())
        assertEquals(listOf(token(TokenType.IMPLIES, "->", 1, 1)
                , token(TokenType.END, "", 1, 3)), Tokenizer("->").tokenize())
        assertEquals(listOf(token(TokenType.IMPLIES, "→", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("→").tokenize())
        assertEquals(listOf(token(TokenType.FORALL, "forall", 1, 1)
                , token(TokenType.END, "", 1, 7)), Tokenizer("forall").tokenize())
        assertEquals(listOf(token(TokenType.FORALL, "∀", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("∀").tokenize())
        assertEquals(listOf(token(TokenType.EXISTS, "exists", 1, 1)
                , token(TokenType.END, "", 1, 7)), Tokenizer("exists").tokenize())
        assertEquals(listOf(token(TokenType.EXISTS, "∃", 1, 1)
                , token(TokenType.END, "", 1, 2)), Tokenizer("∃").tokenize())

        assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 1),
                token(TokenType.END, "", 2, 2)
        ), Tokenizer("P(x,            y     \n)").tokenize())
        assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 3),
                token(TokenType.END, "", 2, 4)
        ), Tokenizer("P(x,            y     \n  )").tokenize())
        assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 3),
                token(TokenType.END, "", 2, 4)
        ), Tokenizer("P(x,            y     \r\n  )").tokenize())
        assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 2, 3),
                token(TokenType.END, "", 2, 4)
        ), Tokenizer("P(x,            y     \r  )").tokenize())
        assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.RPAREN, ")", 4, 3),
                token(TokenType.END, "", 4, 4)
        ), Tokenizer("P(x,            y     \n\n  \n  )").tokenize())
        assertEquals(listOf(
                token(TokenType.UPPER, "P", 1, 1),
                token(TokenType.LPAREN, "(", 1, 2),
                token(TokenType.LOWER, "x", 1, 3),
                token(TokenType.COMMA, ",", 1, 4),
                token(TokenType.LOWER, "y", 1, 17),
                token(TokenType.COMMA, ",", 1, 18),
                token(TokenType.LOWER, "z", 3, 1),
                token(TokenType.RPAREN, ")", 5, 3),
                token(TokenType.END, "", 5, 4)
        ), Tokenizer("P(x,            y,    \n\nz \r\r\n  )").tokenize())

        assertEquals(listOf(
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
                token(TokenType.END, "", 1, 30)
        ), Tokenizer("forall x. (exists y. P(x, y))").tokenize())
        assertEquals(listOf(
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
                token(TokenType.END, "", 1, 20)
        ), Tokenizer("P(x) -> Q(y) → R(z)").tokenize())
        assertEquals(listOf(
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
                token(TokenType.END, "", 1, 46)
        ), Tokenizer("¬(∃x.∀y.(P(x) ∧ Q(y)) → ∀x.∃y.(¬P(x) ∨ ¬Q(y))").tokenize())
    }
}