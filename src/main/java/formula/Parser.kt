package formula

import java.util.*

class ParserException(message: String) : Exception(message)

class Parser {
    /**
     * List of tokens, obtained by tokenizing the source
     */
    var tokens: List<Token> = emptyList()
        set(value) {
            // resetting the input: reset the index
            index = 0
            field = value
        }

    /**
     * Pointer to the next token.
     */
    var index = 0

    /**
     * A stack of list of expected tokens. When parsing an optional string using {@code maybe()}, a new list
     * is pushed into the stack. A list is popped from the stack when the current string is parsed successfully.
     */
    val expectedTokenStack = Stack<List<TokenType>>()

    /**
     * Adds a list of expected token types to the current level of the stack.
     */
    private fun addExpectedToken(types: List<TokenType>) {
        var list = expectedTokenStack.pop()
        list += types
        expectedTokenStack.push(list.distinct())
    }

    /**
     * Return the next token (if exists).
     */
    fun peek(): Token {
        return tokens[index]
    }

    /**
     * Returns the next token without consuming it, if the next token is of one of the expected types;
     * it returns null if the next token is not of the input expected types.
     */
    fun match(type: TokenType, vararg types: TokenType): Token? {
        peek().let {
            return when {
                it.type == type || types.contains(it.type) -> it
                else -> {
                    addExpectedToken(listOf(type) + types) // add the expected tokens to the current level of the stack
                    null
                }
            }
        }
    }

    /**
     * Consumes a token of the given type.
     */
    fun consume(type: TokenType): Token {
        peek().let {
            if (it.type != type) {
                throw ParserException("Parse error at ${it.location}: expecting '$type' but '${it.token}' is found.")
            }
            index++
            return it
        }
    }

    /**
     * Expects a parser to successfully run on the input tokens. A parse error is generated if the parser fails.
     */
    private fun <T> expect(parser: () -> T?): T {
        parser().let {
            if (it != null) {
                // the parser succeeds: reset the expected tokens at the *current level*
                expectedTokenStack.pop()
                expectedTokenStack.push(emptyList())
                return it
            } else {
                // generate a parser error
                peek().let {
                    throw ParserException("Parse error at ${it.location}: " +
                            "expecting ${expectedTokenStack.pop().joinToString { "'$it'" }} but '${it.token}' is found.")
                }
            }
        }
    }

    /**
     * Runs an optional parser that may fail. This function pushes a new level to the stack of expected tokens that is
     * popped after the input parser runs.
     */
    private fun <T> maybe(parser: () -> T?): T? {
        expectedTokenStack.push(emptyList())
        val result = parser()
        expectedTokenStack.pop()
        return result
    }

    /**
     * For a given parser `parser`, parses `parser*` and return the result in a list.
     */
    private fun <T> many(parser: () -> T?): List<T> {
        var result: List<T> = emptyList()
        while (true) {
            val item = parser() ?: break // failed to apply the parser again
            result += item
        }
        return result
    }

    /**
     * For a given parser `parser`, parses `(, parser)*` until no more commas follow.
     */
    private fun <T> commaSeparated(parser: () -> T?): T? {
        return when (match(TokenType.COMMA)?.type) {
            TokenType.COMMA -> {
                consume(TokenType.COMMA)
                expect { parser() }
            }
            else -> null
        }
    }

    /**
     * Parses the input source and returns a first-order theory.
     */
    fun parse(source: String): Theory? {
        this.tokens = tokenize(source)
        val theory = maybe { parseTheory() }
        consume(TokenType.END)
        return theory
    }

    /**
     * Parse a theory
     * Theory = Formula* END
     */
    private fun parseTheory(): Theory? {
        val formulas = many {
            return@many when (peek().type) {
                TokenType.END -> null
                else -> expect { parseFormula() }
            }
        }
        return formulas.let { Theory(formulas) }
    }

    /**
     * Parse a formula
     * Formula = Quantified (IMPLIES Quantified)*
     */
    private fun parseFormula(): Formula? {
        val formula = expect { parseQuantified() }
        val formulas = many {
            when (match(TokenType.IMPLIES)?.type) {
                TokenType.IMPLIES -> {
                    return@many expect {
                        consume(TokenType.IMPLIES)
                        parseQuantified()
                    }
                }
                else -> return@many null
            }
        }
        return formulas.fold(formula, ::Implies)
    }

    /**
     * Parse a quantified formula
     * Quantified = EXISTS Variables DOT Quantified
     * Quantified = FORALL Variables DOT Quantified
     * Quantified = Or
     */
    private fun parseQuantified(): Formula? {
        when (match(TokenType.EXISTS, TokenType.FORALL)?.type) {
            TokenType.EXISTS -> return expect {
                consume(TokenType.EXISTS)
                val vs = parseVars()
                consume(TokenType.DOT)
                parseQuantified()?.let { Exists(vs, it) }
            }
            TokenType.FORALL -> return expect {
                consume(TokenType.FORALL)
                val vs = parseVars()
                consume(TokenType.DOT)
                parseQuantified()?.let { Forall(vs, it) }
            }
            else -> return expect { parseOr() }
        }
    }

