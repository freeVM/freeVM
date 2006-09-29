/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tools.jarsigner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * The class to parse the program arguments. 
 */
public class ArgParser {
    // TODO
    // options names to compare to //
    final static String sVerify = "-verify";

    final static String sKeyStore = "-keystore";
    
    final static String sStoreType = "-storetype";
    
    final static String sStorePass = "-storepass";
    
    final static String sKeyPass = "-keypass";
    
    final static String sSigFile = "-sigfile";
    
    final static String sSignedJAR = "-signedjar";
    
    final static String sCerts = "-certs";
    
    final static String sVerbose = "-verbose";
    
    final static String sInternalSF = "-internalsf";
    
    final static String sSectionsOnly = "-sectionsonly";
    
    final static String sProvider = "-provider";
    
    final static String sProviderName = "-providername";

    final static String sCertProvider = "-certprovider";
    
    final static String sCertProviderName = "-certprovidername";

    final static String sSigProvider = "-sigprovider";
    
    final static String sSigProviderName = "-sigprovidername";

    final static String sKSProvider = "-ksprovider";
    
    final static String sKSProviderName = "-ksprovidername";
    
    final static String sTSA = "-tsa";
    
    final static String sTSAcert = "-tsacert";
    
    final static String sAltSigner = "-altsigner";
    
    final static String sAltSignerPath = "-altsignerpath";
    
    /**
     * @param args
     * @param param
     * @return new instance of JSParameters if param is null or updated param
     *         object if it is non-null. Returns null if args is null or
     *         zero-sized, an unknown option is found or an expected option
     *         value is not given or not of an expected type. If null is
     *         returned, the param object contents is not defined.
     * @throws JarSignerException  
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     * @throws UnrecoverableKeyException 
     * @throws KeyStoreException 
     * @throws NoSuchProviderException 
     * @throws CertificateException 
     */
    static JSParameters parseArgs(String[] args, JSParameters param)
            throws JarSignerException, KeyStoreException,
            UnrecoverableKeyException, NoSuchAlgorithmException, IOException,
            CertificateException, NoSuchProviderException {
        if (args == null){
            return null;
        }
        if (args.length == 0){
            return null;
        }
        if (param == null){
            param = new JSParameters();
        } else {
            // clean param
            param.setDefault();
        }
        
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase(sVerify)) {
                    param.setVerify(true);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sKeyStore)) {
                    param.setStoreURI(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sStoreType)) {
                    param.setStoreType(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sStorePass)) {
                    param.setStorePass(args[++i].toCharArray());
                    continue;
                }
                if (args[i].equalsIgnoreCase(sKeyPass)) {
                    param.setKeyPass(args[++i].toCharArray());
                    continue;
                }
                if (args[i].equalsIgnoreCase(sSigFile)) {
                    param.setSigFileName(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sSignedJAR)) {
                    param.setSignedJARName(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sCerts)) {
                    param.setCerts(true);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sVerbose)) {
                    param.setVerbose(true);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sInternalSF)) {
                    param.setInternalSF(true);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sSectionsOnly)) {
                    param.setSectionsOnly(true);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sProvider)) {
                    param.setProvider(args[++i]);
                    addProvider(args[i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sProviderName)) {
                    param.setProviderName(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sCertProvider)) {
                    param.setCertProvider(args[++i]);
                    addProvider(args[i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sCertProviderName)) {
                    param.setCertProviderName(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sSigProvider)) {
                    param.setSigProvider(args[++i]);
                    addProvider(args[i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sSigProviderName)) {
                    param.setSigProviderName(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sKSProvider)) {
                    param.setKsProvider(args[++i]);
                    addProvider(args[i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sKSProviderName)) {
                    param.setKsProviderName(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sTSA)) {
                    try {
                        // TODO: URI scheme
                        param.setTsaURI(new URI(args[++i]));
                    } catch (URISyntaxException e) {
                        throw new JarSignerException("Argument " + args[i]
                                + " is not a path or URL");
                    }
                    continue;
                }
                if (args[i].equalsIgnoreCase(sTSAcert)) {
                    param.setTsaCertAlias(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sAltSigner)) {
                    param.setAltSigner(args[++i]);
                    continue;
                }
                if (args[i].equalsIgnoreCase(sAltSignerPath)) {
                    param.setAltSignerPath(args[++i]);
                    continue;
                }
                
                if ((param.isVerify() && i == args.length - 1)
                        || (!param.isVerify() && i == args.length - 2)) {
                    try {
                        // TODO: URI scheme
                        param.setJarURI(new URI(args[i]));
                    } catch (URISyntaxException e) {
                        throw new JarSignerException("Argument " + args[i]
                                + " is not a path or URL");
                    }
                    continue;
                }
                if (!param.isVerify() && i == args.length - 1){
                    param.setAlias(args[i]);
                    continue;
                }
                
                System.out.println("Illegal option: " + args[i]);
                return null;
            }
        } catch(ArrayIndexOutOfBoundsException e){
            // ignore the last option if its value is not provided
        }
        
        // set specific provider names the same as the main provider name
        String providerName = param.getProviderName();
        if (providerName != null){
            if (param.getCertProviderName() == null){
                param.setCertProviderName(providerName);
            }
            if (param.getSigProviderName() == null){
                param.setSigProviderName(providerName);
            }
            if (param.getKsProviderName() == null){
                param.setKsProviderName(providerName);
            }
        }
        
        // if the store password is not given, prompt for it
        if (param.getStorePass() == null) {
            param.setStorePass(UserInteractor
                    .getDataFromUser("Enter keystore password:  "));
        }
        
        if (param.getAlias() == null){
            // TODO
        }
        // if key password is not given, try to inplace it with store password
        if (param.getKeyPass() == null){
            param.setKeyPass(tryStorePassAsKeyPass(param.getKeyStore(), param
                    .getAlias(), param.getStorePass()));
        }
        
        return param;
    }
    
    
    // Method tries to get the key, associated with alias, using the storePass.
    // If it can be recovered using the password, storePass is returned,
    // otherwise - the password is prompted for. Another attempt to recover the
    // key with entered password. If it is ok, it is returned, otherwise
    // UnrecoverableKeyException is thrown.
    private static char[] tryStorePassAsKeyPass(KeyStore keyStore,
            String alias, char[] storePass) throws KeyStoreException,
            IOException, UnrecoverableKeyException, NoSuchAlgorithmException {
        try {
            // try to get a key with keystore password
            // if succeed set key password same as that for keystore
            keyStore.getKey(alias, storePass);

            // will not come here if exception is thrown
            return storePass;
        } catch (UnrecoverableKeyException e) {
            // if key password is not equal to store password, ask for it.
            char[] keyPass = UserInteractor
                    .getDataFromUser("Enter key password for <" + alias + ">: ");
            // if the new password is incorrect an exception will be thrown
            try {
                keyStore.getKey(alias, keyPass);
            } catch (NoSuchAlgorithmException nsae) {
                throw new NoSuchAlgorithmException(
                        "Cannot find the algorithm to recover the key. ", e);
            }
            return keyPass;
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(
                    "Cannot find the algorithm to recover the key. ", e);
        }
    }
    
    // method for adding providers to java.security.Security
    static int addProvider(String provider) throws JarSignerException {
        try {
            return Security.addProvider(Class.forName(provider).asSubclass(
                    Provider.class).newInstance());
        } catch (Exception e) {
            throw new JarSignerException("Failed to load the provider "
                    + provider, e);
        }
    }

}

