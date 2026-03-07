package service;

import exception.SleeperApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic HTTP service for making REST API calls. Provides reusable HTTP communication with error
 * handling and logging.
 */
@Slf4j
public class HttpService {
  private static final int HTTP_OK = 200;
  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  /**
   * Executes a GET request to the specified URL and returns the response body.
   *
   * @param url the URL to fetch
   * @param errorMessage the error message prefix for failures
   * @return response body as string
   * @throws SleeperApiException if the request fails or status is not 200
   */
  public static String get(String url, String errorMessage) {
    log.debug("GET request to: {}", url);
    long startTime = System.currentTimeMillis();

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(30))
              .GET()
              .build();

      HttpResponse<String> response =
          HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

      long duration = System.currentTimeMillis() - startTime;
      log.debug("Request completed in {}ms with status code: {}", duration, response.statusCode());

      if (response.statusCode() != HTTP_OK) {
        String message = String.format("%s: HTTP %d", errorMessage, response.statusCode());
        String bodyPreview = truncate(response.body(), 200);
        log.error("{} - Response: {}", message, bodyPreview);
        throw new SleeperApiException(message, response.statusCode());
      }

      return response.body();

    } catch (IOException e) {
      log.error("Network error while fetching {}: {}", url, e.getMessage());
      throw new SleeperApiException(errorMessage + ": Network error", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Request interrupted while fetching {}: {}", url, e.getMessage());
      throw new SleeperApiException(errorMessage + ": Request interrupted", e);
    }
  }

  /**
   * Executes a GET request with default error message.
   *
   * @param url the URL to fetch
   * @return response body as string
   * @throws SleeperApiException if the request fails
   */
  public static String get(String url) {
    return get(url, "HTTP request failed");
  }

  /**
   * Truncates a string to the specified maximum length.
   *
   * @param str the string to truncate
   * @param maxLength the maximum length
   * @return truncated string
   */
  private static String truncate(String str, int maxLength) {
    if (str == null) {
      return "";
    }
    return str.substring(0, Math.min(maxLength, str.length()));
  }
}
