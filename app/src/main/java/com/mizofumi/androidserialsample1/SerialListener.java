package com.mizofumi.androidserialsample1;

/**
 * Created by mizofumi on 2016/11/18.
 */

public interface SerialListener {
    void opened();
    void open_failed(String errorMessage);
    void read(String data);
    void read_failed(String errorMessage);
    void write_success();
    void write_failed(String s);
    void stoped();
    void closed();
    void close_failed(String s);
}
