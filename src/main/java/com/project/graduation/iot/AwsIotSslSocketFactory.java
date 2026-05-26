package com.project.graduation.iot;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileReader;
import java.io.Reader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public final class AwsIotSslSocketFactory {

    private AwsIotSslSocketFactory() {
    }

    public static SSLSocketFactory create(String rootCaPath, String certificatePath, String privateKeyPath)
            throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        try (Reader caReader = new FileReader(rootCaPath)) {
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(caReader);
            trustStore.setCertificateEntry("aws-root-ca", caCert);
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        try (Reader certReader = new FileReader(certificatePath)) {
            X509Certificate clientCert = (X509Certificate) certificateFactory.generateCertificate(certReader);
            keyStore.setCertificateEntry("client-cert", clientCert);
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            keyStore.setKeyEntry("client-key", privateKey, "".toCharArray(), new Certificate[]{clientCert});
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }

    private static PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        try (Reader keyReader = new FileReader(privateKeyPath);
             PEMParser pemParser = new PEMParser(keyReader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (object instanceof PEMKeyPair pemKeyPair) {
                return converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            }
            if (object instanceof PrivateKeyInfo privateKeyInfo) {
                return converter.getPrivateKey(privateKeyInfo);
            }
            throw new IllegalArgumentException("지원하지 않는 private key 형식입니다: " + privateKeyPath);
        }
    }
}
