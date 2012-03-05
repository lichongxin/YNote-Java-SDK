/**
 * @(#)YNoteOAuthException.java, 2012-3-2. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.client;

import net.oauth.OAuthException;
import net.sf.json.JSONObject;

/**
 * Exception throwed by YNote OAuth framework.
 *
 * @author licx
 */
public class YNoteOAuthException extends YNoteException {
    private static final long serialVersionUID = 1L;

    public static final int UNKNOWN_ERROR = 1000;
    public static final int NOT_AUTHORIZED_ERROR = 10000;

    private static final String OAUTH_SIGNATURE_BASE = "oauth_signature_base_string";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_SIGNATURE = "oauth_signature";

    //
    // OAuth information
    //
    private String baseString;
    private String singatureMethod;
    private String signature;

    public YNoteOAuthException(JSONObject json) {
        super(json);
        this.baseString = json.getString(OAUTH_SIGNATURE_BASE);
        this.singatureMethod = json.getString(OAUTH_SIGNATURE_METHOD);
        this.signature = json.getString(OAUTH_SIGNATURE);
    }

    public YNoteOAuthException(int errorCode, String message) {
        super(errorCode, message);
    }

    public YNoteOAuthException(OAuthException cause) {
        super(UNKNOWN_ERROR, cause.getMessage(), cause);
    }

    /**
     * @return the baseString
     */
    public String getBaseString() {
        return baseString;
    }

    /**
     * @return the singatureMethod
     */
    public String getSingatureMethod() {
        return singatureMethod;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }
}
