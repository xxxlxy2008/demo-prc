package com.demo.rpc.protocol;


import java.io.Serializable;

public class Response implements Serializable {

    private int code = 0; // 响应的错误码，正常响应为0，非0表示异常响应

    private String errMsg; // 异常信息

    private Object result; // 响应结果

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }


    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
