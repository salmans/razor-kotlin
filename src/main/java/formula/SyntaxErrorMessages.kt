package formula

typealias ErrorMessage = String

val INVALID_TERM: ErrorMessage = "Internal Error: Invalid term"
val INVALID_FORMULA: ErrorMessage = "Internal Error: Invalid formula"
val EXPECTED_NNF_FORMULA: ErrorMessage = "Internal Error: Expecting a formula in negation normal form."


fun ErrorMessage.internalError() = RuntimeException(this)