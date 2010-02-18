package com.todoroo.androidcommons.service;

import java.io.IOException;

/**
 * RestClient invokes the HTML requests as desired
 * 
 * @author timsu
 *
 */
public interface RestClient {
    public String get(String url) throws IOException;
    public String post(String url, String data) throws IOException;
}