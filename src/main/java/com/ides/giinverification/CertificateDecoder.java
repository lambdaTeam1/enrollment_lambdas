package com.ides.giinverification;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateDecoder {

    public static void main(String[] args) {
        String base64EncodedCertificate;
        
        if (args.length > 0) {
            try {
                // Read Base64 encoded certificate from a file if a file path is provided
                base64EncodedCertificate = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.out.println("Failed to read certificate from file. Check file path and permissions.");
                return;
            }
        } else {
    
            base64EncodedCertificate = "MIIGvTCCBaWgAwIBAgIQYFXJlP6frXr8nk7hkwHzGDANBgkqhkiG9w0BAQsFADCB\r\n"
            		+ "ujELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUVudHJ1c3QsIEluYy4xKDAmBgNVBAsT\r\n"
            		+ "H1NlZSB3d3cuZW50cnVzdC5uZXQvbGVnYWwtdGVybXMxOTA3BgNVBAsTMChjKSAy\r\n"
            		+ "MDEyIEVudHJ1c3QsIEluYy4gLSBmb3IgYXV0aG9yaXplZCB1c2Ugb25seTEuMCwG\r\n"
            		+ "A1UEAxMlRW50cnVzdCBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eSAtIEwxSzAeFw0y\r\n"
            		+ "MzEwMDIxNzAzNTVaFw0yNDExMDIxNzAzNTRaMIGOMQswCQYDVQQGEwJVUzEWMBQG\r\n"
            		+ "A1UECBMNV2VzdCBWaXJnaW5pYTEWMBQGA1UEBxMNS2Vhcm5leXN2aWxsZTEhMB8G\r\n"
            		+ "A1UEChMYSW50ZXJuYWwgUmV2ZW51ZSBTZXJ2aWNlMSwwKgYDVQQDEyNlbmNyeXB0\r\n"
            		+ "aW9uLXNlcnZpY2Uuc2VydmljZXMuaXJzLmdvdjCCASIwDQYJKoZIhvcNAQEBBQAD\r\n"
            		+ "ggEPADCCAQoCggEBANAOYvZC/waIsfTysfcMxSKKnK7T9qBLabojW3pykhPAp/iV\r\n"
            		+ "nN77ljb66zf3RaB9kZUG9s/3juiLkuvXVYWD/vKlAWdogXGoaH438/ioBBf62FQP\r\n"
            		+ "wNVxCb9mcU3nfRgLgcIIIiIWWxnp+Zdwafm5xV4ttJiSqZOp+g6+PWhZHBOTrT50\r\n"
            		+ "22Irv1ZQ6QBB9VVY9ChqB/MZf5JFrCCD+oxQ8lN0zJdKlKW1f+IDcCouhwgdul16\r\n"
            		+ "qrVEFXXaaO6CxdmQyoPV53bqA8cYOzerrCvTXHkfOPI3PsUJ+KaAOce8Rfozwo0f\r\n"
            		+ "gpfteRFhjwF0BaCesFgCnJUNG5Kfq+5Go2J9ZykCAwEAAaOCAucwggLjMAwGA1Ud\r\n"
            		+ "EwEB/wQCMAAwHQYDVR0OBBYEFFDeNBDqxIv7dMFcmZ5PyPCU6S+fMB8GA1UdIwQY\r\n"
            		+ "MBaAFIKicHTdvFM/z3vU981/p2DGCky/MGgGCCsGAQUFBwEBBFwwWjAjBggrBgEF\r\n"
            		+ "BQcwAYYXaHR0cDovL29jc3AuZW50cnVzdC5uZXQwMwYIKwYBBQUHMAKGJ2h0dHA6\r\n"
            		+ "Ly9haWEuZW50cnVzdC5uZXQvbDFrLWNoYWluMjU2LmNlcjAzBgNVHR8ELDAqMCig\r\n"
            		+ "JqAkhiJodHRwOi8vY3JsLmVudHJ1c3QubmV0L2xldmVsMWsuY3JsMC4GA1UdEQQn\r\n"
            		+ "MCWCI2VuY3J5cHRpb24tc2VydmljZS5zZXJ2aWNlcy5pcnMuZ292MA4GA1UdDwEB\r\n"
            		+ "/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwEwYDVR0gBAww\r\n"
            		+ "CjAIBgZngQwBAgIwggF+BgorBgEEAdZ5AgQCBIIBbgSCAWoBaAB2AO7N0GTV2xrO\r\n"
            		+ "xVy3nbTNE6Iyh0Z8vOzew1FIWUZxH7WbAAABivFYr1MAAAQDAEcwRQIhAMvqlygl\r\n"
            		+ "A1c7YRT2DdirdX0yjbA2Xox2PjwYklC3nDW1AiBILw/f9sDhO115pOs1/M6u2xrm\r\n"
            		+ "dDLobACB9DJPvRZoDwB2ANq2v2s/tbYin5vCu1xr6HCRcWy7UYSFNL2kPTBI1/ur\r\n"
            		+ "AAABivFYr4IAAAQDAEcwRQIhAMW/BouGZ9L3RsZG7jvYAYuC/oP4H9VC4VEZNMnv\r\n"
            		+ "HSsSAiBa4Ju2N8jAmxixGkLrai9VshzT2XCu2GhcNvIVBEVqrAB2AD8XS0/XIkdY\r\n"
            		+ "lB1lHIS+DRLtkDd/H4Vq68G/KIXs+GRuAAABivFYr7AAAAQDAEcwRQIgbA9IOTsn\r\n"
            		+ "adBlBCyLyFZgcfB39TvxscVw98RhzhW72QUCIQD3M71+7BG7fH5CbDGzS3gUFtkC\r\n"
            		+ "H4cHtX3r6jf4143IHjANBgkqhkiG9w0BAQsFAAOCAQEAkAL8+9CS6h/iuM5W5IRR\r\n"
            		+ "EmdMEUdvV8eyyfFr6F0etWktRZBw72YXWS6vdbFWI2aL7PJgBB3+CbxUECUQE88C\r\n"
            		+ "qM/icwUecm2wroE9w3hbaIiMwxuunDzK+hGz4PKnwZBRa6TCiSmvXJlE8Xpu8MeZ\r\n"
            		+ "I2I0dXvp1RJtr4vJ99RFNkt2VWUv3zzWgXK0bnb4cdfUcPDhyGibtw+Bww+Jxq7K\r\n"
            		+ "dcK0QKmR1jZoFq0U4CWjQ2kFVt4OmUIuy6X+O7l4firKDRe7rH40tSwaBVK4Dqji\r\n"
            		+ "eA+U5amGU7vflAIkZxOoMkeC2123owVurYduieyT1dMtlSw/KLh+5jVHMxhLaKFg\r\n"
            		+ "bg==";
        }

        // Remove all whitespace including newlines and spaces
        String base64EncodedCertificateWithoutNewlines = base64EncodedCertificate.replaceAll("\\s", "");
        
        try {
            // Decode the Base64 string to a byte array
            byte[] certificateBytes = Base64.getDecoder().decode(base64EncodedCertificateWithoutNewlines);
            
            // Create an InputStream from the byte array
            InputStream inputStream = new ByteArrayInputStream(certificateBytes);
            
            // Create a CertificateFactory instance for X.509 certificates
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            
            // Generate an X509Certificate from the InputStream
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
            
            // Output various details about the certificate
            System.out.println("Certificate Subject: " + certificate.getSubjectX500Principal());
            System.out.println("Certificate Issuer: " + certificate.getIssuerX500Principal());
            System.out.println("Certificate Version: " + certificate.getVersion());
            System.out.println("Certificate Serial Number: " + certificate.getSerialNumber());
            System.out.println("Certificate Valid From: " + certificate.getNotBefore());
            System.out.println("Certificate Valid Until: " + certificate.getNotAfter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

