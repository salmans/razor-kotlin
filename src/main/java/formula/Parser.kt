package formula

import combinator.and
import combinator.many
import combinator.optional
import combinator.or
import parser.*
import parser.Parser
import tools.Either
import tools.compose
import tools.identity
import java.util.*
import java.util.regex.Pattern

/**
 * Token Type
 */
enum class TokenType(vararg regex: String) {
    WHITESPACE("( |\\t|\\f|(\\r\\n)|\\r|\\n)") {
        override fun toString(): String = "<Whitespace>"
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
        override fun toString(): String = "<Lowercase Identifier>"
    },
    UPPER("[A-Z][a-zA-Z0-9_]*") {
        override fun toString(): String = "<Uppercase Identifier>"
    },
    COMMENT("\\/\\/[^\\r\\n]*[\\r\\n]?", "\\/\\*([^*]|\\*+[^*/])*\\**\\*\\/", "\\/\\*([^*]|\\*+[^*/])*") {
        override fun toString(): String = "<Comment>"
    },
    END("^$") {
        override fun toString(): String = "<End of Input>"
    },
    INVALID("[^\\s]+") {
        override fun toString(): String = "<Invalid Token>"
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
    var line: Int = 1

    /**
     * Column number
     */
    var column: Int = 1

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
    if (tokens.firstOrNull()?.type == type)
        Either.right(tokens.first() to tokens.drop(1))
    else {
        val found = if (tokens.firstOrNull() != null) tokens.firstOrNull().toString() else END_OF_SOURCE
        Either.left(UnexpectedTokenException(type.toString(), found = found))
    }
}

fun <T> commaSeparated(parser: Parser<Token, T>): Parser<Token, List<T>> = { v: ParserResult<Token, List<Pair<Token, T>>> ->
    v.mapResult({ r -> r.map { it.second } })
} compose many(expect(TokenType.COMMA) and parser)

/**
 * Parses the input source and returns a first-order theory.
 */
fun parse(source: String): Theory? {
    val tokens = tokenize(source)
    val result = (parseTheory() and expect(TokenType.END))(tokens.asSequence())
    return when (result) {
        is Either.Right -> result.value.first.first
        is Either.Left -> throw result.value
    }

}

fun parseTheory(): Parser<Token, Theory> = {
    many(parseFormula())(it).let {
        it.mapResult { Theory(it) }
    }
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
fun parseQuantified(): Parser<Token, Formula> = {
    (((expect(TokenType.EXISTS) or expect(TokenType.FORALL)) and parseVars() and expect(TokenType.DOT) and parseQuantified()) or parseOr())(it).let {
        it.mapResult {
            when (it) {
                is Either.Right -> it.value
                is Either.Left -> {
                    val quantifier = it.value.first.first.first
                    val vars = it.value.first.first.second
                    val formula = it.value.second
                    when (quantifier) {
                        is Either.Left -> Exists(vars, formula)
                        is Either.Right -> Forall(vars, formula)
                    }
                }
            }
        }
    }
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

fun parseOr(): Parser<Token, Formula> = {
    (parseAnd() and optional(expect(TokenType.OR) and (parseOr() or parseQuantified())))(it).let {
        it.mapResult {
            val first = it.first
            if (it.second != null) {
                val second = when (it.second!!.second) {
                    is Either.Left -> it.second!!.second.left()!!
                    is Either.Right -> it.second!!.second.right()!!
                }
                Or(first, second)
            } else {
                first
            }
        }
    }
}

fun parseAnd(): Parser<Token, Formula> = {
    (parseNot() and optional(expect(TokenType.AND) and (parseAnd() or parseQuantified())))(it).let {
        it.mapResult {
            val first = it.first
            if (it.second != null) {
                val second = when (it.second!!.second) {
                    is Either.Left -> it.second!!.second.left()!!
                    is Either.Right -> it.second!!.second.right()!!
                }
                And(first, second)
            } else {
                first
            }
        }
    }
}

fun parseNot(): Parser<Token, Formula> = {
    ((expect(TokenType.NOT) and (parseNot() or parseQuantified())) or parseAtom())(it).let {
        it.mapResult {
            when (it) {
                is Either.Left -> Not(it.value.second.either({ identity(it) }, { identity(it) }))
                is Either.Right -> it.value
            }
        }
    }
}

fun parseAtom(): Parser<Token, Formula> = {
    (expect(TokenType.TRUE) or expect(TokenType.FALSE) or parseEquals() or
            (expect(TokenType.UPPER) and expect(TokenType.LPAREN) and parseTerms() and expect(TokenType.RPAREN)) or
            (expect(TokenType.LPAREN) and parseFormula() and expect(TokenType.RPAREN)))(it).let {
        it.mapResult {
            when (it) {
                is Either.Left -> it.value.let {
                    when (it) {
                        is Either.Left -> it.value.let {
                            when (it) {
                                is Either.Left -> it.value.let {
                                    when (it) {
                                        is Either.Left -> TRUE
                                        is Either.Right -> FALSE
                                    }
                                }
                                is Either.Right -> it.value
                            }
                        }
                        is Either.Right -> Atom(Pred(it.value.first.first.first.token), it.value.first.second)
                    }
                }
                is Either.Right -> it.value.first.second
            }
        }
    }
}

fun parseEquals(): Parser<Token, Equals> = {
    (parseTerm() and expect(TokenType.EQUALS) and parseTerm())(it).let {
        it.mapResult {
            Equals(it.first.first, it.second)
        }
    }
}

fun parseTerms(): Parser<Token, Terms> = {
    optional(parseTerm() and commaSeparated(parseTerm()))(it).let {
        it.mapResult {
            if (it == null) {
                emptyList()
            } else {
                val first = it.first
                listOf(first) + it.second
            }
        }
    }
}

fun parseTerm(): Parser<Token, Term> = {
    ((expect(TokenType.LOWER) and expect(TokenType.LPAREN) and parseTerms() and expect(TokenType.RPAREN))
            or parseVar()
            or parseConst())(it).let {
        it.mapResult {
            when (it) {
                is Either.Left -> it.value.let {
                    when(it){
                        is Either.Left -> App(Func(it.value.first.first.first.token), it.value.first.second)
                        is Either.Right -> it.value
                    }
                }
                is Either.Right -> it.value
            }
        }
    }
}

fun String.parseTheory() = parse(this)

fun main(args: Array<String>) {
    System.out.println(parse("P() and Q() and R()"))
}