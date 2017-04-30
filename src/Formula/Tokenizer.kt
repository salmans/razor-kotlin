package Formula

import java.util.*
import java.util.regex.Pattern

enum class TokenType(regex: List<String>) {
    COMMA(listOf(",")),
    DOT(listOf("\\.")),
    LPAREN(listOf("\\(")),
    RPAREN(listOf("\\)")),
    EQUALS(listOf("\\=")),
    TRUE(listOf("TRUE", "⊤")),
    FALSE(listOf("FALSE", "⟘")),
    NOT(listOf("not", "\\~", "¬")),
    AND(listOf("and", "\\&", "∧")),
    OR(listOf("or", "\\|", "∨")),
    IMPLIES(listOf("implies", "->", "→")),
    FORALL(listOf("forall", "∀")),
    EXISTS(listOf("exists", "∃")),
    LOWER(listOf("[a-z_][a-zA-Z0-9_]*")),
    UPPER(listOf("[A-Z][a-zA-Z0-9_]*")),
    END(listOf("^$"));

    val pattern: Pattern = Pattern.compile("^(${regex.joinToString(separator = "|")})")
}

data class Token(val type: TokenType, val token: String, val location: Location) {
    data class Location(val line: Int, val column: Int)
}

fun loc(line: Int, column: Int) = Token.Location(line, column)

fun token(type: TokenType, string: String, line: Int = 0, column: Int = 0) = Token(type, string, loc(line, column))

class Tokenizer(var source: String) {
    fun tokenize(): List<Token> {
        val tokens = LinkedList<Token>()
        var line: Int = 1
        var column: Int = 1

        val whiteSpace = Pattern.compile("^\\s*")
        tokens.clear()

        do {
            // Ignore whitespace
            whiteSpace.matcher(source).let {
                if (it.find()) {
                    val tok = it.group()
                    var lastCarriageReturn = false
                    tok.forEach {
                        when(it){
                            '\r' -> {line++; column = 1; lastCarriageReturn = true}
                            '\n' -> if (!lastCarriageReturn) {line++; column = 1; lastCarriageReturn = false}
                            else -> {column++; lastCarriageReturn = false}
                        }
                    }
                    source = it.replaceFirst("")
                }
            }

            var match = false
            var isEnd = false
            for (type in TokenType.values()) {
                val m = type.pattern.matcher(source)
                if (m.find()) {
                    match = true
                    isEnd = type == TokenType.END
                    val tok = m.group()
                    tokens.add(Token(type, tok.trim(), Token.Location(line, column)))
                    column += tok.length
                    source = m.replaceFirst("")
                    break
                }
            }
            // TODO use parser exception
            if (!match) throw RuntimeException("Unexpected token at: $source")
        } while(!isEnd)

        return tokens
    }
}