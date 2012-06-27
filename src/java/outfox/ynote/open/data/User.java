/**
 * @(#)User.java, 2012-6-4. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.data;

import java.util.Date;

import net.sf.json.JSONObject;
import outfox.ynote.open.client.YNoteConstants;

/**
 * This class represents a ynote user.
 * 
 * @author licx
 */
public class User {

    /**
     * JSON field names for user 
     */
    public static final String USER_ID = "user";
    public static final String TOTAL_SIZE = "total_size";
    public static final String USED_SIZE = "used_size";
    public static final String REGISTER_TIME = "register_time";
    public static final String LAST_LOGIN_TIME = "last_login_time";
    public static final String LAST_MODIFY_TIME = "last_modify_time";
    public static final String DEFAULT_NOTEBOOK = "default_notebook";

    private String userId;

    private long totalSize;
    private long usedSize;

    private long registerTime;
    private long lastLoginTime;
    private long lastModifyTime;

    /**
     * Default notebook of the user for this application, each application would
     * have a default notebook separately
     */
    private String defaultNotebook;

    public User() {
    }

    public User(String json) {
        JSONObject jsonObj = JSONObject.fromObject(json);
        this.userId = jsonObj.getString(USER_ID);
        this.totalSize = jsonObj.getLong(TOTAL_SIZE);
        this.usedSize = jsonObj.getLong(USED_SIZE);
        this.registerTime = jsonObj.getLong(REGISTER_TIME);
        this.lastLoginTime = jsonObj.getLong(LAST_LOGIN_TIME);
        this.lastModifyTime = jsonObj.getLong(LAST_MODIFY_TIME);
        this.defaultNotebook = jsonObj.getString(DEFAULT_NOTEBOOK);
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the totalSize
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * @param totalSize the totalSize to set
     */
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * @return the usedSize
     */
    public long getUsedSize() {
        return usedSize;
    }

    /**
     * @param usedSize the usedSize to set
     */
    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    /**
     * @return the registerTime
     */
    public long getRegisterTime() {
        return registerTime;
    }

    /**
     * @param registerTime the registerTime to set
     */
    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    /**
     * @return the lastLoginTime
     */
    public long getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * @param lastLoginTime the lastLoginTime to set
     */
    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * @return the lastModifyTime
     */
    public long getLastModifyTime() {
        return lastModifyTime;
    }

    /**
     * @param lastModifyTime the lastModifyTime to set
     */
    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    /**
     * @return the defaultNotebook
     */
    public String getDefaultNotebook() {
        return defaultNotebook;
    }

    /**
     * @param defaultNotebook the defaultNotebook to set
     */
    public void setDefaultNotebook(String defaultNotebook) {
        this.defaultNotebook = defaultNotebook;
    }

    public String toString() {
        return "[User userId=" + userId
                + ", totalSize=" + totalSize
                + ", usedSize=" + usedSize
                + ", registerTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(registerTime))
                + ", lastLoginTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(lastLoginTime))
                + ", lastModifyTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(lastModifyTime))
                + ", defaultNotebook=" + defaultNotebook + "]";
    }
}
