package org.objectrepository.pid;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Logger;
import org.objectrepository.util.Normalizers;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * PidHttpClient
 * <p/>
 * Simple and direct soap post, rather than using the wsdl compiled material.
 * We only need one webservice method.
 */
public class PidHttpClient {

    String or_endpoint;
    String or_key;
    Transformer transformer;
    HttpClient httpClient;

    public PidHttpClient() throws TransformerConfigurationException, IOException {
        URL resource = getClass().getResource("/pid.xsl");
        StreamSource source = new StreamSource(resource.openStream());
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer(source);
        httpClient = new HttpClient();

    }

    /**
     * Retrieves a PID from the web service.
     * This method can only be used in an integration\production environment.
     *
     * @param na
     * @param localIdentifier
     * @param resolveUrl
     * @return
     */
    public String getPid(String cp_endpoint, String cp_key, String na, String localIdentifier, String resolveUrl) {

        String key = (Normalizers.isEmpty(cp_key)) ? or_key : cp_key;
        String endpoint = (Normalizers.isEmpty(cp_endpoint)) ? or_endpoint : cp_endpoint;
        final RequestEntity entity = new ByteArrayRequestEntity(soapenv(na, localIdentifier, resolveUrl), "text/xml; charset=utf-8");
        final PostMethod method = new PostMethod(endpoint);
        method.setRequestHeader("Authorization", "oauth " + key);
        method.setRequestEntity(entity);
        return send(method);
    }

    private String send(PostMethod method) {

        int statusCode = 0;
        byte[] body = null;
        try {
            statusCode = httpClient.executeMethod(method);
            body = method.getResponseBody();
        } catch (IOException e) {
            log.fatal(e);
            System.exit(-1);
        } finally {
            method.releaseConnection();
        }

        // In case of multiple HttpClient instance:
        // CLOSE_WAIT is set, so close the socket
        //HttpConnectionManager mgr = httpClient.getHttpConnectionManager();
        //((SimpleHttpConnectionManager) mgr).shutdown();

        assert (statusCode == HttpStatus.SC_OK);
        assert (body != null);

        String pid = null;
        try {
            pid = getPid(body);
        } catch (Exception e) {
            log.fatal(e);
            System.exit(-1);
        }
        return pid;
    }

    private byte[] soapenv(String na, String localIdentifier, String resolveUrl) {

        return String.format("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pid=\"http://pid.socialhistoryservices.org/\">" +
                "   <soapenv:Body>" +
                "      <pid:GetQuickPidRequest>" +
                "         <pid:na>%s</pid:na>" +
                "         <pid:localIdentifier>%s</pid:localIdentifier>" +
                "         <!--<pid:resolveUrl>%s</pid:resolveUrl>-->" +
                "      </pid:GetQuickPidRequest>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>", na, localIdentifier, resolveUrl).getBytes();
    }

    private String getPid(byte[] body) throws TransformerException {

        final StreamSource source = new StreamSource(new ByteArrayInputStream(body));
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(source, new StreamResult(baos));
        return new String(baos.toByteArray());
    }

    public void setEndpoint(String endpoint) {
        this.or_endpoint = endpoint;
    }

    public void setKey(String key) {
        this.or_key = key;
    }

    private static Logger log = Logger.getLogger(PidHttpClient.class);
}
