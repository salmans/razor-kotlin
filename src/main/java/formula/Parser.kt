package formula

import combinator.*
import parser.*
import tools.Either
import java.util.*
import java.util.regex.Pattern

/**
 * Token Type
 */
enum class TokenType(vararg regex: String) {
    WHITESPACE("( |\\t|\\f|(\\r\\n)|\\r|\\n)") {
        override fun toString(): String = "whitespace"
    },
    COMMA(",") {
        override fun toString(): String = ","
    },
    DOT("\\.") {
        override fun toString(): String = "."
    },
    APOSTROPHE("\\'") {
        override fun toString(): String = "'"
    },
    LPAREN("\\(") {
        override fun toString(): String = "("
    },
    RPAREN("\\)") {
        override fun toString(): String = ")"
    },
    EQUALS("\\=") {
        override fun toString(): String = "="
    },
    TRUE("TRUE", "⊤") {
        override fun toString(): String = "⊤"
    },
    FALSE("FALSE", "⟘") {
        override fun toString(): String = "⟘"
    },
    NOT("not", "\\~", "¬") {
        override fun toString(): String = "¬"
    },
    AND("and", "\\&", "∧") {
        override fun toString(): String = "∧"
    },
    OR("or", "\\|", "∨") {
        override fun toString(): String = "∨"
    },
    IMPLIES("implies", "->", "→") {
        override fun toString(): String = "→"
    },
    IFF("iff", "<=>", "⇔") {
        override fun toString(): String = "⇔"
    },
    FORALL("forall", "∀") {
        override fun toString(): String = "∀"
    },
    EXISTS("exists", "∃") {
        override fun toString(): String = "∃"
    },
    LOWER("[a-z_][a-zA-Z0-9_]*") {
        override fun toString(): String = "lowercase identifier"
    },
    UPPER("[A-Z][a-zA-Z0-9_]*") {
        override fun toString(): String = "uppercase identifier"
    },
    COMMENT("\\/\\/[^\\r\\n]*[\\r\\n]?", "\\/\\*([^*]|\\*+[^*/])*\\**\\*\\/", "\\/\\*([^*]|\\*+[^*/])*") {
        override fun toString(): String = "comment"
    },
    END("^$") {
        override fun toString(): String = "end of input"
    },
    INVALID("[^\\s]+") {
        override fun toString(): String = "invalid token"
    };

    /**
     * The pattern by which the token is detected.
     */
    val pattern: Pattern = Pattern.compile("^(${regex.joinToString(separator = "|")})")
}

/**
 * Token
 */
data class Token(val type: TokenType, val token: String, val location: Location) {
    /**
     * Location of tokens
     */
    data class Location(private val line: Int, private val column: Int) {
        override fun toString(): String = "($line, $column)"
    }
}

/**
 * Tokenizes the input {@code source} according to {@link #TokenType}
 */
fun tokenize(source: String): List<Token> {
    /**
     * List of tokens to return
     */
    val tokens = LinkedList<Token>()

    /**
     * Line number
     */
    var line = 1

    /**
     * Column number
     */
    var column = 1

    /**
     * Source to tokenize
     */
    var src = source

    do {
        var match = false
        var isEnd = false

        for (type in TokenType.values()) {
            val m = type.pattern.matcher(src)
            if (m.find()) {
                match = true
                isEnd = type == TokenType.END
                val tok = if (isEnd) {
                    TokenType.END.toString()
                } else m.group()
                if (type == TokenType.WHITESPACE || type == TokenType.COMMENT) {
                    var lastCarriageReturn = false
                    tok.forEach {
                        // \r\n, \r or \n indicate line breaks.
                        when (it) {
                            '\r' -> {
                                line++; column = 1; lastCarriageReturn = true
                            }
                            '\n' -> if (!lastCarriageReturn) {
                                line++; column = 1; lastCarriageReturn = false
                            }
                            else -> {
                                column++; lastCarriageReturn = false
                            }
                        }
                    }
                } else {
                    tokens.add(Token(type, tok.trim(), Token.Location(line, column)))
                    column += tok.length
                }
                src = m.replaceFirst("")
                break
            }
        }
        // TODO use parser exception
        if (!match) throw RuntimeException("Unexpected token at: $src")
    } while (!isEnd)

    return tokens
}

