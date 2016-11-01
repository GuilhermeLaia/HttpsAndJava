# HttpsAndJava
Together with my teammate we tried to implemented the HTTPS client in Java. Combining our knowledge about the TLS/SSL handshake and the experience from the manual test with curl we assumed that only three files were required to implement the client side: a client's certificate, a client's private key and a trusted certificate to verify the server's certificate.


# Java: The Problem, the Solution and Why Is It So Hard
Because it is quite unusual to use mutual authentication every day, we asked the best source in the world for a small assistance. A first look at the results served by uncle Google didn't revealed the complexity behind the implementation, but each click on the results led us to more and more confusing solutions (some of them where from 90's). To make matters worse we had to use Apache HttpComponents to implement our connection, but most of the proposed solutions were based on the pure Java libraries.

# The knowledge from the internet allows us to establish that:

Java cannot use directly any certificates or private keys (like e.g. curl)
Java requires separate files (Java Keystores) which can contain original certificates and keys.
We needed a trusted keystore with the certificate required for the server's certificate verification for each HTTPS connection.
We needed a keys keystore with the client's certificate and the client's private key for mutual authentication.
First, we had to create the trusted keystore. We created the keystore with the certificate using the keytool command:

# $ keytool -import -alias trusted_certificate -keystore trusted.jks -file trusted.crt

We stored in the keystore file trusted.jks the certificate trusted.crt under the alias trusted_certificate. During the execution of this command, we were asked to input a password for this keystore. We used this password later to get access to the keystore file.

To create a keystore, a few additional steps were required. In most cases, you will probably receive two files from the company which issues the client's certificate. The first file will be the client's certificate in the pem format. This certificate will be sent to the server. The second file is the client's private key (also in the pem format) which is used during the handshake to confirm that you are the owner of the client's certificate.

Unfortunately, Java only supports the PKCS12 format. So we had to translate our certificate and private key to PKCS12 format. We can do that using OpenSSL.

# $ openssl pkcs12 -export \ -in client.crt \ -inkey client.key \ -out key.p12 \ -name client
    
We generated the file key.p12 from the files client.crt and client.key. Once again a password input was required. This password is used to protect the private key.

From the file in the PKCS12 format we can generate another keystore by importing our PKCS12 into the new keystore:

$ keytool -importkeystore \
    -destkeystore key.jks \
    -deststorepass <<keystore_password>> \
    -destkeypass <<key_password_in_keystore>> \
    -alias client \
    -srckeystore key.p12 \
    -srcstoretype PKCS12 \
    -srcstorepass <<original_password_of_PKCS12_file>>
    
This command looks a little bit more complex, but it is fairly easy to decrypt. At the beginning of the command we declare the parameters of the new keystore named key.jks. We define the password for the keystore and the password for the private key which will be used by this keystore. We also assign the private key to some alias in the keystore (in this case it is client). Next, we specify the source file (key.p12), the format of this file and the original password.

With trusted.jks and key.jks we were ready to code. In the first step we had to describe how we wanted to use our keystores.
