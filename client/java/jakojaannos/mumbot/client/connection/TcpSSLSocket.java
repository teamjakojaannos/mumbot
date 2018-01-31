package jakojaannos.mumbot.client.connection;

import com.sun.deploy.security.X509DeployTrustManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Wrapper for SocketChannel and SSLEngine
 */
public class TcpSSLSocket {
    public SocketChannel createSecureSocket(String hostname, int port) {
        SocketChannel channel;
        try {
            channel = SocketChannel.open(new InetSocketAddress(hostname, port));
            while (!channel.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        SSLEngine engine;
        try {
            // Create TLSv1 SSL Context
            SSLContext context = SSLContext.getInstance("TLSv1");

            // Initialize and set as default
            // FIXME: Initialize with proper values
            context.init(new KeyManager[0], new TrustManager[]{new X509DeployTrustManager()}, new SecureRandom());
            SSLContext.setDefault(context);

            // Create new SSL-engine
            engine = context.createSSLEngine(hostname, port);
            engine.setUseClientMode(true);
        } catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | KeyStoreException | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }

        SSLSession session = engine.getSession();
        ByteBuffer localAppBuffer = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer localPacketBuffer = ByteBuffer.allocate(session.getPacketBufferSize());
        ByteBuffer remoteAppBuffer = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer remotePacketBuffer = ByteBuffer.allocate(session.getPacketBufferSize());

        handshake(channel, engine, localPacketBuffer, remotePacketBuffer);
    }

    private void handshake(
            SocketChannel channel,
            SSLEngine engine,
            ByteBuffer localPacketBuffer,
            ByteBuffer remotePacketBuffer
    ) throws IOException {
        ByteBuffer localAppBuffer = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
        ByteBuffer remoteAppBuffer = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());

        engine.beginHandshake();

        SSLEngineResult.HandshakeStatus status = engine.getHandshakeStatus();
        while (status != SSLEngineResult.HandshakeStatus.FINISHED && status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (status) {
                case NEED_UNWRAP:
                    status = handleHandshakeUnwrap(channel, engine, remotePacketBuffer, remoteAppBuffer);
                    break;
                case NEED_WRAP:
                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        new Thread(task).start();
                    }
                    break;
            }
        }
    }

    private SSLEngineResult.HandshakeStatus handleHandshakeUnwrap(
            SocketChannel channel,
            SSLEngine engine,
            ByteBuffer remotePacketBuffer,
            ByteBuffer remoteAppBuffer
    ) throws IOException {
        if (channel.read(remoteAppBuffer) == -1) {
            // TODO: Handle unexpectedly closed channel
        }

        return doHandshakeUnwrap(channel, engine, remotePacketBuffer, remoteAppBuffer);
    }

    private SSLEngineResult.HandshakeStatus doHandshakeUnwrap(
            SocketChannel channel,
            SSLEngine engine,
            ByteBuffer remotePacketBuffer,
            ByteBuffer remoteAppBuffer
    ) throws IOException {
        remotePacketBuffer.flip();
        SSLEngineResult result = engine.unwrap(remotePacketBuffer, remoteAppBuffer);
        remotePacketBuffer.compact();

        switch (result.getStatus()) {
            case BUFFER_UNDERFLOW:
                if (remotePacketBuffer.capacity() < engine.getSession().getPacketBufferSize()) {
                    throw new BufferUnderflowException();
                } else {
                    remotePacketBuffer.compact();
                }
                handleHandshakeUnwrap(channel, engine, remotePacketBuffer, remoteAppBuffer);
                break;
            case BUFFER_OVERFLOW:
                if (remoteAppBuffer.capacity() < engine.getSession().getApplicationBufferSize()) {
                    throw new BufferOverflowException();
                } else {
                    remoteAppBuffer.compact();
                }
                doHandshakeUnwrap(channel, engine, remotePacketBuffer, remoteAppBuffer);
                break;
            case OK:

                break;
            case CLOSED:

                break;
        }

        return result.getHandshakeStatus();
    }
}
