package formula

import java.util.*
import java.util.regex.Pattern

/**
 * Token Type
 */
enum class TokenType(vararg regex: String) {
    COMMA(",") {
        override fun toString(): String = ","
    },
    DOT("\\.") {
        override fun toString(): String = "."
    },
    APOSTROPHE("\\'"){
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
    data class Location(val line: Int, val column: Int) {
        override fun toString(): String = "($line, $column)"
    }
}

fun loc(line: Int, column: Int) = Token.Location(line, column)

fun token(type: TokenType, string: String, line: Int = 0, column: Int = 0) = Token(type, string, loc(line, column))

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

    /**
     * A matcher to match and discard whitespaces. (Whitespace is not a token)
     */
    val whiteSpace = Pattern.compile("^\\s*")

    /**
     * Ignores whitespace characters using {@code whiteSpace}. It updates {@code line} and {@code column} accordingly.
     */
    fun ignoreWhiteSpace() {
        whiteSpace.matcher(src).let {
            if (it.find()) {
                val tok = it.group()
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
                src = it.replaceFirst("")
            }
        }
    }

    do {
        ignoreWhiteSpace()

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
                tokens.add(Token(type, tok.trim(), Token.Location(line, column)))
                column += tok.length
                src = m.replaceFirst("")
                break
            }
        }
        // TODO use parser exception
        if (!match) throw RuntimeException("Unexpected token at: $src")
    } while (!isEnd)

    return tokens
}