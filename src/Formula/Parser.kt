package Formula

class ParserException(message: String): Exception(message)

class Parser {
    var tokens: List<Token> = emptyList()
      set(value){
          // resetting the input: reset the index
          index = 0
          field = value
      }
    var index = 0
    fun peek(): Token? = tokens.getOrNull(index)
    fun consume(type: TokenType): Token {
        peek().let {
            if (it != null && it.type == type) {
                index++
                return it
            } else if (it == null) {
                throw ParserException("Unreachable!")
            } else {
                throw ParserException("Parsing Error at ${it.location}: expecting ${type.toString()} but ${it.token} is found.")
            }
        }
    }

    private fun <T> many(parser: () -> T?): List<T> {
        var result: List<T> = emptyList()
        while (true) {
            val item = parser() ?: break
            result += item
        }
        return result
    }

    private fun <T> expect(parser: () -> T?): T {
        parser().let {
            when (it) {
                null -> throw RuntimeException("error 1")
                else -> return it
            }
        }
    }

    private fun <T> commaSeparated(parser: () -> T?): T? {
        val token: Token? = peek()
        if (token!!.type != TokenType.END) {
            if (token.type == TokenType.COMMA) {
                consume(TokenType.COMMA)
                return expect(parser)
            }
        }

        return null
    }

    fun parse(source: String): Theory? {
        this.tokens = tokenize(source)
        val theory = parseTheory()
        consume(TokenType.END)
        return theory
    }

    private fun parseTheory(): Theory? = Theory(many { parseFormula() })

    private fun parseFormula(): Formula? {
        val token1: Token? = peek()
        if (token1!!.type != TokenType.END) {
            val formula = expect { parseQuantified() }
            val formulas = many({
                val token2 = peek()
                if (token2!!.type != TokenType.END) {
                    if (token2.type == TokenType.IMPLIES) {
                        consume(TokenType.IMPLIES)
                        return@many expect { parseQuantified() }
                    }
                }
                return@many null
            })
            return formulas.fold(formula, ::Implies)
        }
        return null
    }

    private fun parseQuantified(): Formula? {
        val token: Token? = peek()
        if (token!!.type != TokenType.END) {
            if (token.type == TokenType.EXISTS) {
                consume(TokenType.EXISTS)
                val vars = parseVars()
                consume(TokenType.DOT)
                val formula = expect { parseQuantified() }
                return Exists(vars, formula)

            } else if (token.type == TokenType.FORALL) {
                consume(TokenType.FORALL)
                val vars = parseVars()
                consume(TokenType.DOT)
                val formula = expect { parseQuantified() }
                return Forall(vars, formula)
            } else {
                return expect { parseOr() }
            }
        }
        return null
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
        val token: Token? = peek()
        if (token!!.type != TokenType.END) {
            if (token.type == TokenType.LOWER) {
                consume(TokenType.LOWER)
                return Var(token.token)
            }
        }
        return null
    }


    private fun parseOr(): Formula? {
        val token1: Token? = peek()
        if (token1!!.type != TokenType.END) {
            val formula = expect { parseAnd() }
            val formulas = many({
                val token2 = peek()
                if (token2!!.type != TokenType.END) {
                    if (token2.type == TokenType.OR) {
                        consume(TokenType.OR)
                        val token3 = peek()
                        if (token3!!.type != TokenType.END) {
                            when (token3.type) {
                                TokenType.EXISTS -> return@many expect { parseQuantified() }
                                TokenType.FORALL -> return@many expect { parseQuantified() }
                                else -> return@many expect { parseAnd() }
                            }
                        }
                    }
                }
                return@many null
            })
            return formulas.fold(formula, ::Or)
        }
        return null
    }

    private fun parseAnd(): Formula? {
        val token1: Token? = peek()
        if (token1!!.type != TokenType.END) {
            val formula = expect { parseNot() }
            val formulas = many({
                val token2 = peek()
                if (token2!!.type != TokenType.END) {
                    if (token2.type == TokenType.AND) {
                        consume(TokenType.AND)
                        val token3 = peek()
                        if (token3!!.type != TokenType.END) {
                            when (token3.type) {
                                TokenType.EXISTS -> return@many expect { parseQuantified() }
                                TokenType.FORALL -> return@many expect { parseQuantified() }
                                else -> return@many expect { parseNot() }
                            }
                        }
                    }
                }
                return@many null
            })
            return formulas.fold(formula, ::And)
        }
        return null
    }

    private fun parseNot(): Formula? {
        val token1: Token? = peek()
        if (token1!!.type != TokenType.END) {
            if (token1.type == TokenType.NOT) {
                consume(TokenType.NOT)
                val token2 = peek()
                if (token2!!.type != TokenType.END) {
                    when (token2.type) {
                        TokenType.EXISTS -> return Not(expect { parseQuantified() })
                        TokenType.FORALL -> return Not(expect { parseQuantified() })
                        else -> return Not(expect { parseNot() })
                    }
                }
            } else {
                return expect { parseAtom() }
            }
        }
        return null
    }

    private fun parseAtom(): Formula? {
        val token: Token? = peek()
        if (token!!.type != TokenType.END) {
            when (token.type) {
                TokenType.TRUE -> {
                    consume(TokenType.TRUE)
                    return Top
                }
                TokenType.FALSE -> {
                    consume(TokenType.FALSE)
                    return Bottom
                }
                TokenType.LOWER -> {
                    val term1 = expect { parseTerm() }
                    consume(TokenType.EQUALS)
                    val term2 = expect { parseTerm() }
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
                    val formula = expect { parseFormula() }
                    consume(TokenType.RPAREN)
                    return formula
                }
                else -> {
                }
            }
        }
        return null
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
        var token: Token? = peek()
        if (token!!.type != TokenType.END) {
            val firstToken = consume(TokenType.LOWER)
            token = peek()
            if (token!!.type != TokenType.END) {
                when (token.type) {
                    TokenType.LPAREN -> {
                        consume(TokenType.LPAREN)
                        val terms = parseTerms()
                        consume(TokenType.RPAREN)
                        return App(Func(firstToken.token), terms)
                    }
                    else -> return Var(firstToken.token) // This is not standard
                }
            }
            return Var(firstToken.token)
        }
        return null
    }
}

fun String.parseTheory() = Parser().parse(this)