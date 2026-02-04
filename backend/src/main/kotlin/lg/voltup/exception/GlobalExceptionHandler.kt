package lg.voltup.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String?
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "User Not Found", message = e.message)
        )
    }

    @ExceptionHandler(AlreadyParticipatedException::class)
    fun handleAlreadyParticipated(e: AlreadyParticipatedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Already Participated", message = e.message)
        )
    }

    @ExceptionHandler(BudgetExhaustedException::class)
    fun handleBudgetExhausted(e: BudgetExhaustedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Budget Exhausted", message = e.message)
        )
    }

    @ExceptionHandler(ParticipationNotFoundException::class)
    fun handleParticipationNotFound(e: ParticipationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Participation Not Found", message = e.message)
        )
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(e: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Product Not Found", message = e.message)
        )
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStock(e: InsufficientStockException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Insufficient Stock", message = e.message)
        )
    }

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFound(e: OrderNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Order Not Found", message = e.message)
        )
    }

    @ExceptionHandler(InsufficientPointsException::class)
    fun handleInsufficientPoints(e: InsufficientPointsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Insufficient Points", message = e.message)
        )
    }

    @ExceptionHandler(ProductNotAvailableException::class)
    fun handleProductNotAvailable(e: ProductNotAvailableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Product Not Available", message = e.message)
        )
    }
}
