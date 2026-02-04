package lg.voltup.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class AlreadyParticipatedException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BudgetExhaustedException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class ProductNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrderNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class ParticipationNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InsufficientPointsException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InsufficientStockException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ProductNotAvailableException(message: String) : RuntimeException(message)
