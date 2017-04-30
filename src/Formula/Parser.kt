package Formula

import java.util.*


private class Context(val tokens: List<Token>) {
    var index = 0
    fun peek(): Token? = tokens.getOrNull(index)
    fun consume(type: TokenType): Token {
        peek().let {
            if (it != null && it.type == type) {
                index++
                return it
            }
        }
        throw RuntimeException("error 2")
    }
}

private fun <T> many(parser: () -> T?): List<T> {
    val result: LinkedList<T> = LinkedList()
    while (true) {
        val item = parser() ?: break
        result.add(item)
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

private fun <T> commaSeparated(context: Context, parser: () -> T?): T? {
    val token: Token? = context.peek()
    if (token!!.type != TokenType.END) {
        if (token.type == TokenType.COMMA) {
            context.consume(TokenType.COMMA)
            return expect(parser)
        }
    }

    return null
}

fun parse(source: String): Theory? {
    val context = Context(Tokenizer(source).tokenize())
    val theory = parseTheory(context)
    expect { context.consume(TokenType.END) }
    return theory
}

private fun parseTheory(context: Context): Theory? = Theory(many { parseFormula(context) })

private fun parseFormula(context: Context): Formula? {
    val token1: Token? = context.peek()
    if (token1!!.type != TokenType.END) {
        val formula = expect { parseQuantified(context) }
        val formulas = many({
            val token2 = context.peek()
            if (token2!!.type != TokenType.END) {
                if (token2.type == TokenType.IMPLIES) {
                    context.consume(TokenType.IMPLIES)
                    return@many expect { parseQuantified(context) }
                }
            }
            return@many null
        })
        return formulas.fold(formula, ::Implies)
    }
    return null
}

private fun parseQuantified(context: Context): Formula? {
    val token: Token? = context.peek()
    if (token!!.type != TokenType.END) {
        if (token.type == TokenType.EXISTS) {
            context.consume(TokenType.EXISTS)
            val vars = parseVars(context)
            context.consume(TokenType.DOT)
            val formula = expect { parseQuantified(context) }
            return Exists(vars, formula)

        } else if (token.type == TokenType.FORALL) {
            context.consume(TokenType.FORALL)
            val vars = parseVars(context)
            context.consume(TokenType.DOT)
            val formula = expect { parseQuantified(context) }
            return Forall(vars, formula)
        } else {
            return expect { parseOr(context) }
        }
    }
    return null
}

private fun parseVars(context: Context): List<Var> {
    parseVar(context).let {
        if (it != null) {
            return listOf(it) + many({ commaSeparated(context, { parseVar(context) }) })
        }
        return emptyList()
    }
}

private fun parseVar(context: Context): Var? {
    val token: Token? = context.peek()
    if (token!!.type != TokenType.END) {
        if (token.type == TokenType.LOWER) {
            context.consume(TokenType.LOWER)
            return Var(token.token)
        }
    }
    return null
}


private fun parseOr(context: Context): Formula? {
    val token1: Token? = context.peek()
    if (token1!!.type != TokenType.END) {
        val formula = expect { parseAnd(context) }
        val formulas = many({
            val token2 = context.peek()
            if (token2!!.type != TokenType.END) {
                if (token2.type == TokenType.OR) {
                    context.consume(TokenType.OR)
                    val token3 = context.peek()
                    if (token3!!.type != TokenType.END) {
                        when (token3.type) {
                            TokenType.EXISTS -> return@many expect { parseQuantified(context) }
                            TokenType.FORALL -> return@many expect { parseQuantified(context) }
                            else -> return@many expect { parseAnd(context) }
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

private fun parseAnd(context: Context): Formula? {
    val token1: Token? = context.peek()
    if (token1!!.type != TokenType.END) {
        val formula = expect { parseNot(context) }
        val formulas = many({
            val token2 = context.peek()
            if (token2!!.type != TokenType.END) {
                if (token2.type == TokenType.AND) {
                    context.consume(TokenType.AND)
                    val token3 = context.peek()
                    if (token3!!.type != TokenType.END) {
                        when (token3.type) {
                            TokenType.EXISTS -> return@many expect { parseQuantified(context) }
                            TokenType.FORALL -> return@many expect { parseQuantified(context) }
                            else -> return@many expect { parseNot(context) }
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

private fun parseNot(context: Context): Formula? {
    val token1: Token? = context.peek()
    if (token1!!.type != TokenType.END) {
        if (token1.type == TokenType.NOT) {
            context.consume(TokenType.NOT)
            val token2 = context.peek()
            if (token2!!.type != TokenType.END) {
                when (token2.type) {
                    TokenType.EXISTS -> return Not(expect { parseQuantified(context) })
                    TokenType.FORALL -> return Not(expect { parseQuantified(context) })
                    else -> return Not(expect { parseNot(context) })
                }
            }
        } else {
            return expect { parseAtom(context) }
        }
    }
    return null
}

private fun parseAtom(context: Context): Formula? {
    val token: Token? = context.peek()
    if (token!!.type != TokenType.END) {
        when (token.type) {
            TokenType.TRUE -> {
                context.consume(TokenType.TRUE)
                return Top
            }
            TokenType.FALSE -> {
                context.consume(TokenType.FALSE)
                return Bottom
            }
            TokenType.LOWER -> {
                val term1 = expect { parseTerm(context) }
                context.consume(TokenType.EQUALS)
                val term2 = expect { parseTerm(context) }
                return Equals(term1, term2)
            }
            TokenType.UPPER -> {
                val relation = Rel(token.token)
                context.consume(TokenType.UPPER)
                context.consume(TokenType.LPAREN)
                val terms = parseTerms(context)
                context.consume(TokenType.RPAREN)
                return Atom(relation, terms)
            }
            TokenType.LPAREN -> {
                context.consume(TokenType.LPAREN)
                val formula = expect { parseFormula(context) }
                context.consume(TokenType.RPAREN)
                return formula
            }
            else -> {
            }
        }
    }
    return null
}

private fun parseTerms(context: Context): List<Term> {
    parseTerm(context).let {
        if (it != null) {
            return listOf(it) + many({ commaSeparated(context, { parseTerm(context) }) })
        }
        return emptyList()
    }
}

private fun parseTerm(context: Context): Term? {
    var token: Token? = context.peek()
    if (token!!.type != TokenType.END) {
        val firstToken = context.consume(TokenType.LOWER)
        token = context.peek()
        if (token!!.type != TokenType.END) {
            when (token.type) {
                TokenType.LPAREN -> {
                    context.consume(TokenType.LPAREN)
                    val terms = parseTerms(context)
                    context.consume(TokenType.RPAREN)
                    return App(Func(firstToken.token), terms)
                }
                else -> return Var(firstToken.token) // This is not standard
            }
        }
        return Var(firstToken.token)
    }
    return null
}