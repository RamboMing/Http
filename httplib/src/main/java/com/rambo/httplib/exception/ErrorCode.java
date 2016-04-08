package com.rambo.httplib.exception;

import android.text.TextUtils;

import java.util.LinkedHashMap;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class ErrorCode {
    public static LinkedHashMap<String, String> codeMap = new LinkedHashMap();
    public static final String UNKNOW = "未知异常";
    public static final int E200 = 200;
    public static final int E201 = 201;
    public static final int E202 = 203;
    public static final int E203 = 203;
    public static final int E204 = 204;
    public static final int E205 = 205;
    public static final int E206 = 206;
    public static final int E300 = 300;
    public static final int E301 = 301;
    public static final int E302 = 302;
    public static final int E303 = 303;
    public static final int E304 = 304;
    public static final int E305 = 305;
    public static final int E306 = 306;
    public static final int E307 = 307;
    public static final int E400 = 400;
    public static final int E401 = 401;
    public static final int E403 = 403;
    public static final int E404 = 404;
    public static final int E405 = 405;
    public static final int E406 = 406;
    public static final int E407 = 407;
    public static final int E408 = 408;
    public static final int E409 = 409;
    public static final int E410 = 410;
    public static final int E411 = 411;
    public static final int E412 = 412;
    public static final int E413 = 413;
    public static final int E414 = 414;
    public static final int E415 = 415;
    public static final int E416 = 416;
    public static final int E500 = 500;
    public static final int E501 = 501;
    public static final int E502 = 502;
    public static final int E503 = 503;
    public static final int E504 = 504;
    public static final int E505 = 505;
    public static final int NOT_CONNECTION = 1101;
    public static final int TIME_OUT = 1102;
    public static final int NO_NET_WORK = 1103;
    public static final int ENTITY_ERROR = 1104;
    public static final int DOWN_FILE_RENAME_ERROR = 1105;
    public static final int DOWN_FILE_ERROR = 1106;
    public static final int DOWN_FILE_REQUEST_CACHLE = 1107;

    public ErrorCode() {
    }

    public static String getErrorMessage(int code) {
        String value = (String)codeMap.get(code + "");
        return TextUtils.isEmpty(value)?"未知异常":value;
    }

    static {
        codeMap.put("200", "服务器已成功处理了请求");
        codeMap.put("201", "请求成功并且服务器创建了新的资源");
        codeMap.put("202", "服务器已接受请求，但尚未处理");
        codeMap.put("203", "服务器已成功处理了请求，但返回的信息可能来自另一来源");
        codeMap.put("204", "服务器成功处理了请求，但没有返回任何内容");
        codeMap.put("205", "重置内容,服务器成功处理了请求，但没有返回任何内容");
        codeMap.put("206", "服务器成功处理了部分 GET请求 ");
        codeMap.put("300", "针对请求，服务器可执行多种操作");
        codeMap.put("301", "请求的资源已永久移动到新位置");
        codeMap.put("302", "服务器目前从不同位置的资源响应请求，但请求者应继续使用原有位置来进行以后的请求");
        codeMap.put("303", "请求者应当对不同的位置使用单独的 GET 请求来检索响应时，服务器返回此代码");
        codeMap.put("304", "自从上次请求后，请求的资源未修改过。 服务器返回此响应时，不会返回资源内容");
        codeMap.put("305", "请求者只能使用代理访问请求的资源。 如果服务器返回此响应，还表示请求者应使用代理 ");
        codeMap.put("307", "服务器目前从不同位置的资源响应请求，但请求者应继续使用原有位置来进行以后的请求 ");
        codeMap.put("400", "请求中有语法问题，或不能满足请求");
        codeMap.put("401", "请求要求身份验证");
        codeMap.put("403", "服务器拒绝请求");
        codeMap.put("404", "服务器找不到请求的资源");
        codeMap.put("405", "服务器禁用请求中指定的方法");
        codeMap.put("406", "无法使用请求的内容特性响应请求的资源");
        codeMap.put("407", "需要代理授权");
        codeMap.put("408", "服务器等候请求时发生超时");
        codeMap.put("409", "服务器在完成请求时发生冲突");
        codeMap.put("410", "如果请求的资源已永久删除，服务器就会返回此响应");
        codeMap.put("411", "服务器不接受不含有效内容长度标头字段的请求");
        codeMap.put("412", "服务器未满足请求者在请求中设置的其中一个前提条件");
        codeMap.put("413", "服务器无法处理请求，因为请求实体过大，超出服务器的处理能力");
        codeMap.put("414", "请求的 URI过长，服务器无法处理");
        codeMap.put("415", "请求的格式不受请求页面的支持");
        codeMap.put("416", "如果页面无法提供请求的范围，则服务器会返回此状态代码");
        codeMap.put("417", "服务器未满足期望请求标头字段的要求");
        codeMap.put("500", "服务器遇到错误，无法完成请求");
        codeMap.put("501", "服务器不具备完成请求的功能");
        codeMap.put("502", "服务器作为网关或代理，从上游服务器收到无效响应");
        codeMap.put("503", "服务器目前无法使用");
        codeMap.put("504", "服务器作为网关或代理，但是没有及时从上游服务器收到请求");
        codeMap.put("505", "服务器不支持请求中所用的 HTTP 协议版本");
        codeMap.put("1101", "无法连接服务器");
        codeMap.put("1102", "请求超时");
        codeMap.put("1103", "网络正在开小差，请检查网络设置");
        codeMap.put("1104", "服务器返回的响应体为空");
        codeMap.put("1105", "文件下载时重命名临时文件异常");
        codeMap.put("1106", "下载的文件异常");
        codeMap.put("1107", "下载文件时请求被取消");
    }
}
