package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.CaseloadAlreadyExistsException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.InvalidRoleAssignmentException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.CaseloadNotFoundException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.PasswordTooShortException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.PasswordValidationException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.ReusedPasswordException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserAlreadyExistsException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserNotFoundException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserRoleAlreadyExistsException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserRoleNotFoundException
import javax.validation.ValidationException

@RestControllerAdvice
class NomisUserRolesApiExceptionHandler {
  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ErrorResponse(status = (HttpStatus.FORBIDDEN.value())))
  }

  @ExceptionHandler(WebClientResponseException::class)
  fun handleWebClientResponseException(e: WebClientResponseException): ResponseEntity<ByteArray> {
    if (e.statusCode.is4xxClientError) {
      log.info("Unexpected client exception with message {}", e.message)
    } else {
      log.error("Unexpected server exception", e)
    }
    return ResponseEntity
      .status(e.rawStatusCode)
      .body(e.responseBodyAsByteArray)
  }

  @ExceptionHandler(WebClientException::class)
  fun handleWebClientException(e: WebClientException): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(ErrorResponse(status = (INTERNAL_SERVER_ERROR.value()), developerMessage = (e.message)))
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
          errorCode = BASIC_VALIDATION_FAILURE
        )
      )
  }

  @ExceptionHandler(TypeMismatchException::class)
  fun handleValidationException(e: TypeMismatchException): ResponseEntity<ErrorResponse> {
    log.info("Parameter conversion exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Parameter conversion failure: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(UserNotFoundException::class)
  fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("User not found exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse(
          status = HttpStatus.NOT_FOUND,
          userMessage = "User not found: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(UserAlreadyExistsException::class)
  fun handleUserAlreadyExistsException(e: UserAlreadyExistsException): ResponseEntity<ErrorResponse?>? {
    log.debug("User already exists exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse(
          status = HttpStatus.CONFLICT,
          userMessage = "User already exists: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(UserRoleNotFoundException::class)
  fun handleUserRoleNotFoundException(e: UserRoleNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("Role not found exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse(
          status = HttpStatus.NOT_FOUND,
          userMessage = "Role not found: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(InvalidRoleAssignmentException::class)
  fun handleInvalidRoleAssignmentException(e: InvalidRoleAssignmentException): ResponseEntity<ErrorResponse?>? {
    log.debug("Invalid Role assignment: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Role assignment invalid: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(UserRoleAlreadyExistsException::class)
  fun handleUserRoleAlreadyExistsException(e: UserRoleAlreadyExistsException): ResponseEntity<ErrorResponse?>? {
    log.debug("Role already exists exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse(
          status = HttpStatus.CONFLICT,
          userMessage = "Role already exists: ${e.message}",
          developerMessage = e.message
        )
      )
  }
  @ExceptionHandler(CaseloadNotFoundException::class)
  fun handleCaseloadNotFoundException(e: CaseloadNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("Caseload not found exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse(
          status = HttpStatus.NOT_FOUND,
          userMessage = "Caseload not found: ${e.message}",
          developerMessage = e.message
        )
      )
  }
  @ExceptionHandler(CaseloadAlreadyExistsException::class)
  fun handleCaseloadAlreadyExistsException(e: CaseloadAlreadyExistsException): ResponseEntity<ErrorResponse?>? {
    log.debug("Caseload already exists exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse(
          status = HttpStatus.CONFLICT,
          userMessage = "Caseload already exists: ${e.message}",
          developerMessage = e.message
        )
      )
  }
  @ExceptionHandler(PasswordTooShortException::class)
  fun handlePasswordTooShortException(e: PasswordTooShortException): ResponseEntity<ErrorResponse> {
    log.debug("Password too short exception caught: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = (BAD_REQUEST.value()),
          userMessage = "Password too short: ${e.message}",
          developerMessage = (e.message)
        )
      )
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleValidationException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
    log.debug("Bad Request (400) returned: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = (BAD_REQUEST.value()),
          userMessage = "Parameter Missing: ${e.message}",
          developerMessage = (e.message)
        )
      )
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    log.debug("Validation error (400) returned: {}", e.message)
    val message = if (e.hasFieldErrors()) { e.fieldError?.defaultMessage } else { e.message }
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = (BAD_REQUEST.value()),
          userMessage = "Validation failure: $message",
          developerMessage = (e.message)
        )
      )
  }

  @ExceptionHandler(ReusedPasswordException::class)
  fun reusedPasswordException(e: ReusedPasswordException): ResponseEntity<ErrorResponse> {
    log.debug("Password reused error (400) returned {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = e.message,
          developerMessage = e.message,
          errorCode = PASSWORD_HAS_BEEN_USED_BEFORE,
        )
      )
  }

  @ExceptionHandler(PasswordValidationException::class)
  fun passwordValidationException(e: PasswordValidationException): ResponseEntity<ErrorResponse> {
    log.debug("Password not acceptable error (400) returned {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = e.message,
          developerMessage = e.message,
          errorCode = PASSWORD_NOT_ACCEPTABLE,
        )
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null
  ) :
    this(status.value(), errorCode, userMessage, developerMessage)
}

const val BASIC_VALIDATION_FAILURE = 1000
const val PASSWORD_HAS_BEEN_USED_BEFORE = 1001
const val PASSWORD_NOT_ACCEPTABLE = 1002
