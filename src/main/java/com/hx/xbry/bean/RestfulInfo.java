package com.hx.xbry.bean;

import java.io.Serializable;

/**
 * @ClassName RestfulInfo
 * @Description DI、EI包装类
 * @Author fmy
 * @Date 2019/10/22 11:12
 * @Version 1.0
 */
public class RestfulInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type; // 接口类型，区分不同种类的资料，需要自定义
    private String name;// 用于描述type
    private String message;// 用于描述type
    private Object fields;// 自定义字段信息
    private long occur_time;// 业务时间或资料时间

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getFields() {
        return fields;
    }

    public void setFields(Object fields) {
        this.fields = fields;
    }

    public Object getOccur_time() {
        return occur_time;
    }

    public void setOccur_time(long occur_time) {
        this.occur_time = occur_time;
    }
}
