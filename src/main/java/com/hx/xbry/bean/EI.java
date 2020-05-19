package com.hx.xbry.bean;

import java.io.Serializable;

/**
 * <br>
 *
 * @author wuzuoqiang
 * @Title EI.java
 * @Package org.cimiss2.dwp.RADAR.bean
 * @Description  EI实体类
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Engineer    Description
 * ------------ ----------- --------------------------
 * 2017年12月15日 下午5:05:15   wuzuoqiang    Initial creation.
 * </pre>
 */
public class EI implements Serializable {
    private static final long serialVersionUID = 1L;

    private String SYSTEM;// 业务系统名称	业务系统名称，如CTS

    private String GROUP_ID;// 告警业务分组标识	事件业务分类（监视点类别属性编号），如OP_DPC_Z_99

    private String ORG_TIME;// 文本生成时间	数据生成时间。包含年、月、日、小时、分、秒。时间格式为 YYYYMMDDThhmmss

    private String MSG_TYPE;// 内容类型	用来标示信息类型 03：告警事件信息；99：未知

    private String COL_TYPE;// 采集方式 01:Rest API 02：消息；

    private String DATA_FROM;// 数据来源	各省4位编码。国家级：BABJ

    private String EVENT_TYPE;// 事件类型	事件编号，命名规则详见第2章，如OP_DPC_Z-1-25-09

    private String EVENT_LEVEL;// 事件级别	0:正常，1:提醒，2:警告，3:紧急

    private String EVENT_TITLE;// 事件标题	最大长度为255字节

    private String KObject;//故障对象 发生告警的具体对象，例如某个业务环节、进程、设备等等

    private String KEvent;//故障内容	发生的故障内容

    private String KResult;// 故障结果	导致的结果，例如某些业务异常

    private String KIndex;// 关键信息 关键索引信息,例如发生告警的进程名称、资料编号或名称、算法名称等

    private String KComment;// 备注信息	备注信息。用于对该条告警进行补充说明

    private String EVENT_TIME;// 事件发生时间 2017-06-16 12:33:00

    private String EVENT_SUGGEST;// 事件处理建议	事件处理建议

    private String EVENT_CONTROL;// 事件处理导向	事件处理导向

    private String EVENT_TRAG;// 事件发生可能原因 事件发生可能原因

    private String EVENT_EXT1;// 气象告警文件名称 当事件类型为气象告警时，使用该字段描述气象告警的文件名称

    private String EVENT_EXT2;// 事件发生节点名称	告警发生所在的设备主机名称

    public void setSYSTEM(String sYSTEM) {
        SYSTEM = sYSTEM;
    }

    public void setGROUP_ID(String gROUP_ID) {
        GROUP_ID = gROUP_ID;
    }

    public void setORG_TIME(String oRG_TIME) {
        ORG_TIME = oRG_TIME;
    }

    public void setMSG_TYPE(String mSG_TYPE) {
        MSG_TYPE = mSG_TYPE;
    }

    public void setCOL_TYPE(String cOL_TYPE) {
        COL_TYPE = cOL_TYPE;
    }

    public void setDATA_FROM(String dATA_FROM) {
        DATA_FROM = dATA_FROM;
    }

    public void setEVENT_TYPE(String eVENT_TYPE) {
        EVENT_TYPE = eVENT_TYPE;
    }

    public void setEVENT_LEVEL(String eVENT_LEVEL) {
        EVENT_LEVEL = eVENT_LEVEL;
    }

    public void setEVENT_TITLE(String eVENT_TITLE) {
        EVENT_TITLE = eVENT_TITLE;
    }

    public void setKObject(String kObject) {
        KObject = kObject;
    }

    public void setKEvent(String kEvent) {
        KEvent = kEvent;
    }

    public void setKResult(String kResult) {
        KResult = kResult;
    }

    public void setKIndex(String kIndex) {
        KIndex = kIndex;
    }

    public void setKComment(String kComment) {
        KComment = kComment;
    }

    public void setEVENT_TIME(String eVENT_TIME) {
        EVENT_TIME = eVENT_TIME;
    }

    public void setEVENT_SUGGEST(String eVENT_SUGGEST) {
        EVENT_SUGGEST = eVENT_SUGGEST;
    }

    public void setEVENT_CONTROL(String eVENT_CONTROL) {
        EVENT_CONTROL = eVENT_CONTROL;
    }

    public void setEVENT_TRAG(String eVENT_TRAG) {
        EVENT_TRAG = eVENT_TRAG;
    }

    public void setEVENT_EXT1(String eVENT_EXT1) {
        EVENT_EXT1 = eVENT_EXT1;
    }

    public void setEVENT_EXT2(String eVENT_EXT2) {
        EVENT_EXT2 = eVENT_EXT2;
    }

    public String getSYSTEM() {
        return SYSTEM;
    }

    public String getGROUP_ID() {
        return GROUP_ID;
    }

    public String getORG_TIME() {
        return ORG_TIME;
    }

    public String getMSG_TYPE() {
        return MSG_TYPE;
    }

    public String getCOL_TYPE() {
        return COL_TYPE;
    }

    public String getDATA_FROM() {
        return DATA_FROM;
    }

    public String getEVENT_TYPE() {
        return EVENT_TYPE;
    }

    public String getEVENT_LEVEL() {
        return EVENT_LEVEL;
    }

    public String getEVENT_TITLE() {
        return EVENT_TITLE;
    }

    public String getKObject() {
        return KObject;
    }

    public String getKEvent() {
        return KEvent;
    }

    public String getKResult() {
        return KResult;
    }

    public String getKIndex() {
        return KIndex;
    }

    public String getKComment() {
        return KComment;
    }

    public String getEVENT_TIME() {
        return EVENT_TIME;
    }

    public String getEVENT_SUGGEST() {
        return EVENT_SUGGEST;
    }

    public String getEVENT_CONTROL() {
        return EVENT_CONTROL;
    }

    public String getEVENT_TRAG() {
        return EVENT_TRAG;
    }

    public String getEVENT_EXT1() {
        return EVENT_EXT1;
    }

    public String getEVENT_EXT2() {
        return EVENT_EXT2;
    }

}
