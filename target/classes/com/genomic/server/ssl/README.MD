# SSL Keystore
Generar antes de ejecutar el servidor:

keytool -genkeypair -alias serverkey -keyalg RSA -keysize 2048 -validity 365 -keystore src/main/java/com/genomic/server/ssl/serverkeystore.jks -storepass password