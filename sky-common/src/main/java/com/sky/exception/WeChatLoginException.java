package com.sky.exception;

public class WeChatLoginException extends BaseException {
    private Integer errCode;
    private String errMsg;

    public WeChatLoginException(Integer errCode, String errMsg) {
        super("微信登录失败，错误码：" + errCode + "，错误信息：" + errMsg + ")");
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public WeChatLoginException(String msg) {
        super(msg);
    }
}