    /**
     * Parse a list of variables
     * Variables = Variable (COMMA Variable)*
     */
    private fun parseVars(): Vars {
        return expect { parseVar() }.let { listOf(it) + many { commaSeparated { parseVar() } } }
    }

    /**
     * Parse a variable
     * Variable = LOWER
     */
    private fun parseVar(): Var? {
        match(TokenType.LOWER).let {
            when (it?.type) {
                TokenType.LOWER -> return expect {
                    consume(TokenType.LOWER)
                    Var(it.token)
                }
                else -> return null
            }
        }
    }

    /**
     * Parse a disjunction
     * Or = And OR Quantified
     * Or = And OR And
     * Or = And
     */
    private fun parseOr(): Formula? {
        val formula = expect { parseAnd() }
        val formulas = many {
            when (match(TokenType.OR)?.type) {
                TokenType.OR -> return@many expect {
                    consume(TokenType.OR)
                    when (match(TokenType.EXISTS, TokenType.FORALL)?.type) {
                        TokenType.EXISTS -> expect { parseQuantified() }
                        TokenType.FORALL -> expect { parseQuantified() }
                        else -> expect { parseAnd() }
                    }
                }
                else -> return@many null
            }
        }
        return formulas.fold(formula, ::Or)
    }

    /**
     * Parse a conjunction
     * And = Not AND Quantified
     * And = Not AND Not
     * And = Not
     */
    private fun parseAnd(): Formula? {
        val formula = expect { parseNot() }
        val formulas = many {
            when (match(TokenType.AND)?.type) {
                TokenType.AND -> return@many expect {
                    consume(TokenType.AND)
                    when (match(TokenType.EXISTS, TokenType.FORALL)?.type) {
                        TokenType.EXISTS -> expect { parseQuantified() }
                        TokenType.FORALL -> expect { parseQuantified() }
                        else -> expect { parseNot() }
                    }
                }
                else -> return@many null
            }
        }
        return formulas.fold(formula, ::And)
    }

    /**
     * Parse a negation
     * Not = NOT Quantified
     * Not = Atom
     */
    private fun parseNot(): Formula? {
        when (match(TokenType.NOT)?.type) {
            TokenType.NOT -> return expect {
                consume(TokenType.NOT)
                when (match(TokenType.EXISTS, TokenType.FORALL)?.type) {
                    TokenType.EXISTS -> Not(expect { parseQuantified() })
                    TokenType.FORALL -> Not(expect { parseQuantified() })
                    else -> Not(expect { parseNot() })
                }
            }
            else -> return expect { parseAtom() }
        }
    }

    /**
     * Parse an atomic formula
     * Atom = TRUE
     * Atom = FALSE
     * Atom = Term EQUALS Term
     * Atom = UPPER LPAREN Terms RPAREN
     * Atom = LPAREN Formula RPAREN
     */
    private fun parseAtom(): Formula? {
        val token = match(TokenType.TRUE, TokenType.FALSE, TokenType.LOWER, TokenType.UPPER, TokenType.LPAREN)
        return when (token?.type) {
            TokenType.TRUE -> expect {
                consume(TokenType.TRUE)
                Top
            }
            TokenType.FALSE -> expect {
                consume(TokenType.FALSE)
                Bottom
            }
            TokenType.LOWER -> expect {
                val term1 = expect { parseTerm() }
                consume(TokenType.EQUALS)
                val term2 = expect { parseTerm() }
                Equals(term1, term2)
            }
            TokenType.UPPER -> expect {
                val pred = Pred(token.token)
                consume(TokenType.UPPER)
                consume(TokenType.LPAREN)
                val terms = parseTerms()
                consume(TokenType.RPAREN)
                Atom(pred, terms)
            }
            TokenType.LPAREN -> expect {
                consume(TokenType.LPAREN)
                val formula = expect { parseFormula() }
                consume(TokenType.RPAREN)
                formula
            }
            else -> null
        }
    }

    /**
     * Parse a list of terms
     * Terms = Term*
     */
    private fun parseTerms(): Terms {
        return parseTerm()?.let { listOf(it) + many { commaSeparated { parseTerm() } } } ?: emptyList()
    }

    /**
     * Parse a term
     * Term = Variable
     * Term = LOWER LPAREN Terms RPAREN
     */
    private fun parseTerm(): Term? {
        return when (match(TokenType.APOSTROPHE, TokenType.LOWER)?.type) {
            TokenType.APOSTROPHE -> {
                consume(TokenType.APOSTROPHE)
                val token = consume(TokenType.LOWER)
                Const(token.token)
            }
            TokenType.LOWER -> {
                val firstToken = consume(TokenType.LOWER)
                when (match(TokenType.LPAREN)?.type) {
                    TokenType.LPAREN -> {
                        consume(TokenType.LPAREN)
                        val terms = parseTerms()
                        consume(TokenType.RPAREN)
                        App(Func(firstToken.token), terms)
                    }
                    else -> Var(firstToken.token) // This is not standard
                }
            }
            else -> null
        }
    }
}

fun String.parseTheory() = Parser().parse(this)