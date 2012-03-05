/**
 * @(#)YNoteException.java, 2012-2-29. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.client;

import net.sf.json.JSONObject;


/**
 * Exception throwed by YNote open API
 *
 * @author licx
 */
public class YNoteException extends Exception {
    protected static final String ERROR = "error";
    protected static final String MESSAGE = "message";

    private static final long serialVersionUID = 1L;

    protected int errorCode;

    public YNoteException(JSONObject json) {
        super(json.getString(YNoteException.MESSAGE));
        this.errorCode = json.getInt(YNoteException.ERROR);
    }

    public YNoteException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public YNoteException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public YNoteException(Throwable cause) {
        super(cause);
    }

    public int getErrorCode() {
        return errorCode;
    }
}
