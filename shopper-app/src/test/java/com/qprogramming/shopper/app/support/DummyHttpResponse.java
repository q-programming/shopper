package com.qprogramming.shopper.app.support;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Jakub Romaniszyn on 2018-07-23
 */
public class DummyHttpResponse implements HttpServletResponse {

    private Set<Cookie> cookies = new HashSet<>();
    private PrintWriter printWriter;
    private Map<String, String> headers = new HashMap<>();


    public DummyHttpResponse withWritter(PrintWriter printWriter) {
        this.printWriter = printWriter;
        return this;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

    @Override
    public boolean containsHeader(String s) {
        return this.headers.containsKey(s);
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {
        this.headers.put(s, String.valueOf(l));

    }

    @Override
    public void addDateHeader(String s, long l) {
        this.headers.put(s, String.valueOf(l));
    }

    @Override
    public void setHeader(String s, String s1) {
        this.headers.put(s, s1);
    }

    @Override
    public void addHeader(String s, String s1) {
        this.headers.put(s, s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        this.headers.put(s, String.valueOf(i));
    }

    @Override
    public void addIntHeader(String s, int i) {
        this.headers.put(s, String.valueOf(i));
    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public void setStatus(int i, String s) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return this.headers.get(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return this.headers.values();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.printWriter;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentLengthLong(long l) {

    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
