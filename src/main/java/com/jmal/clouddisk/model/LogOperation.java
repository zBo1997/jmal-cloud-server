package com.jmal.clouddisk.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @Description 操作日志
 * @author jmal
 */
@Data
@Document(collection = "log")
@CompoundIndexes({
        @CompoundIndex(name = "createTime_1", def = "{'createTime': 1}"),
        @CompoundIndex(name = "time_1", def = "{'time': 1}"),
        @CompoundIndex(name = "username_1", def = "{'username': 1}"),
        @CompoundIndex(name = "showName_1", def = "{'showName': 1}"),
        @CompoundIndex(name = "ip_1", def = "{'ip': 1}"),
        @CompoundIndex(name = "cityIp_1", def = "{'cityIp': 1}"),
        @CompoundIndex(name = "url_1", def = "{'url': 1}"),
        @CompoundIndex(name = "status_1", def = "{'status': 1}"),
        @CompoundIndex(name = "operationModule_1", def = "{'operationModule': 1}"),
        @CompoundIndex(name = "operationFun_1", def = "{'operationFun': 1}"),
        @CompoundIndex(name = "deviceModel_1", def = "{'deviceModel': 1}"),
        @CompoundIndex(name = "operatingSystem_1", def = "{'operatingSystem': 1}"),
        @CompoundIndex(name = "browser_1", def = "{'operatingSystem': 1}"),
        @CompoundIndex(name = "type_1", def = "{'type': 1}"),
        @CompoundIndex(name = "type_createTime_1", def = "{'type': 1, 'createTime': 1}"),
        @CompoundIndex(name = "type_username_1", def = "{'type': 1, 'username': 1}"),
        @CompoundIndex(name = "type_username_createTime_1", def = "{'type': 1, 'username': 1, 'createTime': 1}"),
})
public class LogOperation {
    private String id;
    /***
     * 账号
     */
    private String username;
    /***
     * 用户名
     */
    private String showName;
    /***
     * ip地址
     */
    private String ip;
    /***
     * 操作模块
     */
    private String operationModule;
    /***
     * 操作功能
     */
    private String operationFun;
    /***
     * 请求地址
     */
    private String url;
    /***
     * 请求方式
     */
    private String method;
    /***
     * 设备型号
     */
    private String deviceModel;
    /***
     * 操作系统
     */
    private String operatingSystem;
    /***
     * 浏览器
     */
    private String browser;
    /***
     * 耗时
     */
    private Long time;
    /***
     * 状态
     */
    private Integer status;
    /***
     * 操作时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /***
     * 备注
     */
    private String remarks;
    /***
     * 日志类型
     */
    private String type;

    private IpInfo ipInfo;

    @Data
    public static class IpInfo {
        /***
         * 国家
         */
        private String country;
        /***
         * 区域
         */
        private String area;
        /***
         * 省份
         */
        private String province;
        /***
         * 城市
         */
        private String city;
        /***
         * 运营商
         */
        private String operators;
    }

    /***
     * 日志类型
     */
    public enum Type {
        /***
         * 登录日志
         */
        LOGIN,
        /***
         * 浏览日志
         */
        BROWSE,
        /***
         * 操作日志
         */
        OPERATION,
        /***
         * 文章访问日志
         */
        ARTICLE,
        /***
         * webdav
         */
        WEBDAV
    }
}
