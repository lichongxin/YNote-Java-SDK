/**
 * @(#)DesktopDemo.java, 2012-2-27. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.demo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import outfox.ynote.open.client.YNoteClient;
import outfox.ynote.open.client.YNoteConstants;
import outfox.ynote.open.client.YNoteException;
import outfox.ynote.open.data.Note;
import outfox.ynote.open.data.Notebook;
import outfox.ynote.open.data.Resource;
import outfox.ynote.open.data.User;

/**
 * Demo which illustrate how all the interfaces of ynote client is used for
 * a desktop application.
 *
 * @author licx
 */
public class DesktopDemo {

    // YNote online environment
    private static final OAuthServiceProvider SERVICE_PROVIDER =
        new OAuthServiceProvider(YNoteConstants.REQUEST_TOKEN_URL,
                YNoteConstants.USER_AUTHORIZATION_URL,
                YNoteConstants.ACCESS_TOKEN_URL);

    // YNote sandbox environment
    private static final OAuthServiceProvider SANDBOX_SERVICE_PROVIDER =
        new OAuthServiceProvider(YNoteConstants.SANDBOX_REQUEST_TOKEN_URL,
                YNoteConstants.SANDBOX_USER_AUTHORIZATION_URL,
                YNoteConstants.SANDBOX_ACCESS_TOKEN_URL);

    private static final String CONSUMER_KEY = "your key";
    private static final String CONSUMER_SECRET = "your secret";
    // sandbox consumer
    private static final OAuthConsumer CONSUMER = new OAuthConsumer(null,
            CONSUMER_KEY, CONSUMER_SECRET, SANDBOX_SERVICE_PROVIDER);

    private static YNoteClient client = new YNoteClient(CONSUMER);

    public static void main(String[] args) throws Exception {
        
        // load the save the access token if exists
        File file = new File("conf", "access_token");
        if (file.exists()) {
            Scanner input = new Scanner(file);
            String accessToken = null;
            String tokenSecret = null;
            if (input.hasNext()) {
                accessToken = input.nextLine();
            }
            if (input.hasNext()) {
                tokenSecret = input.nextLine();
            }
            if (accessToken != null && tokenSecret != null) {
                client.setAccessToken(accessToken, tokenSecret);
            }
        }

        // get the user information
        User user = getUser();
        System.out.println("Get user information: ");
        System.out.println(user);
        List<Notebook> notebooks = getAllNotebooks();
        System.out.println(notebooks);
        String notebook = createNotebook("New_Notebook");
        System.out.println("New Notebook is create " + notebook);
        // create a note under this notebook
        Note note = createNote(notebooks.get(0).getPath());
        System.out.println("New Note is create " + note);
        // list the new notebook, there should be one note
        List<String> notes = listNotes(notebook);
        System.out.println("Notes under the new notebook " + notes);
        // move the new note to another notebook
        Notebook anotherNotebook = notebooks.get(0);
        String newNotePath = moveNote(note.getPath(), anotherNotebook.getPath());
        System.out.println("Note " + note.getPath() + " is moved to " + newNotePath);
        // get the note with new note path
        note = getNote(newNotePath);
        System.out.println("Get the note with moved path " + newNotePath);
        // upload a image resource as well as a attachment resource
        Resource resource1 = uploadResource(new File("conf", "access_token"));
        System.out.println("Upload attachment resource");
        Resource resource2 = uploadResource(downloadImage());
        System.out.println("Upload image resource");
        // modify the content of the note to include these two resource
        String content = resource1.toResourceTag() + "<br>" + resource2.toResourceTag();
        note.setContent(content);
        updateNote(note);
        System.out.println("Upload the note to include the two resources");
        // download the resource
        byte[] bytes = downloadResource(resource1.getUrl());
        FileOutputStream output = new FileOutputStream(new File("download-file"));
        output.write(bytes);
        output.close();
        // delete the note
        deleteNote(newNotePath);
        System.out.println("Delete the note " + newNotePath);
        // delete the notebook
        deleteNotebook(notebook);
        System.out.println("Delete the notebook " + notebook);
        // create a note under the application's default notebook
        createNote(null);
        System.out.println("Create a note under application's default notebook");
    }

    private static File downloadImage() throws IOException {
        URL url = new URL("http://note.youdao.com/styles/images/case-1.png");
        byte[] bytes = toBytes(url.openStream());
        File image = new File("case-1.png");
        FileOutputStream output = new FileOutputStream(image);
        output.write(bytes);
        output.close();
        return image;
    }

