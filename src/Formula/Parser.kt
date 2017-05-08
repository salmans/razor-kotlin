package Formula

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

    val expectedTokenStack = Stack<List<TokenType>>()

    private fun addExpectedToken(types: List<TokenType>) {
        var list = if (expectedTokenStack.empty()) {
            emptyList()
        } else {
            expectedTokenStack.pop()
        }
        list += types
        expectedTokenStack.push(list.distinct())
    }

    /**
     * Returns the next token without consuming it. It returns null if the token is missing.
     */
    fun peek(type: TokenType, vararg types: TokenType): Token? {
        tokens[index].let {
            return when (it.type) {
                type -> it
                else -> if (types.contains(it.type)) {
                    it
                } else {
                    addExpectedToken(listOf(type) + types)
                    null
                } // TODO improve this
            }
        }
    }

    /**
     * Consumes a token of the given type.
     */
    fun consume(type: TokenType): Token {
        // If the parser is correct, consume is always called when a token is left; thus, the result of {@code peek()} is nonnull.
        tokens[index].let {
            if (it.type != type) {
                throw ParserException("Parse error at ${it.location}: expecting '$type' but '${it.token}' is found.")
            }
            index++
            return it
        }
    }

    private fun <T> expect(parser: () -> T?): T {
        parser().let {
            if (it != null) {
                // reset the expected tokens at this level:
                expectedTokenStack.pop()
                expectedTokenStack.push(emptyList())
                return it
            } else {
                tokens[index].let {
                    throw ParserException("Parse error at ${it.location}: " +
                            "expecting ${expectedTokenStack.pop().joinToString { "'$it'" }} but '${it.token}' is found.")
                }
            }
        }
    }

    private fun <T> maybe(parser: () -> T?): T? {
        expectedTokenStack.push(listOf())
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

    private fun <T> commaSeparated(parser: () -> T?): T? {
        when (peek(TokenType.COMMA)?.type) {
            TokenType.COMMA -> {
                consume(TokenType.COMMA)
                return expect { parser() }
            }
            else -> return null
        }
    } // FIXME should this be expected?

    /**
     * Parses the input source and returns a first-order theory.
     */
    fun parse(source: String): Theory? {
        this.tokens = tokenize(source)
        val theory = maybe { parseTheory() }
        consume(TokenType.END)
        return theory
    }

    private fun parseTheory(): Theory? = Theory(many {
        val token1: Token? = tokens[index]
        if (token1!!.type != TokenType.END) {
            return@many expect { parseFormula() }
        }
        return@many null

        // TODO create a list of formulas until parseFormula fails.
    })

    private fun parseFormula(): Formula? {
        val formula = expect { parseQuantified() }
        val formulas = // TODO maybe {
                many {
                    when (peek(TokenType.IMPLIES)?.type) {
                        TokenType.IMPLIES -> {
                            return@many expect {
                                consume(TokenType.IMPLIES)
                                parseQuantified()
                            }
                        }
                        else -> return@many null
                    }
                }
        // }
        //return formulas?.fold(formula, ::Implies) ?: formula
        return formulas.fold(formula, ::Implies)
    }

    private fun parseQuantified(): Formula? {
        when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
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

    private fun parseVars(): List<Var> {
        return expect { parseVar() }.let { listOf(it) + many { commaSeparated { parseVar() } } }
    }

    private fun parseVar(): Var? {
        peek(TokenType.LOWER).let {
            when (it?.type) {
                TokenType.LOWER -> return expect {
                    consume(TokenType.LOWER)
                    Var(it.token)
                }
                else -> return null
            }
        }
    }

    private fun parseOr(): Formula? {
        val formula = parseAnd()!!
        val formulas = many {
            when (peek(TokenType.OR)?.type) {
                TokenType.OR -> return@many expect {
                    consume(TokenType.OR)
                    when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
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

    private fun parseAnd(): Formula? {
        val formula = parseNot()!!
        val formulas = many {
            when (peek(TokenType.AND)?.type) {
                TokenType.AND -> return@many expect {
                    consume(TokenType.AND)
                    when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
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

    private fun parseNot(): Formula? {
        when (peek(TokenType.NOT)?.type) {
            TokenType.NOT -> return expect {
                consume(TokenType.NOT)
                when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
                    TokenType.EXISTS -> Not(expect { parseQuantified() })
                    TokenType.FORALL -> Not(expect { parseQuantified() })
                    else -> Not(expect { parseNot() })
                }
            }
            else -> return expect { parseAtom() }
        }
    }

    private fun parseAtom(): Formula? {
        val token = peek(TokenType.TRUE, TokenType.FALSE, TokenType.LOWER, TokenType.UPPER, TokenType.LPAREN)
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
                val relation = Rel(token.token)
                consume(TokenType.UPPER)
                consume(TokenType.LPAREN)
                val terms = parseTerms()
                consume(TokenType.RPAREN)
                Atom(relation, terms)
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

    private fun parseTerms(): List<Term> {
        return parseTerm()?.let { listOf(it) + many { commaSeparated { parseTerm() } } } ?: emptyList()
    }

    private fun parseTerm(): Term? {
        return when (peek(TokenType.LOWER)?.type) {
            TokenType.LOWER -> {
                val firstToken = consume(TokenType.LOWER)
                when (peek(TokenType.LPAREN)?.type) {
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