package com.hx.xbry.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName InitContext
 * @Description 初始化内容类
 * @Author fmy
 * @Date 2019/11/29 17:12
 * @Version 1.0
 */
public class InitContext {

    private String system;//系统名
    private String recordFile;// 记录文件名
    private JSONObject recordInitJo;// 记录初始化内容
    private String failedFile;// 失败记录文件名
    private String failedInitContent = "{}";// 失败记录初始化内容

    public InitContext(String system) {
        this.system = system;
    }

    public InitContext(String system,String recordFile) {
        this.system = system;
        this.recordFile = recordFile;
    }

    public InitContext(String system, String recordFile, JSONObject recordInitJo, String failedFile) {
        this.system = system;
        this.recordFile = recordFile;
        this.recordInitJo = recordInitJo;
        this.failedFile = failedFile;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getRecordFile() {
        return recordFile;
    }

    public void setRecordFile(String recordFile) {
        this.recordFile = recordFile;
    }

    public JSONObject getRecordInitJo() {
        return recordInitJo;
    }

    public void setRecordInitJo(JSONObject recordInitJo) {
        this.recordInitJo = recordInitJo;
    }

    public String getFailedFile() {
        return failedFile;
    }

    public void setFailedFile(String failedFile) {
        this.failedFile = failedFile;
    }

    public String getFailedInitContent() {
        return failedInitContent;
    }

    public void setFailedInitContent(String failedInitContent) {
        this.failedInitContent = failedInitContent;
    }
}
