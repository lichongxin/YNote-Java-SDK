/**
 * @(#)YNoteHttpUtils.java, 2012-3-2. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;


/**
 * This class provides the http utility methods for YNote OAuth client.
 *
 * @author licx
 */
public class YNoteHttpUtils {

    private static final HttpClient client = new DefaultHttpClient();

    /**
     * Do a http get for the given url.
     *
     * @param url requested url
     * @param parameters request parameters
     * @param accessor oauth accessor
     * @return the http response
     * @throws IOException
     * @throws {@link YNoteException}
     */
    public static HttpResponse doGet(String url, Map<String, String> parameters,
            OAuthAccessor accessor) throws IOException, YNoteException {
        // add ynote parameters to the url
        OAuth.addParameters(url, parameters == null ? null : parameters.entrySet());
        HttpGet get = new HttpGet(url);
        // sign all parameters, including oauth parameters and ynote parameters
        // and add the oauth related information into the header        
        Header oauthHeader = getAuthorizationHeader(url, OAuthMessage.GET,
                parameters, accessor);
        get.addHeader(oauthHeader);
        HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        get.setParams(params);
        HttpResponse response = client.execute(get);
        if ((response.getStatusLine().getStatusCode() / 100) != 2) {
            YNoteException e = wrapYNoteException(response);
            throw e;
        }
        return response;
    }

    /**
     * Do a http post with url encoded content type.
     *
     * @param url
     * @param formParams
     * @param accessor
     * @return
     * @throws IOException
     * @throws YNoteException
     */
    public static HttpResponse doPostByUrlEncoded(String url,
            Map<String, String> formParams, OAuthAccessor accessor)
            throws IOException, YNoteException {
        HttpPost post = new HttpPost(url);
        // for url encoded post, sign all the parameters, including oauth
        // parameters and form parameters
        Header oauthHeader = getAuthorizationHeader(url, OAuthMessage.POST,
                formParams, accessor);
        if (formParams != null) {
            // encode our ynote parameters
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for(Entry<String, String> entry : formParams.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, YNoteConstants.ENCODING);
            post.setEntity(entity);
        }
        post.addHeader(oauthHeader);
        HttpResponse response = client.execute(post);
        if ((response.getStatusLine().getStatusCode() / 100) != 2) {
            YNoteException e = wrapYNoteException(response);
            throw e;
        }
        return response;
    }

    /**
     * Do a http post with the multipart content type. This method is usually
     * used to upload the large size content, such as uploading a file.
     *
     * @param url
     * @param formParams
     * @param accessor
     * @return
     * @throws IOException
     * @throws YNoteException
     */
    public static HttpResponse doPostByMultipart(String url,
            Map<String, Object> formParams, OAuthAccessor accessor)
            throws IOException, YNoteException {
        HttpPost post = new HttpPost(url);
        // for multipart encoded post, only sign with the oauth parameters
        // do not sign the our form parameters
        Header oauthHeader = getAuthorizationHeader(url, OAuthMessage.POST,
                null, accessor);
        if (formParams != null) {
            // encode our ynote parameters
            MultipartEntity entity =
                new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (Entry<String, Object> parameter : formParams.entrySet()) {
                if (parameter.getValue() instanceof File) {
                    // deal with file particular
                    entity.addPart(parameter.getKey(),
                            new FileBody((File)parameter.getValue()));
                } else if (parameter.getValue() != null){
                    entity.addPart(parameter.getKey(), new StringBody(
                            parameter.getValue().toString(),
                            Charset.forName(YNoteConstants.ENCODING)));
                }
            }
            post.setEntity(entity);
        }
        post.addHeader(oauthHeader);
        HttpResponse response = client.execute(post);
        if ((response.getStatusLine().getStatusCode() / 100) != 2) {
            YNoteException e = wrapYNoteException(response);
            throw e;
        }
        return response;
    }

    /**
     * Get the OAuth authorization header for the given url, parameters and
     * accessor.
     *
     * @param url
     * @param parameters
     * @param accessor
     * @return
     * @throws IOException
     */
    private static Header getAuthorizationHeader(String url, String method,
            Map<String, String> parameters, OAuthAccessor accessor)
            throws IOException {
        try {
            OAuthMessage message = accessor.newRequestMessage(method,
                    url, parameters == null ? null : parameters.entrySet());
            // System.out.println(OAuthSignatureMethod.getBaseString(message));
            // System.out.println(message.getAuthorizationHeader(null));
            return new BasicHeader("Authorization",
                    message.getAuthorizationHeader(null));
        } catch (OAuthException e) {
            throw new IOException("Fail to signature", e);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL", e);
        }
    }

    /**
     * Wrap the response as a {@link YNoteException} if the response status
     * code is above 200.
     *
     * @param response
     * @return
     * @throws IOException
     */
    private static YNoteException wrapYNoteException(HttpResponse response)
            throws IOException {
        int status = response.getStatusLine().getStatusCode();
        InputStream body = response.getEntity().getContent();
        if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            // server error, usually response in json, parse the error
            String content = getResponseContent(body);
            try {
                JSONObject json = JSONObject.fromObject(content);
                int errorCode = json.getInt(YNoteException.ERROR);
                if (errorCode >= 1000) {
                    return new YNoteOAuthException(json);
                } else {
                    return new YNoteException(json);
                }
            } catch (JSONException e) {
                // could not parse the error message as json
                throw new IOException(content);
            }
        } else if (body != null) {
            String content = getResponseContent(body);
            throw new IOException(content);
        } else {
            throw new RuntimeException(response.toString());
        }
    }

    /**
     * Get the response content as a string.
     *
     * @param response
     * @return
     * @throws IOException
     */
    public static String getResponseContent(InputStream response)
            throws IOException {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = response.read(buffer))) {
                bytes.write(buffer, 0, n);
            }
            bytes.close();
            return new String(bytes.toByteArray(), YNoteConstants.ENCODING);
        } finally {
            // release the http response
            response.close();
        }
    }

    /**
     * Parse the OAuth response content into a map.
     * @param content
     * @return
     */
    public static Map<String, String> parseOAuthResponse(String content) {
        Map<String, String> map = new HashMap<String, String>();
        if (content != null && !content.isEmpty()) {
            String[] items = content.split("&");
            for (String item : items) {
                int index = item.indexOf("=");
                String key = item.substring(0, index);
                String value = item.substring(index + 1);
                map.put(key, value);
            }
        }
        return map;
    }
}
