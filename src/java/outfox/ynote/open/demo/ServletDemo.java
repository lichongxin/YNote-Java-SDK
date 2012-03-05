/**
 * @(#)WebDemo.java, 2012-2-27. 
 * 
 * Copyright 2012 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package outfox.ynote.open.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import outfox.ynote.open.client.YNoteClient;
import outfox.ynote.open.client.YNoteConstants;
import outfox.ynote.open.client.YNoteException;

/**
 * This class illustrates how to invoke the 'get all notebooks' interface by a
 * servlet.
 *
 * @author licx
 */
public class ServletDemo {

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

    private static final String CONSUMER_KEY = "Your Key";
    private static final String CONSUMER_SECRET = "Your Secret";
    // sandbox consumer
    private static final OAuthConsumer CONSUMER = new OAuthConsumer(null,
            CONSUMER_KEY, CONSUMER_SECRET, SANDBOX_SERVICE_PROVIDER);

    /**
     * This servlet is used to get all notebooks of a user.
     *
     */
    public static class NotebookServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            // callback URL for authorization, includes the the callback servlet
            // url as well as this servlet url so that the callback url could
            // redirect to this servlet after authorized
            String callbackURL = "";
            YNoteClient client = new YNoteClient(CONSUMER);
            try {
                // 1. extract the accessor information if exists, usually the
                // accessor information is passed in cookies

                // 2. call the 'get all notebooks' interface
                client.getAllNotebooks();
            } catch (YNoteException e) {
                if (e.getErrorCode() == 307) {
                    try {
                        // 3. the application is not authorized, get the request
                        // token and redirect to the authorization url
                        String userAuthorizationURL = client.grantRequestToken(callbackURL);
                        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        response.setHeader("Location", userAuthorizationURL);
                    } catch (YNoteException e2) {
                        throw new IOException(e2);
                    }
                }
            }
            
        }
    }

    /**
     * This servlet is used to get the access token and secret when the use
     * has finished the authorization.
     */
    public static class CallbackServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            // 1. extract accessor information, including request token and secret
            // which is usually passed by cookie

            // 2. extract the verifier from the response

            // 3. get the access token and secret

            // 4. redirect to the original servlet url, in this case should be
            // NotebookServlet
        }
    }
}
