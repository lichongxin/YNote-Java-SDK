/**
 * @(#)YNoteConstants.java, 2012-2-28. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.client;

import java.text.SimpleDateFormat;

/**
 * Constants used in YNote client
 *
 * @author licx
 */
public class YNoteConstants {

    public static final String ENCODING = "UTF-8";

    /**
     * YNote Service URLS
     */
    public static final String REQUEST_TOKEN_URL = "http://note.youdao.com/oauth/request_token";
    public static final String USER_AUTHORIZATION_URL = "http://note.youdao.com/oauth/authorize";
    public static final String ACCESS_TOKEN_URL = "http://note.youdao.com/oauth/access_token";

    /**
     * YNote Sandbox Service URLS
     */
    public static final String SANDBOX_REQUEST_TOKEN_URL = "http://sandbox.note.corp.youdao.com/oauth/request_token";
    public static final String SANDBOX_USER_AUTHORIZATION_URL = "http://sandbox.note.corp.youdao.com/oauth/authorize";
    public static final String SANDBOX_ACCESS_TOKEN_URL = "http://sandbox.note.corp.youdao.com/oauth/access_token";

    /**
     * Request parameter names
     */
    public static final String NOTEBOOK_PARAM = "notebook";
    public static final String NAME_PARAM = "name";
    public static final String PATH_PARAM = "path";
    public static final String SOURCE_PARAM = "source";
    public static final String AUTHOR_PARAM = "author";
    public static final String TITLE_PARAM = "title";
    public static final String CONTENT_PARAM = "content";
    public static final String FILE_PARAM = "file";

    public static final SimpleDateFormat DATE_FORMATTER =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
}
