/**
 * @(#)Notebook.java, 2012-2-28. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.data;

import java.util.Date;

import net.sf.json.JSONObject;
import outfox.ynote.open.client.YNoteConstants;

/**
 * This class represents a notebook.
 *
 * @author licx
 */
public class Notebook {
    /**
     * JSON field names for notebook 
     */
    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final String NOTES = "notes_num";
    public static final String CREATE_TIME = "create_time";
    public static final String MODIFY_TIME = "modify_time";

    private String path;
    private String name;
    private int notesNum;

    private long createTime;
    private long modifyTime;

    public Notebook() {
    }

    public Notebook(String json) {
        JSONObject jsonObj = JSONObject.fromObject(json);
        this.path = jsonObj.getString(PATH);
        this.name = jsonObj.getString(NAME);
        this.notesNum = jsonObj.getInt(NOTES);
        this.createTime = jsonObj.getLong(CREATE_TIME);
        this.modifyTime = jsonObj.getLong(MODIFY_TIME);
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the notesNum
     */
    public int getNotesNum() {
        return notesNum;
    }

    /**
     * @param notesNum the notesNum to set
     */
    public void setNotesNum(int notesNum) {
        this.notesNum = notesNum;
    }

    /**
     * @return the createTime
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the modifyTime
     */
    public long getModifyTime() {
        return modifyTime;
    }

    /**
     * @param modifyTime the modifyTime to set
     */
    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String toString() {
        return "[Notebook path=" + path
                + ", name=" + name
                + ", notesNum=" + notesNum
                + ", createTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(createTime * 1000))
                + ", modifyTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(modifyTime * 1000))
                + "]";
    }
}
