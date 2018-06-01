package jakojaannos.mumbot.client.connection;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

class SocketFactory {
    /**
     * Context may be null if initialization fails
     */
    @Nullable
    private final SSLContext context;

    SocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        // Init key manager TODO
        KeyManager[] keyManagers = new KeyManager[0];

        // Init trust manager TODO
        TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
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
        }};

        // Init context
        context = SSLContext.getInstance("TLSv1");
        context.init(keyManagers, trustManagers, new SecureRandom());
    }

    @Nullable
    Socket createSSLSocket(String hostname, int port) throws IOException {
        if (context == null) {
            throw new IllegalStateException("Cannot create socket with invalid SocketFactory!");
        }
        Socket socket = context.getSocketFactory().createSocket(hostname, port);
        socket.setKeepAlive(true);

        return socket;
    }
}
