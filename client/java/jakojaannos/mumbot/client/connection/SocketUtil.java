package jakojaannos.mumbot.client.connection;

import com.sun.deploy.security.X509DeployTrustManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Wrapper for SocketChannel and SSLEngine
 */
class SocketUtil {
    static Socket openSSLSocket(String hostname, int port) {
        SSLEngine engine;
        try {
            // Create TLSv1 SSL Context
            SSLContext context = SSLContext.getInstance("TLSv1");

            // Initialize and set as default

            // FIXME: Dummy trust manager implementation accepts all certificates. Implement certificate storage etc.
            context.init(new KeyManager[0], new TrustManager[]{ new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new SecureRandom());
            SSLContext.setDefault(context);

            // Create new SSL-engine
            engine = context.createSSLEngine(hostname, port);
            engine.setUseClientMode(true);

            return context.getSocketFactory().createSocket(hostname, port);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
