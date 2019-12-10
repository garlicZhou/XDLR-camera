package com.xdlr.camera.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {
    public static String doGet(String httpurl) {
        HttpURLConnection connection = getHttpsURLConnection(httpurl, "GET");
        return read(connection);
    }

    public static String doPost(String httpUrl, String param) {
        HttpURLConnection connection = getHttpsURLConnection(httpUrl, "POST");
        try(OutputStream os = connection.getOutputStream()) {
            os.write(param.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return read(connection);
    }

    private static HttpURLConnection getHttpsURLConnection(String httpUrl, String method) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            if (method.equals("GET")) {
                connection.setRequestMethod("GET");
            } else {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
                connection.setDoInput(true);
                // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
                connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static String read(HttpURLConnection connection) {
        try {
            if (connection.getResponseCode() == 200) {
                try (InputStream is = connection.getInputStream();
                     BufferedReader br = new BufferedReader(
                             new InputStreamReader(is, "UTF-8"))
                ) {
                    StringBuffer sbf = new StringBuffer();
                    String temp = null;
                    while ((temp = br.readLine()) != null) {
                        sbf.append(temp);
                        sbf.append("\r\n");
                    }
                    connection.disconnect();
                    return sbf.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String res = doGet("http://125.76.159.135:8080/cli/request?cid=1");
        System.out.println(res);
    }
}