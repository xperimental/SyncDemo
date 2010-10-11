package net.sourcewalker.syncdemo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class NumbersClient {

    private static final int BUFSIZE = 8 * 1024;

    private final URI serverUri;
    private final HttpClient client;

    public NumbersClient(String host, int port, String database, String user)
            throws ServerException {
        try {
            this.serverUri = new URI("http", null, host, port, "/" + database
                    + "/" + user, null, null);
        } catch (URISyntaxException e) {
            throw new ServerException("Invalid URL: " + e.getMessage(), e);
        }
        this.client = new DefaultHttpClient();
    }

    public NumbersClient(String user) throws ServerException {
        this("sourcewalker.net", 5984, "numbers", user);
    }

    public boolean exists() throws ServerException {
        HttpGet get = new HttpGet(serverUri);
        try {
            HttpResponse response = client.execute(get);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            throw new ServerException("IO error: " + e.getMessage(), e);
        }
    }

    public NumbersData getNumbers() throws ServerException {
        HttpGet get = new HttpGet(serverUri);
        try {
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = readEntity(response.getEntity());
                JSONObject jsonContent = new JSONObject(content);
                return new NumbersData(jsonContent);
            } else {
                throw new ServerException("User not found!");
            }
        } catch (IOException e) {
            throw new ServerException("IO error: " + e.getMessage(), e);
        } catch (JSONException e) {
            throw new ServerException("Error parsing JSON: " + e.getMessage(),
                    e);
        }
    }

    private String readEntity(HttpEntity entity) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity
                .getContent()), BUFSIZE);
        StringBuilder sb = new StringBuilder();
        String line;
        do {
            line = reader.readLine();
            if (line != null) {
                sb.append(line);
            }
        } while (line != null);
        reader.close();
        entity.consumeContent();
        return sb.toString();
    }

    public void saveNumbers(NumbersData data) throws ServerException {
        HttpPut put = new HttpPut(serverUri);
        put.addHeader("Content-type", "application/json");
        try {
            put.setEntity(new StringEntity(data.toJSON().toString()));
            HttpResponse response = client.execute(put);
            if (response.getStatusLine().getStatusCode() != 201) {
                String responseContent = readEntity(response.getEntity());
                throw new ServerException("Error from server while saving: "
                        + responseContent);
            }
        } catch (UnsupportedEncodingException e) {
            throw new ServerException("Error creating JSON data: "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new ServerException("IO error: " + e.getMessage(), e);
        }
    }

}
