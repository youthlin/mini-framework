package com.youthlin.mvc.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

/**
 * A response that includes no body, for use in (dumb) "HEAD" support.
 * This just swallows that body, counting the bytes in order to set
 * the content length appropriately.  All other methods delegate directly
 * to the wrapped HTTP Servlet Response object.
 * <p>
 * 创建：youthlin.chen
 * 时间：2017-08-17 00:26
 *
 * @see javax.servlet.http.HttpServlet
 */
class NoBodyResponse extends HttpServletResponseWrapper {
    private static final ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");

    private NoBodyOutputStream noBody;
    private PrintWriter writer;
    private boolean didSetContentLength;
    private boolean usingOutputStream;

    // file private
    NoBodyResponse(HttpServletResponse r) {
        super(r);
        noBody = new NoBodyOutputStream();
    }

    // file private
    void setContentLength() {
        if (!didSetContentLength) {
            if (writer != null) {
                writer.flush();
            }
            setContentLength(noBody.getContentLength());
        }
    }

    @Override
    public void setContentLength(int len) {
        super.setContentLength(len);
        didSetContentLength = true;
    }

    @Override
    public void setContentLengthLong(long len) {
        super.setContentLengthLong(len);
        didSetContentLength = true;
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        checkHeader(name);
    }

    private void checkHeader(String name) {
        if ("content-length".equalsIgnoreCase(name)) {
            didSetContentLength = true;
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException(lStrings.getString("err.ise.getOutputStream"));
        }
        usingOutputStream = true;
        return noBody;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (usingOutputStream) {
            throw new IllegalStateException(lStrings.getString("err.ise.getWriter"));
        }
        if (writer == null) {
            OutputStreamWriter w = new OutputStreamWriter(
                    noBody, getCharacterEncoding());
            writer = new PrintWriter(w);
        }
        return writer;
    }
}
