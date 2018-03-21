package businesslogic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exceptions.MySQLException;
import model.JsonWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

class Util {

    private static String getJson(String string) throws MySQLException {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        if (hostAvailabilityCheck()) {
            try {
                URL url = new URL(string);
                urlConn = url.openConnection();
                urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
                urlConn.setReadTimeout(60 * 1000);
                if (urlConn.getInputStream() != null) {
                    in = new InputStreamReader(urlConn.getInputStream(),
                            Charset.defaultCharset());
                    BufferedReader bufferedReader = new BufferedReader(in);
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                throw new MySQLException("Exception while calling URL:" + e.getMessage());
            }
            return sb.toString();
        } else {
            System.out.println("Error: no connection to api.ethermine.org");
        }
        return null;
    }

    private static boolean hostAvailabilityCheck() {
        try(Socket ignored = new Socket("api.ethermine.org", 443)) {
            return true;
        } catch (IOException ioe) {

        }
        return false;
    }

    static List<JsonWorker> getWorkers(String minerAddress) throws MySQLException {
        String fullJSON = getJson("https://api.ethermine.org/miner/" + minerAddress + "/workers");
        if (fullJSON != null) {
            System.out.println("https://api.ethermine.org/miner/" + minerAddress + "/workers");
            String withoutStart = fullJSON.replace("{\"status\":\"OK\",\"data\":", "");
            String withoutEnd = withoutStart.substring(0, withoutStart.length() - 1);
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<JsonWorker>>(){}.getType();
            return gson.fromJson(withoutEnd, collectionType);
        } else {
            return null;
        }
    }
}