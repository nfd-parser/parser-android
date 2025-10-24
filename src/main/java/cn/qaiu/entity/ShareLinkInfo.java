package cn.qaiu.entity;

import java.util.HashMap;
import java.util.Map;

public class ShareLinkInfo {

    private String shareKey;      // 分享键

    private String panName;       // 网盘名称
    private String type;          // 分享类型
    private String sharePassword; // 分享密码（如果存在）
    private String shareUrl;      // 原始分享链接
    private String standardUrl;   // 规范化的标准链接

    /**
     * 其他参数预定义
     * dirId: 目录ID 传入
     * auths: 认证相关 传入
     * UA: 浏览器请求头 传入
     * fileInfo: 解析成功的文件信息对象 传出
     */
    private Map<String, Object> otherParam;

    private ShareLinkInfo(Builder builder) {
        this.shareKey = builder.shareKey;
        this.panName = builder.panName;
        this.type = builder.type;
        this.sharePassword = builder.sharePassword;
        this.shareUrl = builder.shareUrl;
        this.standardUrl = builder.standardUrl;
        this.otherParam = builder.otherParam;
    }

    // Getter和Setter方法

    public String getShareKey() {
        return shareKey;
    }

    public String getPanName() {
        return panName;
    }

    public void setShareKey(String shareKey) {
        this.shareKey = shareKey;
    }

    public void setPanName(String panName) {
        this.panName = panName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSharePassword() {
        return sharePassword;
    }

    public void setSharePassword(String sharePassword) {
        this.sharePassword = sharePassword;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getStandardUrl() {
        return standardUrl;
    }

    public void setStandardUrl(String standardUrl) {
        this.standardUrl = standardUrl;
    }

    public String getCacheKey() {
        // 将type和shareKey组合成一个字符串作为缓存key
        String key = type + ":" + shareKey;
        if (type.equals("p115")) {
            key += ("_" + otherParam.get("UA").toString().hashCode());
        }
        return key;
    }

    public ShareLinkInfo setOtherParam(Map<String, Object> otherParam) {
        this.otherParam = otherParam;
        return this;
    }

    public Map<String, Object> getOtherParam() {
        return otherParam;
    }

    // 静态方法创建建造者对象
    public static ShareLinkInfo.Builder newBuilder() {
        return new ShareLinkInfo.Builder();
    }

    // 建造者类
    public static class Builder {
        public String panName;        // 分享网盘名称
        private String shareKey;      // 分享键
        private String type;          // 分享类型 (网盘模板枚举的小写)
        private String sharePassword = ""; // 分享密码（如果存在）
        private String shareUrl;      // 原始分享链接
        private String standardUrl;   // 规范化的标准链接
        private Map<String, Object> otherParam = new HashMap<>();   // 其他参数

        public Builder shareKey(String shareKey) {
            this.shareKey = shareKey;
            return this;
        }

        public Builder panName(String panName) {
            this.panName = panName;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder sharePassword(String sharePassword) {
            this.sharePassword = sharePassword;
            return this;
        }

        public Builder shareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
            return this;
        }

        public Builder standardUrl(String standardUrl) {
            this.standardUrl = standardUrl;
            return this;
        }

        public Builder otherParam(Map<String, Object> otherParam) {
            this.otherParam = otherParam;
            return this;
        }

        public ShareLinkInfo build() {
            return new ShareLinkInfo(this);
        }
    }

    @Override
    public String toString() {
        return "ShareLinkInfo{" +
                "shareKey='" + shareKey + '\'' +
                ", panName='" + panName + '\'' +
                ", type='" + type + '\'' +
                ", sharePassword='" + sharePassword + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                ", standardUrl='" + standardUrl + '\'' +
                ", otherParam='" + otherParam + '\'' +
                '}';
    }
}
