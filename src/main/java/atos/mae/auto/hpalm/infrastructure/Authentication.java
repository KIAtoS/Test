package atos.mae.auto.hpalm.infrastructure;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Authentication {

	/**
	 * Logger.
	 */
	private static Logger Log = LoggerFactory.getLogger(Authentication.class);

	private RestConnector connector = RestConnector.getInstance();

	public boolean login(String loginUrl, String username, String password) throws Exception {
        //create a string that lookes like:
        // "Basic ((username:password)<as bytes>)<64encoded>"
        byte[] credBytes = (username + ":" + password).getBytes();
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

        Map<String, String> map = new HashMap<String, String>();
        map.put("Authorization", credEncodedString);

        Response response = this.connector.httpGet(loginUrl, null, map);

        boolean ret = response.getStatusCode() == HttpURLConnection.HTTP_OK;

        return ret;
	}

	/**
     * @return null if authenticated.<br>
     *         a url to authenticate against if not authenticated.
     * @throws Exception
     */
    public String isAuthenticated() throws Exception {

        String isAuthenticateUrl = this.connector.buildUrl("rest/is-authenticated");
        String ret;

        Response response = this.connector.httpGet(isAuthenticateUrl, null, null);
        int responseCode = response.getStatusCode();

        //if already authenticated
        if (responseCode == HttpURLConnection.HTTP_OK) {

            ret = null;
        }

        //if not authenticated - get the address where to authenticate
        // via WWW-Authenticate
        else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {

            Iterable<String> authenticationHeader =
                    response.getResponseHeaders().get("WWW-Authenticate");

            String newUrl =
                authenticationHeader.iterator().next().split("=")[1];
            newUrl = newUrl.replace("\"", "");
            //rajouter des replace pour enlever le port :80 et transformer http en https
            newUrl = newUrl.replace("http:", "https:");
            newUrl = newUrl.replace(":80", "");
            newUrl += "/authenticate";
            ret = newUrl;
        }

        //Not ok, not unauthorized. An error, such as 404, or 500
        else {

            throw response.getFailure();
        }

        return ret;
    }

    /**
     * @return string cookie list.<br>
     * @throws Exception
     */
    public String siteSession() throws Exception {
	    String newSessionUrl = this.connector.buildUrl("rest/site-session");
	    String ret;

	    Response response = this.connector.httpPost(newSessionUrl, null, null);
	    int responseCode = response.getStatusCode();

	    //CPD contr�ler la r�ponse (201) et r�cup�rer les cookies
	    if (responseCode == HttpURLConnection.HTTP_CREATED) {

	    String cookie;
	    cookie = this.connector.getCookieString();
	    ret = cookie;

	    }

	    //Not ok, not unauthorized. An error, such as 404, or 500
	    else {

	        throw response.getFailure();
	    }

	    return ret;

	}

    /**
     * @return string cookie list.<br>
     * @throws Exception
     */
    public Map<String, String> cookieSession() throws Exception {

        String newSessionUrl = this.connector.buildUrl("rest/site-session");
        Map<String, String> ret;

        Response response = this.connector.httpPost(newSessionUrl, null, null);
        int responseCode = response.getStatusCode();

        //CPD contr�ler la r�ponse (201) et r�cup�rer les cookies
        if (responseCode == HttpURLConnection.HTTP_CREATED) {

        Map<String, String> cookieMAP = new HashMap<String, String>();
        cookieMAP = this.connector.getCookies();
        ret = cookieMAP;

        }

        //Not ok, not unauthorized. An error, such as 404, or 500
        else {

            throw response.getFailure();
        }

        return ret;

    }

    /**
     * @return true if logout successful
     * @throws Exception
     *             close session on server and clean session cookies on client
     */
    public boolean logout() throws Exception {

     //note the get operation logs us out by setting authentication cookies to:
     // LWSSO_COOKIE_KEY="" via server response header Set-Cookie
        Response response = this.connector.httpGet(this.connector.buildUrl("authentication-point/logout"),
                null, null);

        return (response.getStatusCode() == HttpURLConnection.HTTP_OK);

    }
}
