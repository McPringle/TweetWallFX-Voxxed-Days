/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2022 TweetWallFX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.tweetwallfx.voxxed;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RestCallHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCallHelper.class);

    private RestCallHelper() {
        // prevent instantiation
    }

    private static Client getClient() {
        return ClientBuilder.newClient();
    }

    private static String getHttpsUrl(final String url) {
        Objects.requireNonNull(url, "url parameter must not be null!");
        if (url.startsWith("http:")) {
            return url.replaceAll("^http:", "https:");
        } else {
            return url;
        }
    }

    private static Response getResponse(final String url, final Map<String, Object> queryParameters) {
        LOGGER.info("Calling URL: {} with query parameters: {}", url, queryParameters);
        WebTarget webTarget = getClient().target(getHttpsUrl(url));

        if (null != queryParameters && !queryParameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                webTarget = switch(value) {
                    case Object[] array -> webTarget.queryParam(key, array);
                    case Collection<?> collection -> webTarget.queryParam(key, collection.toArray());
                    default -> webTarget.queryParam(key, value);
                };
            }
        }

        final Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        LOGGER.info("Received Response: {}", response);

        return response;
    }

    public static Optional<Response> getOptionalResponse(final String url, final Map<String, Object> queryParameters) {
        try {
            return Optional.ofNullable(getResponse(url, queryParameters));
        } catch (final ProcessingException pe) {
            LOGGER.error("Encountered ProcessingException while calling to '" + url + "'", pe);
            return Optional.empty();
        }
    }

    public static Optional<Response> getOptionalResponse(final String url) {
        return getOptionalResponse(url, Collections.emptyMap());
    }

    public static <T> Optional<T> readOptionalFrom(final Response response, final Class<T> typeClass) {
        return Optional.of(Objects.requireNonNull(response, "Parameter response must not be null!"))
                .filter(r -> Response.Status.OK.getStatusCode() == r.getStatus())
                .map(r -> r.readEntity(typeClass));
    }

    public static <T> Optional<T> readOptionalFrom(final Response response, final GenericType<T> genericType) {
        return Optional.of(Objects.requireNonNull(response, "Parameter response must not be null!"))
                .filter(r -> Response.Status.OK.getStatusCode() == r.getStatus())
                .map(r -> r.readEntity(genericType));
    }

    public static <T> Optional<T> readOptionalFrom(final String url, final Class<T> typeClass) {
        return readOptionalFrom(url, null, typeClass);
    }

    public static <T> Optional<T> readOptionalFrom(final String url, final Map<String, Object> queryParameters, final Class<T> typeClass) {
        return getOptionalResponse(url, queryParameters)
                .flatMap(response -> readOptionalFrom(response, typeClass));
    }

    public static <T> Optional<T> readOptionalFrom(final String url, final GenericType<T> genericType) {
        return readOptionalFrom(url, null, genericType);
    }

    public static <T> Optional<T> readOptionalFrom(final String url, final Map<String, Object> queryParameters, final GenericType<T> genericType) {
        return getOptionalResponse(url, queryParameters)
                .flatMap(response -> readOptionalFrom(response, genericType));
    }
    public static <T> List<T> cast(List<?> list, Class<T> type) {
        return list.stream().map(type::cast).toList();
    }
}
