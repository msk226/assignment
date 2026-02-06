package lg.voltup.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String?,
)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "User Not Found", message = e.message),
        )
    }

    @ExceptionHandler(AlreadyParticipatedException::class)
    fun handleAlreadyParticipated(e: AlreadyParticipatedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Already Participated", message = e.message),
        )
    }

    @ExceptionHandler(BudgetExhaustedException::class)
    fun handleBudgetExhausted(e: BudgetExhaustedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Budget Exhausted", message = e.message),
        )
    }

    @ExceptionHandler(ParticipationNotFoundException::class)
    fun handleParticipationNotFound(e: ParticipationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Participation Not Found", message = e.message),
        )
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(e: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Product Not Found", message = e.message),
        )
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStock(e: InsufficientStockException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Insufficient Stock", message = e.message),
        )
    }

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFound(e: OrderNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(status = 404, error = "Order Not Found", message = e.message),
        )
    }

    @ExceptionHandler(InsufficientPointsException::class)
    fun handleInsufficientPoints(e: InsufficientPointsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Insufficient Points", message = e.message),
        )
    }

    @ExceptionHandler(ProductNotAvailableException::class)
    fun handleProductNotAvailable(e: ProductNotAvailableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Product Not Available", message = e.message),
        )
    }

    @ExceptionHandler(OrderAlreadyCancelledException::class)
    fun handleOrderAlreadyCancelled(e: OrderAlreadyCancelledException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Order Already Cancelled", message = e.message),
        )
    }

    @ExceptionHandler(PointsAlreadyUsedException::class)
    fun handlePointsAlreadyUsed(e: PointsAlreadyUsedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Points Already Used", message = e.message),
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(e: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val rootMessage = e.rootCause?.message ?: e.message ?: ""

        // RouletteParticipation unique constraint 위반 (user_id, date)
        val isParticipationDuplicate =
            rootMessage.contains("roulette_participation", ignoreCase = true) ||
                (rootMessage.contains("user_id", ignoreCase = true) && rootMessage.contains("date", ignoreCase = true)) ||
                rootMessage.contains("UK_", ignoreCase = true) // H2 unique constraint prefix

        return if (isParticipationDuplicate) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(status = 400, error = "Already Participated", message = "오늘 이미 참여했습니다."),
            )
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse(status = 409, error = "Conflict", message = "데이터 처리 중 오류가 발생했습니다."),
            )
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(status = 400, error = "Bad Request", message = e.message),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(status = 500, error = "Internal Server Error", message = "서버 오류가 발생했습니다."),
        )
    }
}