fun expect(type: TokenType) = { tokens: Sequence<Token> ->
    tokens.firstOrNull().let {
        when {
            it == null -> Either.left(UnexpectedEndOfInputFailure(type.toString())) to tokens
            it.type == type -> Either.right(it) to tokens.drop(1)
            else -> {
                Either.left(UnexpectedTokenFailure(Token(type, "", Token.Location(0, 0)))) to tokens
            }
        }
    }
}

fun <R> parens(parser: Parser<Token, R>): Parser<Token, R> = between(expect(TokenType.LPAREN), expect(TokenType.RPAREN), parser)

/**
 * Parses the input source and returns a first-order theory.
 */
fun parse(source: String): Theory {
    val tokens = tokenize(source).asSequence()
    return manyTill(parseFormula(), expect(TokenType.END))(tokens).let {
        when (it.first) {
            is Either.Left -> throw reportError(it.first.left()!!, it.second.firstOrNull())
            is Either.Right -> Theory(it.first.right()!!)
        }
    }
}

private fun reportError(value: ParserFailure, found: Token?): Exception = when (value) {
    is UnexpectedTokenFailure<*> -> {
        val tokens = value.tokens
        val message = if (tokens.size == 1) {
            "Parse error at ${found?.location ?: "unknown"}: expecting '${(tokens[0] as Token).type}' but '${printToken(found)}' is found."
        } else {
            "Parse error at ${found?.location ?: "unknown"}: " +
                    "expecting one of ${tokens.joinToString(",") { "'${(it as Token).type}'" }} but '${printToken(found)}' is found."
        }
        Exception(message)
    }
    is UnexpectedEndOfInputFailure<*> -> {
        val message = when (value.tokens.size) {
            0 -> "Unexpected 'end of input' was found."
            1 -> "Expecting '${value.tokens[0]}' but 'end of input' was found."
            else -> "Expecting one of ${value.tokens.joinToString(",") { "'$it'" }} but 'end of input' was found."
        }
        Exception(message)
    }
    is UnexpectedFailure -> Exception("Unexpected ${value.message}")
}

private fun printToken(token: Token?): String = if (token != null) {
    when (token.type) {
        TokenType.WHITESPACE -> " "
        TokenType.LOWER -> token.token
        TokenType.UPPER -> token.token
        TokenType.INVALID -> token.token
        else -> token.type.toString()
    }
} else {
    "unknown"
}

/**
 * Formula = Qualified (IMPLIES Qualified)*
 */
fun parseFormula(): Parser<Token, Formula> = run {
    fun makeFormula(connective: Token): Parser<Token, (Formula, Formula) -> Formula> {
        return if (connective.type == TokenType.IFF) {
            give({ l, r -> Iff(l, r) })
        } else {
            give({ l, r -> Implies(l, r) })
        }
    }

    chainl1(parseQuantified(), (expect(TokenType.IMPLIES) or expect(TokenType.IFF)){ makeFormula(it) })
}


/**
 * Qualified = EXISTS Vars DOT Qualified
 * Qualified = FORALL Vars DOT Qualified
 * Qualified = Or
 */
fun parseQuantified(): Parser<Token, Formula> = run {
    fun makeFormula(quantifier: Token, vars: Vars, formula: Formula): Parser<Token, Formula> {
        return when (quantifier.type) {
            TokenType.EXISTS -> give<Token, Formula>(Exists(vars, formula))
            TokenType.FORALL -> give<Token, Formula>(Forall(vars, formula))
            else -> throw RuntimeException("unreachable")
        }
    }

    fun helper(quantifier: Token): Parser<Token, Formula> {
        return (parseVars() left expect(TokenType.DOT) and parseQuantified()) { (vars, formula) ->
            makeFormula(quantifier, vars, formula)
        }
    }

    ((expect(TokenType.EXISTS) or expect(TokenType.FORALL)) { quantifier ->
        helper(quantifier)
    } or parseOr())
}


/**
 * Vars = Var (COMMA Var)*
 */