    /**
     * Do OAuth authorization
     *
     * @param <T> return type of the callback method
     * @param callback method to be called when authorization is finished
     * @return
     * @throws Exception
     */
    public static <T> T doOAuth(Callable<T> callback) throws Exception {
        String userAuthorizationURL = client.grantRequestToken(null);
        BareBonesBrowserLaunch.browse(userAuthorizationURL);
        // waiting for the user to finished the authorization

        System.out.print("Put your pin code here:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String verifier = br.readLine();
        client.grantAccessToken(verifier);

        OAuthAccessor accessor = client.getOAuthAccessor();
        PrintStream output = new PrintStream(new File("conf", "access_token"));
        output.println(accessor.accessToken);
        output.println(accessor.tokenSecret);
        output.close();
        /*
         *  you could save the access token and secret here and use it next time
         *  before the access token is expired, for example:
         *  YNoteClient ynoteClient2 =new YNoteClient(consumer);
         *  ynoteClient2.setAccessToken(accessToken, tokenSecret);
         */
        return callback.call();
    }

    private static User getUser() throws Exception {
        User user = null;
        try {
            user = client.getUser();
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                user = doOAuth(new Callable<User>() {
                    @Override
                    public User call() throws Exception {
                        return client.getUser();
                    }
                });
            } else {
                throw e;
            }
        }
        return user;
    }

    private static List<Notebook> getAllNotebooks() throws Exception {
        List<Notebook> notebooks = new ArrayList<Notebook>();
        try {
            notebooks = client.getAllNotebooks();
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                notebooks = doOAuth(new Callable<List<Notebook>>() {
                    @Override
                    public List<Notebook> call() throws Exception {
                        return client.getAllNotebooks();
                    }
                });
            } else {
                throw e;
            }
        }
        return notebooks;
    }

    private static List<String> listNotes(final String notebook) throws Exception {
        List<String> notes = new ArrayList<String>();
        try {
            notes = client.listNotes(notebook);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                notes = doOAuth(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        return client.listNotes(notebook);
                    }
                });
            } else {
                throw e;
            }
        }
        return notes;
    }

    private static String createNotebook(final String name) throws Exception {
        String notebook = null;
        try {
            notebook = client.createNotebook(name);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                notebook = doOAuth(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return client.createNotebook(name);
                    }
                });
            } else {
                throw e;
            }
        }
        return notebook;
    }

    private static void deleteNotebook(final String notebook) throws Exception {
        try {
            client.deletedNotebook(notebook);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                doOAuth(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        client.deletedNotebook(notebook);
                        return null;
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static Note createNote(final String notebookPath) throws Exception {
        final Note note = new Note();
        note.setAuthor("Li Lei");
        note.setSize(100);
        note.setSource("www.youdao.com");
        note.setTitle("����");
        note.setContent("����");
        try {
            return client.createNote(notebookPath, note);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                return doOAuth(new Callable<Note>() {
                    @Override
                    public Note call() throws Exception {
                        return client.createNote(notebookPath, note);
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static Note getNote(final String notePath) throws Exception {
        try {
            return client.getNote(notePath);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                return doOAuth(new Callable<Note>() {
                    @Override
                    public Note call() throws Exception {
                        return client.getNote(notePath);
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static void updateNote(final Note note)
            throws Exception {
        try {
            client.updateNote(note);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                doOAuth(new Callable<Note>() {
                    @Override
                    public Note call() throws Exception {
                        client.updateNote(note);
                        return null;
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static String moveNote(final String notePath, final String destNotebookPath)
            throws Exception {
        try {
            return client.moveNote(notePath, destNotebookPath);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                return doOAuth(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return client.moveNote(notePath, destNotebookPath);
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static void deleteNote(final String notePath) throws Exception {
        try {
            client.deleteNote(notePath);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                doOAuth(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        client.deleteNote(notePath);
                        return null;
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static Resource uploadResource(final File resource) throws Exception {
        try {
            return client.uploadResource(resource);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                return doOAuth(new Callable<Resource>() {
                    @Override
                    public Resource call() throws Exception {
                        return client.uploadResource(resource);
                    }
                });
            } else {
                throw e;
            }
        }
    }

    private static byte[] downloadResource(final String url) throws Exception {
        InputStream body = null;
        try {
            body = client.downloadResource(url);
        } catch (YNoteException e) {
            if (e.getErrorCode() == 307 || e.getErrorCode() == 207) {
                body = doOAuth(new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        return client.downloadResource(url);
                    }
                });
            } else {
                throw e;
            }
        }
        return toBytes(body);
    }

    private static byte[] toBytes(InputStream input) throws IOException {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int n = -1;
            while ((n = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
            }
            bytes.close();
            return bytes.toByteArray();
        } finally {
            input.close();
        }
    }
}
