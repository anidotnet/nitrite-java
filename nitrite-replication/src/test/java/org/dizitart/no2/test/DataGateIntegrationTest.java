package org.dizitart.no2.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.Replica;

import javax.net.ssl.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;

import static org.dizitart.no2.collection.Document.createDocument;

/**
 * @author Anindya Chatterjee
 */
public class DataGateIntegrationTest {
    public static void main(String[] args) {
        Path dbPath = null;
        try {
            createUser();
            dbPath = Files.createTempFile("no2-datagate-it", "db");

            Nitrite db = NitriteBuilder.get()
                .filePath(dbPath.toFile())
                .openOrCreate();

            NitriteCollection collection = db.getCollection("datagateIntegration");
            Document document = createDocument().put("firstName", "Anindya")
                .put("lastName", "Chatterjee")
                .put("address", createDocument("street", "1234 Abcd Street")
                    .put("pin", 123456));
            collection.insert(document);

            String jwt = getToken();

            System.out.println("Token - " + jwt);
            Replica replica = Replica.builder()
                .of(collection)
                .remote("wss://127.0.0.1:3030/ws/datagate/anidotnet@gmail.com/datagateIntegration")
                .jwtAuth("anidotnet@gmail.com", jwt)
                .acceptAllCertificates(true)
                .create();
            replica.connect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dbPath != null) {
                    Files.delete(dbPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createUser() throws Exception {
        OkHttpClient client = getUnsafeOkHttpClient();
        Request request = new Request.Builder()
            .url("https://127.0.0.1:3030/exists?email=anidotnet@gmail.com")
            .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        ObjectMapper mapper = new ObjectMapper();
        assert response.body() != null;
        JsonNode jsonNode = mapper.readValue(response.body().string(), JsonNode.class);
        if (jsonNode.has("exists")) {
            if (jsonNode.get("exists").asBoolean()) {
                return;
            }
        }

        String json = "{" +
            "\"email\":\"anidotnet@gmail.com\"," +
            "\"password\":\"chang3me\"," +
            "\"firstName\":\"Anindya\"," +
            "\"lastName\":\"Chatterjee\"," +
            "\"roles\": [\"admin\"]}";
        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), json);

        request = new Request.Builder()
            .url("https://127.0.0.1:3030/register")
            .post(body)
            .build();

        call = client.newCall(request);
        response = call.execute();

        if (response.code() != 201) {
            throw new Exception("user creation failed");
        }
    }

    private static String getToken() throws Exception {
        OkHttpClient client = getUnsafeOkHttpClient();
        String json = "{" +
            "\"email\":\"anidotnet@gmail.com\"," +
            "\"password\":\"chang3me\"}";
        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
            .url("https://127.0.0.1:3030/login")
            .post(body)
            .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            assert response.body() != null;
            json = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(json, JsonNode.class);

            if (jsonNode.has("token")) {
                return jsonNode.get("token").asText();
            }
        }

        throw new Exception("failed to login");
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws
                        CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws
                        CertificateException {
                    }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
