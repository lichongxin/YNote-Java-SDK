/**
 * @(#)YNoteClient.java, 2012-2-27. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;

import outfox.ynote.open.data.Note;
import outfox.ynote.open.data.Notebook;
import outfox.ynote.open.data.Resource;
import outfox.ynote.open.data.User;

/**
 * YNote client which is used to access YNote data via the open API. For a
 * consumer application, each YNote client instance is supposed to associated
 * with a single user. And the YNote client is able to access the user's data
 * if and only if the user has granted the authorization to this consumer.
 * This class is thread safe.
 *
 * <p>See {@link http://oauth.net/} for more information about OAuth.
 *
 * @author licx
 */
public class YNoteClient {

    private final OAuthAccessor accessor;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * Construct a YNote client for a given consumer. It is supposed to construct
     * a YNoteClient instance for each user in an application.
     *
     * @param consumer oauth consumer information
     */
    public YNoteClient(OAuthConsumer consumer) {
        this.accessor = new OAuthAccessor(consumer);
    }

    /**
     * @return the oauth accessor information for this consumer
     */
    public OAuthAccessor getOAuthAccessor() {
        return accessor;
    }

    /**
     * Grant the OAuth request token and secret for this consumer based on the
     * consumer key and consumer secret.
     *
     * @return the user authorization URL
     * @throws YNoteException 
     * @throws IOException 
     */
    public String grantRequestToken(String callbackURL) throws IOException,
            YNoteException {
        lock.writeLock().lock();
        try {
            HttpResponse response = YNoteHttpUtils.doGet(
                    accessor.consumer.serviceProvider.requestTokenURL,
                    null, accessor);
            // extract the request token and token secret
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            Map<String, String> model = YNoteHttpUtils.parseOAuthResponse(content);
            accessor.requestToken = model.get(OAuth.OAUTH_TOKEN);
            accessor.tokenSecret = model.get(OAuth.OAUTH_TOKEN_SECRET);
            // compose the authorization url, add the callback url if exists
            String authorizationURL = OAuth.addParameters(
                    accessor.consumer.serviceProvider.userAuthorizationURL,
                    OAuth.OAUTH_TOKEN, accessor.requestToken);
            if (callbackURL == null || callbackURL.isEmpty()) {
                callbackURL = accessor.consumer.callbackURL;
            }
            if (callbackURL != null && !callbackURL.isEmpty()) {
                authorizationURL = OAuth.addParameters(authorizationURL,
                        OAuth.OAUTH_CALLBACK, callbackURL);
            }
            return authorizationURL;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Grant the access token and secret for this consumer based on the request
     * token and secret. When the access token is granted, the consumer could
     * access the user's data in YNote.
     *
     * <p>User must have granted the authorization for this consumer before
     * this method is invoked. Usually this method is invoked in a callback
     * method which is notified after user authorized.
     *
     * @param verifier oauth verifier
     * @throws YNoteException 
     * @throws IOException 
     */
    public void grantAccessToken(String verifier) throws IOException, YNoteException {
        lock.writeLock().lock();
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
            parameters.put(OAuth.OAUTH_VERIFIER, verifier);
            HttpResponse response = YNoteHttpUtils.doGet(
                    accessor.consumer.serviceProvider.accessTokenURL,
                    parameters, accessor);
            // extract the access token and token secret
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            Map<String, String> model = YNoteHttpUtils.parseOAuthResponse(content);
            accessor.accessToken = model.get(OAuth.OAUTH_TOKEN);
            accessor.tokenSecret = model.get(OAuth.OAUTH_TOKEN_SECRET);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Set the access token and secret with the given token and secret.
     *
     * <p>When the consumer has granted the authorization and the access token
     * is not expired, the consumer could access the user's data with previous
     * access token and secret.
     *
     * @param accessToken previous saved access token which is not expired
     * @param verifier previous saved access token secret
     */
    public void setAccessToken(String accessToken, String tokenSecret) {
        lock.writeLock().lock();
        try {
            accessor.accessToken = accessToken;
            accessor.tokenSecret = tokenSecret;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @return the base url of YNote
     */
    private String getBaseURL() {
        String[] parts =
            accessor.consumer.serviceProvider.accessTokenURL.split("oauth");
        return parts[0] + "yws/open/";
    }

    //
    // Notebook operations
    //

    /**
     * Get the user information with the current authentication. 
     *
     * @return current user information
     * @throws IOException
     * @throws YNoteException
     */
    public User getUser() throws IOException, YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "user/get.json";
            HttpResponse response = YNoteHttpUtils.doGet(url, null, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            return new User(content);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get all the notebooks for this authenticate user.
     * <p>This method calls http://note.youdao.com/yws/open/notebook/all.json
     *
     * @return notebooks of this authenticate user
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public List<Notebook> getAllNotebooks() throws IOException, YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "notebook/all.json";
            HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                    null, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            JSONArray array = JSONArray.fromObject(content);
            List<Notebook> notebooks = new ArrayList<Notebook>();
            for (int i = 0; i < array.size(); i++) {
                Notebook notebook = new Notebook(array.get(i).toString());
                notebooks.add(notebook);
            }
            return notebooks;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * List the notes under a given notebook.
     * <p>This method calls http://note.youdao.com/yws/open/notebook/list.json
     *
     * @param notebookPath
     * @return notes path under this notebook
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public List<String> listNotes(String notebookPath) throws IOException,
            YNoteException {
        lock.readLock().lock();
        try {
        String url = getBaseURL() + "notebook/list.json";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(YNoteConstants.NOTEBOOK_PARAM, notebookPath);
        HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                parameters, accessor);
        String content = YNoteHttpUtils.getResponseContent(
                response.getEntity().getContent());
        JSONArray array = JSONArray.fromObject(content);
        List<String> notes = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            notes.add(array.getString(i));
        }
        return notes;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Create a new notebook with the given name.
     * <p>This method calls http://note.youdao.com/yws/open/notebook/create.json
     *
     * @param name the notebook name
     * @return path the newly created notebook
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public String createNotebook(String name) throws IOException,
            YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "notebook/create.json";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(YNoteConstants.NAME_PARAM, name);
            HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                    parameters, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            // TODO return the notebook instance
            JSONObject json = JSONObject.fromObject(content);
            return json.getString(Notebook.PATH);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Create a notebook with the given path.
     * <p>This method calls http://note.youdao.com/yws/open/notebook/delete.json
     *
     * @param notebookPath notebook path
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public void deletedNotebook(String notebookPath) throws IOException,
            YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "notebook/delete.json";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(YNoteConstants.NOTEBOOK_PARAM, notebookPath);
            HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                    parameters, accessor);
            // release the http response
            response.getEntity().consumeContent();
        } finally {
            lock.readLock().unlock();
        }
    }

    //
    // Note operations
    //

    /**
     * Get note with the given note path.
     * <p>This method calls http://note.youdao.com/yws/open/note/get.json
     *
     * @param notePath
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public Note getNote(String notePath) throws IOException, YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "note/get.json";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(YNoteConstants.PATH_PARAM, notePath);
            HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                    parameters, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            Note note = new Note(content);
            note.setPath(notePath);
            return note;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Create a new note.
     * <p>This method calls http://note.youdao.com/yws/open/note/create.json
     *
     * @param notebookPath under which the note would be created
     * @param note note to be created, note path should be left as null
     * @return the newly create note with note path
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public Note createNote(String notebookPath, Note note) throws IOException,
            YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "note/create.json";
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(YNoteConstants.TITLE_PARAM, note.getTitle());
            parameters.put(YNoteConstants.AUTHOR_PARAM, note.getAuthor());
            parameters.put(YNoteConstants.SOURCE_PARAM, note.getSource());
            parameters.put(YNoteConstants.CONTENT_PARAM, note.getContent());
            if (!StringUtils.isBlank(notebookPath)) {
                parameters.put(YNoteConstants.NOTEBOOK_PARAM, notebookPath);
            }
            HttpResponse response = YNoteHttpUtils.doPostByMultipart(url,
                    parameters, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            JSONObject json = JSONObject.fromObject(content);
            note.setPath(json.getString(Note.PATH));
            // TODO set the create/modify time of the note
            return note;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Update a given note.
     * <p>This method calls http://note.youdao.com/yws/open/note/update.json
     *
     * @param note udpated note
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     * @throws YNoteException 
     */
    public void updateNote(Note note) throws IOException, YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "note/update.json";
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(YNoteConstants.PATH_PARAM, note.getPath());
            parameters.put(YNoteConstants.TITLE_PARAM, note.getTitle());
            parameters.put(YNoteConstants.AUTHOR_PARAM, note.getAuthor());
            parameters.put(YNoteConstants.SOURCE_PARAM, note.getSource());
            parameters.put(YNoteConstants.CONTENT_PARAM, note.getContent());
            HttpResponse response = YNoteHttpUtils.doPostByMultipart(url,
                    parameters, accessor);
            // TODO set modify time and return note
            // release the http response
            response.getEntity().consumeContent();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Move a note to the specified notebook
     * <p>This method calls http://note.youdao.com/yws/open/note/move.json
     *
     * @param notePath note to be moved
     * @param destNotebookPath destination notebook path
     * @return the new path of the note
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     */
    public String moveNote(String notePath, String destNotebookPath)
            throws IOException, YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "note/move.json";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(YNoteConstants.PATH_PARAM, notePath);
            parameters.put(YNoteConstants.NOTEBOOK_PARAM, destNotebookPath);
            HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                    parameters, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            JSONObject json = JSONObject.fromObject(content);
            return json.getString(Note.PATH);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Delete a specified note.
     * <p>This method calls http://note.youdao.com/yws/open/note/delete.json
     *
     * @param notePath path of the note to be deleted
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     */
    public void deleteNote(String notePath) throws IOException, YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "note/delete.json";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(YNoteConstants.PATH_PARAM, notePath);
            HttpResponse response = YNoteHttpUtils.doPostByUrlEncoded(url,
                    parameters, accessor);
            // release the http response
            response.getEntity().consumeContent();
        } finally {
            lock.readLock().unlock();
        }
    }

    //
    // Resource operations
    //

    /**
     * Upload a resource
     *
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws OAuthException 
     */
    public Resource uploadResource(File resource) throws IOException,
            YNoteException {
        lock.readLock().lock();
        try {
            String url = getBaseURL() + "resource/upload.json";
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(YNoteConstants.FILE_PARAM, resource);
            HttpResponse response = YNoteHttpUtils.doPostByMultipart(url,
                    parameters, accessor);
            String content = YNoteHttpUtils.getResponseContent(
                    response.getEntity().getContent());
            JSONObject json = JSONObject.fromObject(content);
            if (json.containsKey(Resource.SRC)) {
                // attachment
                return new Resource(json.getString(Resource.URL),
                        json.getString(Resource.SRC));
            } else {
                // image
                return new Resource(json.getString(Resource.URL));
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Download the resource with the given url. An input stream for this
     * resource would be returned, and it's the caller's responsibility to
     * close the input stream to release the response resource.
     *
     * @param url resource url
     * @return resource body stream
     * @throws OAuthException
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream downloadResource(String url) throws
            IOException, YNoteException {
        lock.readLock().lock();
        try {
            HttpResponse response = YNoteHttpUtils.doGet(url, null, accessor);
            return response.getEntity().getContent();
        } finally {
            lock.readLock().unlock();
        }
    }
}
