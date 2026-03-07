package exception;

/**
 * Exception thrown when Sleeper API operations fail. This includes HTTP errors, parsing errors, and
 * network failures.
 */
public class SleeperApiException extends RuntimeException {

  private final Integer statusCode;

  public SleeperApiException(String message) {
    super(message);
    this.statusCode = null;
  }

  public SleeperApiException(String message, Throwable cause) {
    super(message, cause);
    this.statusCode = null;
  }

  public SleeperApiException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public Integer getStatusCode() {
    return statusCode;
  }
}
