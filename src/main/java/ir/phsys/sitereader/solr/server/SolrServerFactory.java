package ir.phsys.sitereader.solr.server;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author : Pooya Hosseini
 *         Email : info@pooya-hfp.ir,pooya.husseini@gmail.com
 *         Date: 8/23/15
 *         Time: 11:34 AM
 */


public class SolrServerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrServerFactory.class);

    private static final ConnectionProperties PROPERTIES;

    static {
        try {
            PROPERTIES = retrieveConnectionProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpSolrClient create(String core) throws IOException {
        LOGGER.debug("Going to initialize the core " + core);
        PoolingHttpClientConnectionManager cxMgr = new PoolingHttpClientConnectionManager();
        cxMgr.setMaxTotal(100);
        cxMgr.setDefaultMaxPerRoute(20);

        HttpClientBuilder httpclient = HttpClientBuilder.create();
        httpclient.setConnectionManager(cxMgr);
        httpclient.addInterceptorFirst(new PreemptiveAuthInterceptor());
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(PROPERTIES.getUsername(), PROPERTIES.getPassword()));

        httpclient.setDefaultCredentialsProvider(provider);
        String url = (PROPERTIES.getBaseUrl().endsWith("/") ? PROPERTIES.getBaseUrl() : PROPERTIES.getBaseUrl() + "/") + core;
        HttpSolrClient server = new HttpSolrClient(url, httpclient.build());
        server.setRequestWriter(new BinaryRequestWriter());
        return server;
    }

    public static ConnectionProperties retrieveConnectionProperties() throws IOException {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
        encryptor.setPassword("Without music, life would be a mistake");
        encryptor.setPoolSize(4);

        Properties properties = new Properties();
        InputStream stream = SolrServerFactory.class.getResourceAsStream("/application.properties");

        properties.load(stream);

        for (Object o : properties.keySet()) {
            String property = o.toString();
            String value = properties.getProperty(property);
            if (value.toLowerCase().startsWith("enc(")) {
                String trimmerValue = value.substring(4);
                trimmerValue = trimmerValue.substring(0, trimmerValue.length() - 1);
                String decrypt = encryptor.decrypt(trimmerValue);
                properties.setProperty(property, decrypt);
            }
        }

        return new ConnectionProperties(
                properties.getProperty("solr.url"),
                properties.getProperty("solr.username"),
                properties.getProperty("solr.password")
        );
    }

    public static class ConnectionProperties {
        private String baseUrl;
        private String username;
        private String password;

        public ConnectionProperties(String baseUrl, String username, String password) {
            this.baseUrl = baseUrl;
            this.username = username;
            this.password = password;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}