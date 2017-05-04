package Formula

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
     * Returns the next token without consuming it. It returns null if the token is missing.
     */
    fun peek(type: TokenType, vararg types: TokenType): Token? {
        tokens[index].let {
            return when (it.type) {
                type -> it
                else -> if (types.contains(it.type)) it else null // TODO improve this
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
                throw ParserException("Parsing Error at ${it.location}: expecting $type but ${it.token} is found.")
            }
            index++
            return it
        }
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
     * Parses the input source and returns a first-order theory.
     */
    fun parse(source: String): Theory? {
        this.tokens = tokenize(source)
        val theory = parseTheory()
        consume(TokenType.END)
        return theory
    }

    private fun <T> commaSeparated(parser: () -> T?): T? {
        when (peek(TokenType.COMMA)?.type) {
            TokenType.COMMA -> {
                consume(TokenType.COMMA)
                return parser()
            }
            else -> return null
        }
    }

    private fun parseTheory(): Theory? = Theory(many {
        val token1: Token? = tokens[index]
        if (token1!!.type != TokenType.END) {
            return@many parseFormula()
        }
        return@many null
    })

    private fun parseFormula(): Formula? {
        val formula = parseQuantified()!!
        val formulas = many({
            when (peek(TokenType.IMPLIES)?.type) {
                TokenType.IMPLIES -> {
                    consume(TokenType.IMPLIES)
                    return@many parseQuantified()!!
                }
                else -> return@many null
            }
        })
        return formulas.fold(formula, ::Implies)
    }

    private fun parseQuantified(): Formula? {
        when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
            TokenType.EXISTS -> {
                consume(TokenType.EXISTS)
                val vars = parseVars()
                consume(TokenType.DOT)
                val formula = parseQuantified()!!
                return Exists(vars, formula)

            }
            TokenType.FORALL -> {
                consume(TokenType.FORALL)
                val vars = parseVars()
                consume(TokenType.DOT)
                val formula = parseQuantified()!!
                return Forall(vars, formula)
            }
            else -> return parseOr()!!
        }
    }

    private fun parseVars(): List<Var> {
        parseVar().let {
            if (it != null) {
                return listOf(it) + many({ commaSeparated { parseVar() } })
            }
            return emptyList()
        }
    }

    private fun parseVar(): Var? {
        peek(TokenType.LOWER).let {
            when (it?.type) {
                TokenType.LOWER -> {
                    consume(TokenType.LOWER)
                    return Var(it.token)
                }
                else -> return null
            }
        }
    }

    private fun parseOr(): Formula? {
        val formula = parseAnd()!!
        val formulas = many({
            when (peek(TokenType.OR)?.type) {
                TokenType.OR -> {
                    consume(TokenType.OR)
                    when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
                        TokenType.EXISTS -> return@many parseQuantified()!!
                        TokenType.FORALL -> return@many parseQuantified()!!
                        else -> return@many parseAnd()!!
                    }
                }
                else -> return@many null
            }
        })
        return formulas.fold(formula, ::Or)
    }

    private fun parseAnd(): Formula? {
        val formula = parseNot()!!
        val formulas = many({
            //val token2 = peek()
            when (peek(TokenType.AND)?.type) {
                TokenType.AND -> {
                    consume(TokenType.AND)
                    when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
                        TokenType.EXISTS -> return@many parseQuantified()!!
                        TokenType.FORALL -> return@many parseQuantified()!!
                        else -> return@many parseNot()!!
                    }
                }
                else -> return@many null
            }
        })
        return formulas.fold(formula, ::And)
    }

    private fun parseNot(): Formula? {
        when (peek(TokenType.NOT)?.type) {
            TokenType.NOT -> {
                consume(TokenType.NOT)
                when (peek(TokenType.EXISTS, TokenType.FORALL)?.type) {
                    TokenType.EXISTS -> return Not(parseQuantified()!!)
                    TokenType.FORALL -> return Not(parseQuantified()!!)
                    else -> return Not(parseNot()!!)
                }
            }
            else -> return parseAtom()!!
        }
    }

    private fun parseAtom(): Formula? {
        val token = peek(TokenType.TRUE, TokenType.FALSE, TokenType.LOWER, TokenType.UPPER, TokenType.LPAREN)
        when (token?.type) {
            TokenType.TRUE -> {
                consume(TokenType.TRUE)
                return Top
            }
            TokenType.FALSE -> {
                consume(TokenType.FALSE)
                return Bottom
            }
            TokenType.LOWER -> {
                val term1 = parseTerm()!!
                consume(TokenType.EQUALS)
                val term2 = parseTerm()!!
                return Equals(term1, term2)
            }
            TokenType.UPPER -> {
                val relation = Rel(token.token)
                consume(TokenType.UPPER)
                consume(TokenType.LPAREN)
                val terms = parseTerms()
                consume(TokenType.RPAREN)
                return Atom(relation, terms)
            }
            TokenType.LPAREN -> {
                consume(TokenType.LPAREN)
                val formula = parseFormula()!!
                consume(TokenType.RPAREN)
                return formula
            }
            else -> return null
        }
    }

    private fun parseTerms(): List<Term> {
        parseTerm().let {
            if (it != null) {
                return listOf(it) + many({ commaSeparated { parseTerm() } })
            }
            return emptyList()
        }
    }

    private fun parseTerm(): Term? {
        val firstToken = consume(TokenType.LOWER)

        when (peek(TokenType.LPAREN)?.type) {
            TokenType.LPAREN -> {
                consume(TokenType.LPAREN)
                val terms = parseTerms()
                consume(TokenType.RPAREN)
                return App(Func(firstToken.token), terms)
            }
            else -> return Var(firstToken.token) // This is not standard
        }
    }
}

fun String.parseTheory() = Parser().parse(this)