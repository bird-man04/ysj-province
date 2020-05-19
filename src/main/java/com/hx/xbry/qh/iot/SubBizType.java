package com.hx.xbry.qh.iot;

/**
 * @ClassName SUB_VERSION
 * @Description 业务子类类型
 * @Author fmy
 * @Date 2020/5/8 15:39
 * @Version 1.0
 */
public enum SubBizType {
    AIOS("弹药出入库信息"),
    AMTI("弹药运输信息"),
    DSOI("派发单信息"),
    GSRI("地面作业实时记录信息");

    private final String description;

    SubBizType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SubBizType{" +
                "description='" + description + '\'' +
                '}';
    }

}
