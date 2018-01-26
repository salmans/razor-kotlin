package formula

import combinator.*
import parser.*
import tools.Either
import tools.compose
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
            it == null -> Either.left(UnexpectedEndOfInputException(type.toString())) to tokens
            it.type == type -> Either.right(it) to tokens.drop(1)
            else -> {
                Either.left(UnexpectedTokenException(Token(type, "", Token.Location(0, 0)),
                        found = tokens.firstOrNull())) to tokens
            }
        }
    }
}

fun <T> commaSeparated(parser: Parser<Token, T>): Parser<Token, List<T>> = { v: ParserResult<Token, List<Pair<Token, T>>> ->
    v.mapResult({ r -> r.map { it.second } })
} compose many(expect(TokenType.COMMA) and parser)

/**
 * Parses the input source and returns a first-order theory.
 */
fun parse(source: String): Theory {
    val tokens = tokenize(source).asSequence()
    return manyTill(parseFormula(), expect(TokenType.END))(tokens).first.let {
        when (it) {
            is Either.Left ->throw reportError(it.value)
            is Either.Right -> Theory(it.value)
        }
    }
}

private fun reportError(value: ParserException): Exception {
    when (value) {
        is UnexpectedTokenException<*> -> {
            val found = value.found as Token
            val tokens = value.tokens
            val message = if (tokens.size == 1) {
                "Parse error at ${found.location}: expecting '${(tokens[0] as Token).type}' but '${printToken(found)}' is found."
            } else {
                "Parse error at ${found.location}: " +
                        "expecting one of ${tokens.joinToString(",") { "'${(it as Token).type}'" }} but '${printToken(found)}' is found."
            }
            throw Exception(message)
        }
        else -> throw Exception(value.message)
    }
}

private fun printToken(token: Token): String = when(token.type){
    TokenType.WHITESPACE -> " "
    TokenType.LOWER -> token.token
    TokenType.UPPER -> token.token
    TokenType.END -> "end of input"
    TokenType.INVALID -> token.token
    else -> token.type.toString()
}

/**
 * Formula = Qualified (IMPLIES Qualified)*
 */
// TODO: convert to (Q and optional(=> and F)
fun parseFormula(): Parser<Token, Formula> = {
    (parseQuantified() and many(expect(TokenType.IMPLIES) and parseQuantified()))(it).let {
        it.mapResult {
            val rest = it.second.map { it.second }
            rest.fold(it.first, ::Implies)
        }
    }
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
        return (parseVars() left expect(TokenType.DOT) and parseQuantified()) bind { (vars, formula) ->
            makeFormula(quantifier, vars, formula)
        }
    }

    ((expect(TokenType.EXISTS) or expect(TokenType.FORALL)) bind { quantifier -> helper(quantifier) } or parseOr())
}


/**
 * Vars = Var (COMMA Var)*
 */
fun parseVars(): Parser<Token, Vars> = {
    (parseVar() and commaSeparated(parseVar()))(it).let {
        it.mapResult {
            val (first, rest) = it
            listOf(first) + rest
        }
    }
}

/**
 * Var = LOWER
 */
fun parseVar(): Parser<Token, Var> = {
    expect(TokenType.LOWER)(it).let {
        it.mapResult { Var(it.token) }
    }
}

/**
 * Const = APOSTROPHE LOWER
 */
fun parseConst(): Parser<Token, Const> = {
    (expect(TokenType.APOSTROPHE) and expect(TokenType.LOWER))(it).let {
        it.mapResult { Const(it.second.token) }
    }
}

fun parseOr(): Parser<Token, Formula> = run {
    fun makeFormula(l: Formula, r: Formula): Parser<Token, Formula> = give(Or(l, r))

    fun helper(l: Formula): Parser<Token, Formula> = attempt(expect(TokenType.OR) right (parseOr() or parseQuantified()) bind { r ->
        makeFormula(l, r)
    }) or give(l)

    parseAnd() bind { l -> helper(l) }
}

fun parseAnd(): Parser<Token, Formula> = run {
    fun makeFormula(l: Formula, r: Formula): Parser<Token, Formula> = give(And(l, r))

    fun helper(l: Formula): Parser<Token, Formula> = attempt(expect(TokenType.AND) right (parseAnd() or parseQuantified()) bind { r ->
        makeFormula(l, r)
    }) or give(l)

    parseNot() bind { l -> helper(l) }
}

fun parseNot(): Parser<Token, Formula> = { tokens: Sequence<Token> ->
    fun makeFormula(formula: Formula): Parser<Token, Formula> = give<Token, Formula>(Not(formula))
    ((expect(TokenType.NOT) right (parseNot() or parseQuantified()) bind { makeFormula(it) }) or parseAtom())(tokens)
}

fun parseAtom(): Parser<Token, Formula> = run {
    fun parseTrue(): Parser<Token, Formula> = expect(TokenType.TRUE) right give(TRUE)
    fun parseFalse(): Parser<Token, Formula> = expect(TokenType.FALSE) right give(FALSE)
    fun makeAtomic(pred: Pred, terms: Terms): Parser<Token, Formula> = give<Token, Formula>(Atom(pred, terms))
    fun atomicHelper(pred: Pred): Parser<Token, Formula> = (expect(TokenType.LPAREN) right parseTerms() left expect(TokenType.RPAREN)) bind { terms -> makeAtomic(pred, terms) }

    fun atomic(): Parser<Token, Formula> = expect(TokenType.UPPER) bind { pred -> atomicHelper(Pred(pred.token)) }
    fun parens(): Parser<Token, Formula> = expect(TokenType.LPAREN) right parseFormula() left expect(TokenType.RPAREN)

    choice(listOf(
            parseTrue(),
            parseFalse(),
            parseEquals(),
            atomic(),
            parens()))
}

fun parseEquals(): Parser<Token, Equals> = run {
    fun makeEquals(first: Term, second: Term): Parser<Token, Equals> = give<Token, Equals>(Equals(first, second))
    ((parseTerm() left expect(TokenType.EQUALS)) and parseTerm()) bind { makeEquals(it.first, it.second) }
}

fun parseTerms(): Parser<Token, Terms> = sepEndBy(parseTerm(), expect(TokenType.COMMA))

fun parseTerm(): Parser<Token, Term> = {tokens: Sequence<Token> ->
    fun makeComplex(func: Func, terms: Terms): Parser<Token, Term> = give(App(func, terms))

    fun complex(): Parser<Token, Term> =
            (expect(TokenType.LOWER) left expect(TokenType.LPAREN) and parseTerms() left expect(TokenType.RPAREN)) bind { (f, ts) ->
                makeComplex(Func(f.token), ts)
            }

    choice(listOf(
            complex(),
            parseVar(),
            parseConst()))(tokens)
}

fun String.parseTheory() = parse(this)

fun main(args: Array<String>) {
    System.out.println(parse("exists x . not P(x, y)"))
}