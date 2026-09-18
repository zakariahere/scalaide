package dev.scalaide.workbench;

import com.google.gson.*;
import dev.scalaide.core.LearningPrompt;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.Flow;

/** Direct provider API, bounded responses, no redirects, retries, source/key logging or tools. */
final class DeepSeekClient {
    static final URI ENDPOINT = URI.create("https://api.deepseek.com/chat/completions");
    static final int MAX_RESPONSE_BYTES = 512 * 1024;
    private final HttpClient http;
    private final URI endpoint;
    private final Duration timeout;

    DeepSeekClient() { this(ENDPOINT, Duration.ofSeconds(90)); }
    // Package-private injection is used by loopback HTTP contract tests, never a user setting.
    DeepSeekClient(URI endpoint, Duration timeout) {
        this.endpoint = endpoint;
        this.timeout = timeout;
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }

    record Summary(String markdown, boolean truncated) {}

    void shutdown() { http.shutdownNow(); }

    CompletableFuture<Summary> summarize(String fileName, String source, String language,
                                         boolean compareJava, String model, String key) {
        LearningPrompt.validateSource(fileName, source);
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Add your DeepSeek API key in Learning settings.");
        if (!model.matches("[a-zA-Z0-9][a-zA-Z0-9._-]{0,79}"))
            throw new IllegalArgumentException("Enter a valid DeepSeek model name in Learning settings.");
        if (!key.matches("[!-~]+")) throw new IllegalArgumentException("The API key contains invalid whitespace or characters.");
        var body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 2400);
        var thinking = new JsonObject(); thinking.addProperty("type", "disabled"); body.add("thinking", thinking);
        var messages = new JsonArray();
        messages.add(message("system", LearningPrompt.system(language, compareJava)));
        messages.add(message("user", LearningPrompt.user(fileName, source)));
        body.add("messages", messages);
        var request = HttpRequest.newBuilder(endpoint).timeout(timeout)
                .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
        var transport = http.sendAsync(request, info -> new BoundedBodySubscriber());
        // Own the public future: HttpClient's derived futures propagate cancellation upstream
        // before completing themselves, which can otherwise race the cancellation result.
        var result = new CompletableFuture<Summary>();
        transport.whenComplete((response, error) -> {
            if (error != null) { result.completeExceptionally(error); return; }
            try {
                if (response.statusCode() != 200) throw new LearningFailure(statusMessage(response.statusCode()));
                result.complete(parse(response.body()));
            } catch (RuntimeException invalid) { result.completeExceptionally(invalid); }
        });
        result.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        // Cancelling the UI task or reaching the deadline must also abort the transport/body subscription.
        result.whenComplete((answer, error) -> { if (error != null) transport.cancel(true); });
        return result;
    }

    private static JsonObject message(String role, String content) {
        var message = new JsonObject(); message.addProperty("role", role); message.addProperty("content", content); return message;
    }

    static Summary parse(byte[] bytes) {
        try {
            var root = JsonParser.parseString(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var first = root.getAsJsonArray("choices").get(0).getAsJsonObject();
            String reason = first.get("finish_reason").getAsString();
            if (!reason.equals("stop") && !reason.equals("length")) throw new IllegalArgumentException();
            var value = first.getAsJsonObject("message").get("content");
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new IllegalArgumentException();
            String content = value.getAsString();
            if (content.isBlank() || content.length() > 80_000) throw new IllegalArgumentException();
            return new Summary(content, reason.equals("length"));
        } catch (RuntimeException malformed) {
            // Never expose the response body, which can contain source text or echoed credentials.
            throw new CompletionException(new LearningFailure("DeepSeek returned an unexpected or empty summary. Try again."));
        }
    }

    static String statusMessage(int status) {
        return switch (status) {
            case 401, 403 -> "DeepSeek rejected the API key or access. Check Learning settings.";
            case 402 -> "The DeepSeek account has insufficient balance.";
            case 429 -> "DeepSeek is rate limiting requests. Wait a moment before trying again.";
            case 400, 404, 422 -> "DeepSeek rejected the request. Check the configured model in Learning settings.";
            default -> status >= 500 ? "DeepSeek is temporarily unavailable. Try again later."
                    : "DeepSeek could not complete the request (HTTP " + status + ").";
        };
    }

    static String safeError(Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null)
            error = error.getCause();
        if (error instanceof LearningFailure) return error.getMessage();
        if (error instanceof TimeoutException || error instanceof HttpTimeoutException)
            return "The request timed out. You can try again; no automatic retry was sent.";
        if (error instanceof CancellationException) return "Summary cancelled.";
        return "Could not reach DeepSeek or read its response. Check your connection and try again.";
    }

    static final class LearningFailure extends RuntimeException {
        LearningFailure(String message) { super(message); }
    }

    private static final class BoundedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
        private Flow.Subscription subscription;
        private long bytes;
        private boolean done;
        @Override public CompletionStage<byte[]> getBody() { return delegate.getBody(); }
        @Override public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription; delegate.onSubscribe(subscription);
        }
        @Override public void onNext(List<ByteBuffer> buffers) {
            if (done) return;
            for (var buffer : buffers) bytes += buffer.remaining();
            if (bytes > MAX_RESPONSE_BYTES) {
                done = true; subscription.cancel(); delegate.onError(new IOException("Response exceeded size limit"));
            } else delegate.onNext(buffers);
        }
        @Override public void onError(Throwable error) { if (!done) { done = true; delegate.onError(error); } }
        @Override public void onComplete() { if (!done) { done = true; delegate.onComplete(); } }
    }
}
