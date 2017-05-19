package Formula

typealias ErrorMessage = String

val INVALID_TERM: ErrorMessage = "Internal Error: Invalid term"
val INVALID_FORMULA: ErrorMessage = "Internal Error: Invalid formula"
val EXPECTED_NNF_FORMULA: ErrorMessage = "Internal Error: Formula in negation normal form expected"


fun ErrorMessage.internalError() = RuntimeException(this)