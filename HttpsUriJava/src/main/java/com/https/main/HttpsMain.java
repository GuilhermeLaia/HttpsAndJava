package com.https.main;

import java.io.File;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

public final class HttpsMain {

	private static final SSLConnectionSocketFactory createSslSocketFactory(String trustedKeystore, String keystore) throws Exception {

		File trustedKeystoreFile = new File(trustedKeystore);
		File keystoreFile = new File(keystore);

		try {

			SSLContext sslcontext = SSLContexts.custom()
					.loadTrustMaterial(trustedKeystoreFile, "<<trusted_keystore_password>>".toCharArray())
					.loadKeyMaterial(keystoreFile, "<<keystore_password>>".toCharArray(),
							"<<original_password_of_PKCS12_file>>".toCharArray())
					.build();

			return new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1.2" }, null,
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());

		} catch (Exception ex) {
			throw ex;
		}

	}

	protected CloseableHttpResponse requestHttps(String urlEndPoint, HttpUriRequest httpUriRequest) throws Exception {

		try {

			CloseableHttpClient httpclient = HttpClients.custom()
					.setSSLSocketFactory(createSslSocketFactory("trusted.jks", "key.jks")).build();
			CloseableHttpResponse response = httpclient.execute(httpUriRequest);
			HttpEntity entity = response.getEntity();
			System.out.println(response.getStatusLine());
			EntityUtils.consume(entity);

			return response;

		} catch (Exception e) {
			throw e;
		}

	}


}
