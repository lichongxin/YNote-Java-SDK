/**
 * @(#)Note.java, 2012-2-28. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.data;

import java.util.Date;

import net.sf.json.JSONObject;
import outfox.ynote.open.client.YNoteConstants;

/**
 * This class represents a note.
 *
 * @author licx
 */
public class Note {
    /**
     * JSON field names for note 
     */
    public static final String PATH = "path";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String SOURCE = "source";
    public static final String SIZE = "size";
    public static final String CREATE_TIME = "create_time";
    public static final String MODIFY_TIME = "modify_time";
    public static final String CONTENT = "content";

    private String path;
    private String title;
    private String author;
    private String source;
    private long size;

    private long createTime;
    private long modifyTime;

    private String content;

    public Note() {
    }

    public Note(String json) {
        JSONObject jsonObj = JSONObject.fromObject(json);
        // this.path = jsonObj.getString(PATH);
        this.title = jsonObj.getString(TITLE);
        this.author = jsonObj.getString(AUTHOR);
        this.source = jsonObj.getString(SOURCE);
        this.size = jsonObj.getInt(SIZE);
        this.createTime = jsonObj.getLong(CREATE_TIME);
        this.modifyTime = jsonObj.getLong(MODIFY_TIME);
        this.content = jsonObj.getString(CONTENT);
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
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
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

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "[Notebook path=" + path
                + ", title=" + title
                + ", author=" + author
                + ", source=" + source
                + ", size=" + size
                + ", createTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(createTime * 1000))
                + ", modifyTime=" + YNoteConstants.DATE_FORMATTER.format(new Date(modifyTime * 1000))
                + "]";
    }
}
