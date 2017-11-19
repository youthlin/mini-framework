package com.youthlin.mvc.support;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * copy from: org.springframework.web.filter.CharacterEncodingFilter
 * <p>
 * 创建： youthlin.chen
 * 时间： 2017-08-18 16:46.
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class CharacterEncodingFilter implements Filter {
    private String encoding = "UTF-8";
    private boolean forceRequestEncoding = true;
    private boolean forceResponseEncoding = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encoding = filterConfig.getInitParameter("encoding");
        if (encoding != null) {
            this.encoding = encoding;
        }
        String forceRequestEncoding = filterConfig.getInitParameter("forceRequestEncoding");
        if (forceRequestEncoding != null) {
            this.forceRequestEncoding = Boolean.valueOf(forceRequestEncoding);
        }
        String forceResponseEncoding = filterConfig.getInitParameter("forceResponseEncoding");
        if (forceResponseEncoding != null) {
            this.forceResponseEncoding = Boolean.valueOf(forceResponseEncoding);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String encoding = getEncoding();
        if (encoding != null) {
            if (isForceRequestEncoding() || request.getCharacterEncoding() == null) {
                request.setCharacterEncoding(encoding);
            }
            if (isForceResponseEncoding()) {
                response.setCharacterEncoding(encoding);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    public String getEncoding() {
        return encoding;
    }

    public CharacterEncodingFilter setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public boolean isForceRequestEncoding() {
        return forceRequestEncoding;
    }

    public CharacterEncodingFilter setForceRequestEncoding(boolean forceRequestEncoding) {
        this.forceRequestEncoding = forceRequestEncoding;
        return this;
    }

    public boolean isForceResponseEncoding() {
        return forceResponseEncoding;
    }

    public CharacterEncodingFilter setForceResponseEncoding(boolean forceResponseEncoding) {
        this.forceResponseEncoding = forceResponseEncoding;
        return this;
    }
}
