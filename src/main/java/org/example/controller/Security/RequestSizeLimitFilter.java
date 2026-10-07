package org.example.controller.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Rejects request bodies that are too big, before they are read into memory.
 *
 * Spring/Tomcat don't limit JSON bodies by default, so one huge request (or a few at once)
 * could make the server run out of memory. Athlete uploads contain photos, so they get a
 * bigger limit than everything else.
 *
 * Requests that announce their size (Content-Length) are rejected straight away with 413.
 * Chunked requests (no Content-Length) are counted while they are read and fail once they
 * go over the limit.
 */
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    /** Endpoints that carry athlete photos. */
    private static final Set<String> UPLOAD_PATHS = Set.of("/api/add-athlete", "/api/add-athletes");

    private final long maxRequestBytes;
    private final long maxUploadBytes;

    public RequestSizeLimitFilter(long maxRequestBytes, long maxUploadBytes) {
        this.maxRequestBytes = maxRequestBytes;
        this.maxUploadBytes = maxUploadBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        long limit = UPLOAD_PATHS.contains(path) ? maxUploadBytes : maxRequestBytes;

        long declared = request.getContentLengthLong();
        if (declared > limit) {
            response.setStatus(413);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"Request is too large.\"}");
            return;
        }

        chain.doFilter(declared >= 0 ? request : new LimitedRequest(request, limit), response);
    }

    /** Wraps a request with unknown length so reading more than `limit` bytes fails. */
    private static final class LimitedRequest extends HttpServletRequestWrapper {
        private final long limit;
        private ServletInputStream stream;

        LimitedRequest(HttpServletRequest request, long limit) {
            super(request);
            this.limit = limit;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (stream == null) stream = new LimitedInputStream(super.getInputStream(), limit);
            return stream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String encoding = getCharacterEncoding();
            Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8;
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }

    private static final class LimitedInputStream extends ServletInputStream {
        private final ServletInputStream in;
        private final long limit;
        private long count;

        LimitedInputStream(ServletInputStream in, long limit) {
            this.in = in;
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b != -1) add(1);
            return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int n = in.read(buf, off, len);
            if (n > 0) add(n);
            return n;
        }

        private void add(long n) throws IOException {
            count += n;
            if (count > limit) throw new IOException("Request body is larger than " + limit + " bytes");
        }

        @Override public boolean isFinished() { return in.isFinished(); }
        @Override public boolean isReady() { return in.isReady(); }
        @Override public void setReadListener(ReadListener listener) { in.setReadListener(listener); }
    }
}
