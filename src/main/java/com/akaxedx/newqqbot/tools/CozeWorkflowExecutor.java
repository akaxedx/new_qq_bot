package com.akaxedx.newqqbot.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class CozeWorkflowExecutor {

    // 从环境变量获取 Token（推荐）

    private static String API_TOKEN;

    @Value("${coze.key}")
    public void setSavePath(String savePath){
        API_TOKEN = savePath;
    }


    // 基础 API 地址
    private static final String API_URL = "https://api.coze.cn/v1/workflow/run";

    /**
     * 执行 Coze 工作流
     *
     * @param workflowId 工作流 ID
     * @param parameters 参数键值对
     * @return API 响应内容
     * @throws IOException 网络或 API 错误时抛出
     */
    public static String executeWorkflow(String workflowId, Map<String, Object> parameters) throws IOException {
        // 构建 JSON 请求体
        String jsonBody = buildRequestBody(workflowId, parameters);

        // 发送请求
        return sendPostRequest(API_URL, API_TOKEN, jsonBody);
    }

    // 构建 JSON 请求体
    private static String buildRequestBody(String workflowId, Map<String, Object> parameters) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{")
                .append("\"workflow_id\":\"").append(workflowId).append("\",")
                .append("\"parameters\":{");

        // 添加参数
        int count = 0;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (count++ > 0) jsonBuilder.append(",");

            jsonBuilder.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                jsonBuilder.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                jsonBuilder.append(value);
            } else {
                // 其他类型转为字符串
                jsonBuilder.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }

        jsonBuilder.append("}}");
        return jsonBuilder.toString();
    }

    // 转义 JSON 特殊字符
    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // 发送 POST 请求
    private static String sendPostRequest(String url, String token, String jsonBody) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL apiEndpoint = new URL(url);
            connection = (HttpURLConnection) apiEndpoint.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);  // 10秒连接超时
            connection.setReadTimeout(30000);     // 30秒读取超时

            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                    return response.toString();
                }
            } else {
                // 读取错误信息
                String errorResponse = readErrorResponse(connection);
                throw new IOException("API 错误: " + responseCode + " - " +
                        connection.getResponseMessage() +
                        (errorResponse != null ? "\n详情: " + errorResponse : ""));
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 读取错误响应内容
    private static String readErrorResponse(HttpURLConnection conn) {
        try (InputStream es = conn.getErrorStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(es, StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }
}