fun parseVars(): Parser<Token, Vars> = sepBy1(parseVar(), expect(TokenType.COMMA))

/**
 * Var = LOWER
 */
fun parseVar(): Parser<Token, Var> = run {
    fun makeVar(name: String): Parser<Token, Var> = give(Var(name))

    (expect(TokenType.LOWER)) { makeVar(it.token) }
}

/**
 * Const = APOSTROPHE LOWER
 */
fun parseConst(): Parser<Token, Const> = run {
    fun makeConst(name: String): Parser<Token, Const> = give(Const(name))

    (expect(TokenType.APOSTROPHE) right expect(TokenType.LOWER)) { makeConst(it.token) }
}

/**
 * Or = And OR Quantified
 * Or = And
 */
fun parseOr(): Parser<Token, Formula> = run {
    fun makeFormula(l: Formula, r: Formula): Parser<Token, Formula> = give(Or(l, r))

    fun helper(l: Formula): Parser<Token, Formula> = attempt((expect(TokenType.OR) right parseQuantified()) { r ->
        makeFormula(l, r)
    }) or give(l)

    (parseAnd()) { l -> helper(l) }
}

/**
 * And = Not AND Quantified
 * And = Not AND And
 * And = Not
 */
fun parseAnd(): Parser<Token, Formula> = run {
    fun makeFormula(l: Formula, r: Formula): Parser<Token, Formula> = give(And(l, r))

    fun helper(l: Formula): Parser<Token, Formula> = attempt(expect(TokenType.AND) right (parseAnd() or parseQuantified()) { r ->
        makeFormula(l, r)
    }) or give(l)

    (parseNot()) { l -> helper(l) }
}

/**
 * Not = NOT Quantified
 * Not = Atom
 */
fun parseNot(): Parser<Token, Formula> = { tokens: Sequence<Token> ->
    fun makeFormula(formula: Formula): Parser<Token, Formula> = give<Token, Formula>(Not(formula))

    ((expect(TokenType.NOT) right (parseNot() or parseQuantified()) { makeFormula(it) }) or parseAtom())(tokens)
}

/**
 * Parse an atomic formula
 * Atom = TRUE
 * Atom = FALSE
 * Atom = Term EQUALS Term
 * Atom = UPPER LPAREN Terms RPAREN
 * Atom = LPAREN Formula RPAREN
 */
fun parseAtom(): Parser<Token, Formula> = run {
    fun makeAtomic(pred: Pred, terms: Terms): Parser<Token, Formula> = give<Token, Formula>(Atom(pred, terms))
    fun atomicHelper(pred: Pred): Parser<Token, Formula> = (parens(parseTerms())) { terms -> makeAtomic(pred, terms) }

    //fun atomic(): Parser<Token, Formula> =

    choice(listOf(
            expect(TokenType.TRUE) right give(TRUE),
            expect(TokenType.FALSE) right give(FALSE),
            parseEquals(),
            (expect(TokenType.UPPER)) { pred -> atomicHelper(Pred(pred.token)) },
            parens(parseFormula())))
}

fun parseEquals(): Parser<Token, Equals> = run {
    fun makeEquals(first: Term, second: Term): Parser<Token, Equals> = give(Equals(first, second))

    ((parseTerm() left expect(TokenType.EQUALS)) and parseTerm()) { makeEquals(it.first, it.second) }
}

/**
 * Terms = Term*
 */
fun parseTerms(): Parser<Token, Terms> = sepEndBy(parseTerm(), expect(TokenType.COMMA))

/**
 * Term = Var
 * Term = Const
 * Term = LOWER LPAREN Terms RPAREN
 */
fun parseTerm(): Parser<Token, Term> = { tokens: Sequence<Token> ->
    fun makeComplex(func: Func, terms: Terms): Parser<Token, Term> = give(App(func, terms))

    fun complex(): Parser<Token, Term> =
            (expect(TokenType.LOWER) and parens(parseTerms())) { (f, ts) ->
                makeComplex(Func(f.token), ts)
            }

    choice(listOf(
            complex(),
            parseVar(),
            parseConst()))(tokens)
}

fun String.parseTheory() = parse(this)