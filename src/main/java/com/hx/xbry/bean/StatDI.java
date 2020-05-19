package com.hx.xbry.bean;

import java.io.Serializable;

/**
 * @ClassName StatDI
 * @Description 普通资料解码入库统计DI
 * @Author fmy
 * @Date 2019/10/22 11:20
 * @Version 1.0
 */
public class StatDI implements Serializable {

    private static final long serialVersionUID = 1L;

    private String DATA_TYPE;// 本环节处理后的资料编码
    private String DATA_TYPE_1;// 父代资料编码 （若前后无变化同资料编码）
    private String TT;// 报告类别:报文资料的TT项，若无此属性为空
    private String DATA_UPDATE_FLAG;// 资料更正标识:站级资料的更正表示，如BBB项或文件名中的更正标识
    private String IIIII;// 台站号
    private String LONGITUDE;// 经度
    private String LATITUDE;// 纬度
    private String HEIGHT;// 高度
    private String RECEIVE;// 资料来源:当业务系统为CTS并且环节为收集时，为台站行政区划编码，其他业务系统及环节为上游系统编码
    private String SEND;// 资料去向:下游系统编码
    private String TRAN_TIME;// 资料传输时次:资料生成传输时次
    private String DATA_TIME;// 资料业务时次:观测时次或预报时次
    private String DATA_FLOW;// 业务流程标识  BDMAIN:大数据平台主流程； BDBAK:大数据平台备份流程；
    private String SYSTEM;// 业务系统名称
    private String PROCESS_LINK;// 处理环节:业务系统关键环节
    private String PROCESS_START_TIME;// 业务开始时间
    private String PROCESS_END_TIME;// 业务结束时间
    private String FILE_NAME_O;// 原始文件名
    private String FILE_NAME_N;// 新文件名
    private String FILE_SIZE;// 文件大小(BYTE)
    private String PROCESS_STATE;// 系统处理状态
    private String BUSINESS_STATE;// 业务状态
    private String RECORD_TIME;// DI记录时间

    public StatDI() {
//        setSystem("DPC");
//        setReceive("CTS2");
//        setSend("BFDB");
//        setProcessLink("1");
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getDATA_TYPE() {
        return DATA_TYPE;
    }

    public void setDATA_TYPE(String DATA_TYPE) {
        this.DATA_TYPE = DATA_TYPE;
    }

    public String getDATA_TYPE_1() {
        return DATA_TYPE_1;
    }

    public void setDATA_TYPE_1(String DATA_TYPE_1) {
        this.DATA_TYPE_1 = DATA_TYPE_1;
    }

    public String getTT() {
        return TT;
    }

    public void setTT(String TT) {
        this.TT = TT;
    }

    public String getDATA_UPDATE_FLAG() {
        return DATA_UPDATE_FLAG;
    }

    public void setDATA_UPDATE_FLAG(String DATA_UPDATE_FLAG) {
        this.DATA_UPDATE_FLAG = DATA_UPDATE_FLAG;
    }

    public String getIIIII() {
        return IIIII;
    }

    public void setIIIII(String IIIII) {
        this.IIIII = IIIII;
    }

    public String getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(String LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public String getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(String LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public String getHEIGHT() {
        return HEIGHT;
    }

    public void setHEIGHT(String HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    public String getRECEIVE() {
        return RECEIVE;
    }

    public void setRECEIVE(String RECEIVE) {
        this.RECEIVE = RECEIVE;
    }

    public String getSEND() {
        return SEND;
    }

    public void setSEND(String SEND) {
        this.SEND = SEND;
    }

    public String getTRAN_TIME() {
        return TRAN_TIME;
    }

    public void setTRAN_TIME(String TRAN_TIME) {
        this.TRAN_TIME = TRAN_TIME;
    }

    public String getDATA_TIME() {
        return DATA_TIME;
    }

    public void setDATA_TIME(String DATA_TIME) {
        this.DATA_TIME = DATA_TIME;
    }

    public String getDATA_FLOW() {
        return DATA_FLOW;
    }

    public void setDATA_FLOW(String DATA_FLOW) {
        this.DATA_FLOW = DATA_FLOW;
    }

    public String getSYSTEM() {
        return SYSTEM;
    }

    public void setSYSTEM(String SYSTEM) {
        this.SYSTEM = SYSTEM;
    }

    public String getPROCESS_LINK() {
        return PROCESS_LINK;
    }

    public void setPROCESS_LINK(String PROCESS_LINK) {
        this.PROCESS_LINK = PROCESS_LINK;
    }

    public String getPROCESS_START_TIME() {
        return PROCESS_START_TIME;
    }

    public void setPROCESS_START_TIME(String PROCESS_START_TIME) {
        this.PROCESS_START_TIME = PROCESS_START_TIME;
    }

    public String getPROCESS_END_TIME() {
        return PROCESS_END_TIME;
    }

    public void setPROCESS_END_TIME(String PROCESS_END_TIME) {
        this.PROCESS_END_TIME = PROCESS_END_TIME;
    }

    public String getFILE_NAME_O() {
        return FILE_NAME_O;
    }

    public void setFILE_NAME_O(String FILE_NAME_O) {
        this.FILE_NAME_O = FILE_NAME_O;
    }

    public String getFILE_NAME_N() {
        return FILE_NAME_N;
    }

    public void setFILE_NAME_N(String FILE_NAME_N) {
        this.FILE_NAME_N = FILE_NAME_N;
    }

    public String getFILE_SIZE() {
        return FILE_SIZE;
    }

    public void setFILE_SIZE(String FILE_SIZE) {
        this.FILE_SIZE = FILE_SIZE;
    }

    public String getPROCESS_STATE() {
        return PROCESS_STATE;
    }

    public void setPROCESS_STATE(String PROCESS_STATE) {
        this.PROCESS_STATE = PROCESS_STATE;
    }

    public String getBUSINESS_STATE() {
        return BUSINESS_STATE;
    }

    public void setBUSINESS_STATE(String BUSINESS_STATE) {
        this.BUSINESS_STATE = BUSINESS_STATE;
    }

    public String getRECORD_TIME() {
        return RECORD_TIME;
    }

    public void setRECORD_TIME(String RECORD_TIME) {
        this.RECORD_TIME = RECORD_TIME;
    }

}
