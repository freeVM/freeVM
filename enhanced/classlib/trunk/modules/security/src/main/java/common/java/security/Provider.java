/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.harmony.luni.util.TwoKeyHashMap;
import org.apache.harmony.security.Util;
import org.apache.harmony.security.fortress.Services;
import org.apache.harmony.security.internal.nls.Messages;

public abstract class Provider extends Properties {
    private static final long serialVersionUID = -4298000515446427739L;

    private String name;

    private double version;

    // String representation of the provider version number.
    private transient String versionString;

    private String info;

    //The provider preference order number. 
    // Equals -1 for non registered provider.
    private transient int providerNumber = -1;

    // Contains "Service.Algorithm" and Provider.Service classes added using
    // putService()
    private transient TwoKeyHashMap<String, String, Service> serviceTable;

    // Contains "Service.Alias" and Provider.Service classes added using
    // putService()
    private transient TwoKeyHashMap<String, String, Service> aliasTable;

    // Contains "Service.Algorithm" and Provider.Service classes added using
    // put()
    private transient TwoKeyHashMap<String, String, Service> propertyServiceTable;

    // Contains "Service.Alias" and Provider.Service classes added using put()
    private transient TwoKeyHashMap<String, String, Service> propertyAliasTable;

    // The properties changed via put()
    private transient Properties changedProperties;

    // For getService(String type, String algorithm) optimization:
    // previous result
    private transient Provider.Service returnedService;
    // previous parameters
    private transient String lastAlgorithm;
    // last name
    private transient String lastServiceName;

    // For getServices() optimization:
    private transient Set<Service> lastServicesSet;

    // For getService(String type) optimization:
    private transient String lastType;
    // last Service found by type
    private transient Provider.Service lastServicesByType;

    protected Provider(String name, double version, String info) {
        this.name = name;
        this.version = version;
        this.info = info;
        versionString = String.valueOf(version);
        putProviderInfo();
    }

	/**
	 * Returns the name of this provider.
	 * 
	 * 
	 * 
	 * @return String name of the provider
	 */
    public String getName() {
        return name;
    }

	/**
	 * Returns the version number for the services being provided
	 * 
	 * 
	 * 
	 * @return double version number for the services being provided
	 */
    public double getVersion() {
        return version;
    }

	/**
	 * Returns the generic information about the services being provided.
	 * 
	 * 
	 * 
	 * @return String generic description of the services being provided
	 */
    public String getInfo() {
        return info;
    }

	/**
	 * Answers a string containing a concise, human-readable description of the
	 * receiver.
	 * 
	 * 
	 * @return a printable representation for the receiver.
	 */
    public String toString() {
        return name + " version " + version; //$NON-NLS-1$
    }

    public synchronized void clear() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("clearProviderProperties." + name); //$NON-NLS-1$
        }
        super.clear();
        if (serviceTable != null) {
            serviceTable.clear();
        }
        if (propertyServiceTable != null) {
            propertyServiceTable.clear();
        }
        if (aliasTable != null) {
            aliasTable.clear();
        }
        if (propertyAliasTable != null) {
            propertyAliasTable.clear();
        }
        if (changedProperties != null) {
            changedProperties.clear();
        }
        putProviderInfo();
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
        servicesChanged();
    }

    public synchronized void load(InputStream inStream) throws IOException {
        Properties tmp = new Properties();
        tmp.load(inStream);
        myPutAll(tmp);
    }

    public synchronized void putAll(Map<?,?> t) {

        // Implementation note:
        // checkSecurityAccess method call is NOT specified
        // Do it as in put(Object key, Object value).

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("putProviderProperty." + name); //$NON-NLS-1$
        }
        myPutAll(t);
    }

    private void myPutAll(Map<?,?> t) {
        if (changedProperties == null) {
            changedProperties = new Properties();
        }
        Iterator<? extends Map.Entry<?, ?>> it = t.entrySet().iterator();
        Object key;
        Object value;
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = it.next();
            key = entry.getKey();
            if (key instanceof String && ((String) key).startsWith("Provider.")) { //$NON-NLS-1$
                // Provider service type is reserved
                continue;
            }
            value = entry.getValue();
            super.put(key, value);
            if (changedProperties.remove(key) == null) {
                removeFromPropertyServiceTable(key);
            }
            changedProperties.put(key, value);
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
    }

    public synchronized Set<Map.Entry<Object,Object>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    public Set<Object> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    public Collection<Object> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    public synchronized Object put(Object key, Object value) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("putProviderProperty." + name); //$NON-NLS-1$
        }
        if (key instanceof String && ((String) key).startsWith("Provider.")) { //$NON-NLS-1$
            // Provider service type is reserved
            return null;
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
        if (changedProperties != null && changedProperties.remove(key) == null) {
            removeFromPropertyServiceTable(key);
        }
        if (changedProperties == null) {
            changedProperties = new Properties();
        }
        changedProperties.put(key, value);
        return super.put(key, value);
    }

    public synchronized Object remove(Object key) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("removeProviderProperty." + name); //$NON-NLS-1$
        }
        if (key instanceof String && ((String) key).startsWith("Provider.")) { //$NON-NLS-1$
            // Provider service type is reserved
            return null;
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
        if (changedProperties != null && changedProperties.remove(key) == null) {
            removeFromPropertyServiceTable(key);
        }
        return super.remove(key);
    }

    /**
     * Returns true if this provider implements the given algorithm. Caller
     * must specify the cryptographic service and specify constraints via the
     * attribute name and value.
     * 
     * @param serv
     *            Crypto service
     * @param alg
     *            Algorithm or type
     * @param attribute
     *            The attribute name or null
     * @param val
     *            The attribute value
     * @return
     */
    boolean implementsAlg(String serv, String alg, String attribute, String val) {
        String servAlg = serv + "." + alg; //$NON-NLS-1$
        String prop = getPropertyIgnoreCase(servAlg);
        if (prop == null) {
            alg = getPropertyIgnoreCase("Alg.Alias." + servAlg); //$NON-NLS-1$
            if (alg != null) {
                servAlg = serv + "." + alg; //$NON-NLS-1$
                prop = getPropertyIgnoreCase(servAlg); //$NON-NLS-1$
            }
        }
        if (prop != null) {
            if (attribute == null) {
                return true;
            } else {
                return checkAttribute(servAlg, attribute, val); //$NON-NLS-1$
            }
        }
        return false;
    }

    // Returns true if this provider has the same value as is given for the
    // given attribute
    private boolean checkAttribute(String servAlg, String attribute, String val) {
        
        String attributeValue = getPropertyIgnoreCase(servAlg + ' ' + attribute);
        if (attributeValue != null) {
            if (Util.equalsIgnoreCase(attribute,"KeySize")) { //$NON-NLS-1$
                if (Integer.valueOf(attributeValue).compareTo(
                        Integer.valueOf(val)) >= 0) {
                    return true;
                }
            } else { // other attributes
                if (Util.equalsIgnoreCase(attributeValue, val)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 
     * Set the provider preference order number.
     * 
     * @param n
     */
    void setProviderNumber(int n) {
        providerNumber = n;
    }

    /**
     * 
     * Get the provider preference order number.
     * 
     * @return
     */
    int getProviderNumber() {
        return providerNumber;
    }

    /**
     * Get the service of the specified type
     *  
     */
    synchronized Provider.Service getService(String type) {
        updatePropertyServiceTable();
        if (lastServicesByType != null && type.equals(lastType)) {
            return lastServicesByType;
        }
        Provider.Service service;
        for (Iterator<Service> it = getServices().iterator(); it.hasNext();) {
            service = it.next();
            if (type.equals(service.type)) {
                lastType = type;
                lastServicesByType = service;
                return service;
            }
        }
        return null;
    }

    public synchronized Provider.Service getService(String type,
            String algorithm) {
        if (type == null || algorithm == null) {
            throw new NullPointerException();
        }

        if (type.equals(lastServiceName)
                && Util.equalsIgnoreCase(algorithm, lastAlgorithm)) {
            return returnedService;
        }

        String alg = Util.toUpperCase(algorithm);
        Object o = null;
        if (serviceTable != null) {
            o = serviceTable.get(type, alg);
        }
        if (o == null && aliasTable != null) {
            o = aliasTable.get(type, alg);
        }
        if (o == null) {
            updatePropertyServiceTable();
        }
        if (o == null && propertyServiceTable != null) {
            o = propertyServiceTable.get(type, alg);
        }
        if (o == null && propertyAliasTable != null) {
            o = propertyAliasTable.get(type, alg);
        }

        if (o != null) {
            lastServiceName = type;
            lastAlgorithm = algorithm;
            returnedService = (Provider.Service) o;
            return returnedService;
        }
        return null;
    }

    public synchronized Set<Provider.Service> getServices() {
        updatePropertyServiceTable();
        if (lastServicesSet != null) {
            return lastServicesSet;
        }
        if (serviceTable != null) {
            lastServicesSet = new HashSet<Service>(serviceTable.values());
        } else {
            lastServicesSet = new HashSet<Service>();
        }
        if (propertyServiceTable != null) {
            lastServicesSet.addAll(propertyServiceTable.values());
        }
        lastServicesSet = Collections.unmodifiableSet(lastServicesSet);
        return lastServicesSet;
    }

    protected synchronized void putService(Provider.Service s) {
        if (s == null) {
            throw new NullPointerException();
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("putProviderProperty." + name); //$NON-NLS-1$
        }
        if ("Provider".equals(s.getType())) { // Provider service type cannot be //$NON-NLS-1$
                                              // added
            return;
        }
        servicesChanged();
        if (serviceTable == null) {
            serviceTable = new TwoKeyHashMap<String, String, Service>(128);
        }
        serviceTable.put(s.type, Util.toUpperCase(s.algorithm), s);
        if (s.aliases != null) {
            if (aliasTable == null) {
                aliasTable = new TwoKeyHashMap<String, String, Service>(256);
            }
            for (Iterator<String> it = s.getAliases(); it.hasNext();) {
                aliasTable.put(s.type, Util.toUpperCase(it.next()), s);
            }
        }
        serviceInfoToProperties(s);
    }

    protected synchronized void removeService(Provider.Service s) {
        if (s == null) {
            throw new NullPointerException();
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("removeProviderProperty." + name); //$NON-NLS-1$
        }
        servicesChanged();
        if (serviceTable != null) {
            serviceTable.remove(s.type, Util.toUpperCase(s.algorithm));
        }
        if (aliasTable != null && s.aliases != null) {
            for (Iterator<String> it = s.getAliases(); it.hasNext();) {
                aliasTable.remove(s.type, Util.toUpperCase(it.next()));
            }
        }
        serviceInfoFromProperties(s);
    }

    // Add Service information to the provider's properties.
    private void serviceInfoToProperties(Provider.Service s) {
        super.put(s.type + "." + s.algorithm, s.className); //$NON-NLS-1$
        if (s.aliases != null) {
            for (Iterator<String> i = s.aliases.iterator(); i.hasNext();) {
                super.put("Alg.Alias." + s.type + "." + i.next(), s.algorithm); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (s.attributes != null) {
            for (Iterator<Map.Entry<String, String>> i = s.attributes.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, String> entry = i.next();
                super.put(s.type + "." + s.algorithm + " " + entry.getKey(), //$NON-NLS-1$ //$NON-NLS-2$
                        entry.getValue());
            }
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
    }

    // Remove Service information from the provider's properties.
    private void serviceInfoFromProperties(Provider.Service s) {
        super.remove(s.type + "." + s.algorithm); //$NON-NLS-1$
        if (s.aliases != null) {
            for (Iterator<String> i = s.aliases.iterator(); i.hasNext();) {
                super.remove("Alg.Alias." + s.type + "." + i.next()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (s.attributes != null) {
            for (Iterator<Map.Entry<String, String>> i = s.attributes.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, String> entry = i.next();
                super.remove(s.type + "." + s.algorithm + " " + entry.getKey()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
    }

    // Remove property information from provider Services
    private void removeFromPropertyServiceTable(Object key) {
        if (key == null || !(key instanceof String)) {
            return;
        }
        String k = (String) key;
        if (k.startsWith("Provider.")) { // Provider service type is reserved //$NON-NLS-1$
            return;
        }
        Provider.Service s;
        String serviceName;
        String algorithm = null;
        String attribute = null;
        int i;
        if (k.startsWith("Alg.Alias.")) { // Alg.Alias.<crypto_service>.<aliasName>=<standardName> //$NON-NLS-1$
            String aliasName;
            String service_alias = k.substring(10);
            i = service_alias.indexOf('.');
            serviceName = service_alias.substring(0, i);
            aliasName = service_alias.substring(i + 1);
            if (propertyAliasTable != null) {
                propertyAliasTable.remove(serviceName, Util.toUpperCase(aliasName));
            }
            if (propertyServiceTable != null) {
                for (Iterator<Service> it = propertyServiceTable.values().iterator(); it
                        .hasNext();) {
                    s = it.next();
                    if (s.aliases.contains(aliasName)) {
                        s.aliases.remove(aliasName);
                        return;
                    }
                }
            }
            return;
        }
        int j = k.indexOf('.');
        if (j == -1) { // unknown format
            return;
        }

        i = k.indexOf(' ');
        if (i == -1) { // <crypto_service>.<algorithm_or_type>=<className>
            serviceName = k.substring(0, j);
            algorithm = k.substring(j + 1);
            if (propertyServiceTable != null) {
                Provider.Service ser = propertyServiceTable.remove(serviceName, Util.toUpperCase(algorithm));
                if (ser != null && propertyAliasTable != null
                        && ser.aliases != null) {
                    for (Iterator<String> it = ser.aliases.iterator(); it.hasNext();) {
                        propertyAliasTable.remove(serviceName, Util.toUpperCase(it
                                .next()));
                    }
                }
            }
        } else { // <crypto_service>.<algorithm_or_type>
                 // <attribute_name>=<attrValue>
            attribute = k.substring(i + 1);
            serviceName = k.substring(0, j);
            algorithm = k.substring(j + 1, i);
            if (propertyServiceTable != null) {
                Object o = propertyServiceTable.get(serviceName, Util.toUpperCase(algorithm));
                if (o != null) {
                    s = (Provider.Service) o;
                    s.attributes.remove(attribute);
                }
            }
        }
    }

    // Update provider Services if the properties was changed
    private void updatePropertyServiceTable() {
        Object _key;
        Object _value;
        Provider.Service s;
        String serviceName;
        String algorithm;
        if (changedProperties == null || changedProperties.isEmpty()) {
            return;
        }
        for (Iterator<Map.Entry<Object, Object>> it = changedProperties.entrySet().iterator(); it
                .hasNext();) {
            Map.Entry<Object, Object> entry = it.next();
            _key = entry.getKey();
            _value = entry.getValue();
            if (_key == null || _value == null || !(_key instanceof String)
                    || !(_value instanceof String)) {
                continue;
            }
            String key = (String) _key;
            String value = (String) _value;
            if (key.startsWith("Provider")) { // Provider service type is reserved //$NON-NLS-1$
                continue;
            }
            int i;
            if (key.startsWith("Alg.Alias.")) { // Alg.Alias.<crypto_service>.<aliasName>=<standardName> //$NON-NLS-1$
                String aliasName;
                String service_alias = key.substring(10);
                i = service_alias.indexOf('.');
                serviceName = service_alias.substring(0, i);
                aliasName = service_alias.substring(i + 1);
                algorithm = value;
                String algUp = Util.toUpperCase(algorithm);
                Object o = null;
                if (propertyServiceTable == null) {
                    propertyServiceTable = new TwoKeyHashMap<String, String, Service>(128);
                } else {
                    o = propertyServiceTable.get(serviceName, algUp);
                }
                if (o != null) {
                    s = (Provider.Service) o;
                    s.aliases.add(aliasName);
                    if (propertyAliasTable == null) {
                        propertyAliasTable = new TwoKeyHashMap<String, String, Service>(256);
                    }
                    propertyAliasTable.put(serviceName,
                            Util.toUpperCase(aliasName), s);
                } else {
                    String className = (String) changedProperties
                            .get(serviceName + "." + algorithm); //$NON-NLS-1$
                    if (className != null) {
                        List<String> l = new ArrayList<String>();
                        l.add(aliasName);
                        s = new Provider.Service(this, serviceName, algorithm,
                                className, l, new HashMap<String, String>());
                        propertyServiceTable.put(serviceName, algUp, s);
                        if (propertyAliasTable == null) {
                            propertyAliasTable = new TwoKeyHashMap<String, String, Service>(256);
                        }
                        propertyAliasTable.put(serviceName, Util.toUpperCase(aliasName
                                ), s);
                    }
                }
                continue;
            }
            int j = key.indexOf('.');
            if (j == -1) { // unknown format
                continue;
            }
            i = key.indexOf(' ');
            if (i == -1) { // <crypto_service>.<algorithm_or_type>=<className>
                serviceName = key.substring(0, j);
                algorithm = key.substring(j + 1);
                String alg = Util.toUpperCase(algorithm);
                Object o = null;
                if (propertyServiceTable != null) {
                    o = propertyServiceTable.get(serviceName, alg);
                }
                if (o != null) {
                    s = (Provider.Service) o;
                    s.className = value;
                } else {
                    s = new Provider.Service(this, serviceName, algorithm,
                            value, new ArrayList<String>(), new HashMap<String, String>());
                    if (propertyServiceTable == null) {
                        propertyServiceTable = new TwoKeyHashMap<String, String, Service>(128);
                    }
                    propertyServiceTable.put(serviceName, alg, s);

                }
            } else { // <crypto_service>.<algorithm_or_type>
                     // <attribute_name>=<attrValue>
                serviceName = key.substring(0, j);
                algorithm = key.substring(j + 1, i);
                String attribute = key.substring(i + 1);
                String alg = Util.toUpperCase(algorithm);
                Object o = null;
                if (propertyServiceTable != null) {
                    o = propertyServiceTable.get(serviceName, alg);
                }
                if (o != null) {
                    s = (Provider.Service) o;
                    s.attributes.put(attribute, value);
                } else {
                    String className = (String) changedProperties
                            .get(serviceName + "." + algorithm); //$NON-NLS-1$
                    if (className != null) {
                        Map<String, String> m = new HashMap<String, String>();
                        m.put(attribute, value);
                        s = new Provider.Service(this, serviceName, algorithm,
                                className, new ArrayList<String>(), m);
                        if (propertyServiceTable == null) {
                            propertyServiceTable = new TwoKeyHashMap<String, String, Service>(128);
                        }
                        propertyServiceTable.put(serviceName, alg, s);
                    }
                }
            }
        }
        servicesChanged();
        changedProperties.clear();
    }

    private void servicesChanged() {
        lastServicesByType = null;
        lastServiceName = null;
        lastServicesSet = null;
    }

    // These attributes should be placed in each Provider object: 
    // Provider.id name, Provider.id version, Provider.id info, 
    // Provider.id className
    private void putProviderInfo() {
        super.put("Provider.id name", null != name ? name : "null"); //$NON-NLS-1$
		super.put("Provider.id version", versionString); //$NON-NLS-1$
		super.put("Provider.id info", null != info ? info : "null"); //$NON-NLS-1$
        super.put("Provider.id className", this.getClass().getName()); //$NON-NLS-1$
    }

    // Searches for the property with the specified key in the provider
    // properties. Key is not case-sensitive.
    // 
    // @param prop
    // @return the property value with the specified key value.
    private String getPropertyIgnoreCase(String key) {
        String res = getProperty(key);
        if (res != null) {
            return res;
        }
        for (Enumeration<?> e = propertyNames(); e.hasMoreElements();) {
            String pname = (String) e.nextElement();
            if (Util.equalsIgnoreCase(key, pname)) {
                return getProperty(pname);
            }
        }
        return null;
    }

    public static class Service {
        // The provider
        private Provider provider;

        // The type of this service
        private String type;

        // The algorithm name
        private String algorithm;

        // The class implementing this service
        private String className;

        // The aliases
        private List<String> aliases;

        // The attributes
        private Map<String,String> attributes;

        // Service implementation
        private Class<?> implementation;

        // For newInstance() optimization
        private String lastClassName;

        public Service(Provider provider, String type, String algorithm,
                String className, List<String> aliases, Map<String, String> attributes) {
            if (provider == null || type == null || algorithm == null
                    || className == null) {
                throw new NullPointerException();
            }
            this.provider = provider;
            this.type = type;
            this.algorithm = algorithm;
            this.className = className;
            this.aliases = aliases;
            this.attributes = attributes;
        }

        public final String getType() {
            return type;
        }

        public final String getAlgorithm() {
            return algorithm;
        }

        public final Provider getProvider() {
            return provider;
        }

        public final String getClassName() {
            return className;
        }

        public final String getAttribute(String name) {
            if (name == null) {
                throw new NullPointerException();
            }
            if (attributes == null) {
                return null;
            }
            return attributes.get(name);
        }

        Iterator<String> getAliases() {
            if(aliases == null){
                aliases = new ArrayList<String>(0);
            }
            return aliases.iterator();
        }

        public Object newInstance(Object constructorParameter)
                throws NoSuchAlgorithmException {
            if (implementation == null || !className.equals(lastClassName)) {
                NoSuchAlgorithmException result = AccessController
                        .doPrivileged(new PrivilegedAction<NoSuchAlgorithmException>() {
                            public NoSuchAlgorithmException run() {
                                ClassLoader cl = provider.getClass()
                                        .getClassLoader();
                                if (cl == null) {
                                    cl = ClassLoader.getSystemClassLoader();
                                }
                                try {
                                    implementation = Class.forName(className,
                                            true, cl);
                                } catch (Exception e) {
                                    return new NoSuchAlgorithmException(
                                            Messages.getString("security.11",  //$NON-NLS-1$
                                                    new Object[]{type, algorithm, e}));
                                }
                                lastClassName = className;
                                return null;
                            }
                        });
                if (result != null) {
                    throw result;
                }
            }
            if (constructorParameter == null) {
                try {
                    return implementation.newInstance();
                } catch (Exception e) {
                    throw new NoSuchAlgorithmException(Messages.getString("security.199", //$NON-NLS-1$
                            type, algorithm), e);
                }
            } else {
                if (!supportsParameter(constructorParameter)) {
                    throw new InvalidParameterException(
                            Messages.getString("security.12", type)); //$NON-NLS-1$
                }

                Class[] parameterTypes = new Class[1];
                Object[] initargs = { constructorParameter };
                try {
                    if (Util.equalsIgnoreCase(type,"CertStore")) { //$NON-NLS-1$
                        parameterTypes[0] = Class
                                .forName("java.security.cert.CertStoreParameters"); //$NON-NLS-1$
                    } else {
                        parameterTypes[0] = constructorParameter.getClass();
                    }
                    return implementation.getConstructor(parameterTypes)
                            .newInstance(initargs);
                } catch (Exception e) {
                    throw new NoSuchAlgorithmException(Messages.getString("security.199", //$NON-NLS-1$
                            type, algorithm), e);
                }
            }
        }

        public boolean supportsParameter(Object parameter) {
            return true;
        }

	/**
	 * Answers a string containing a concise, human-readable
	 * description of the receiver.
	 * 
	 * 
	 * @return a printable representation for the receiver.
	 */
        public String toString() {
            String result = "Provider " + provider.getName() + " Service " //$NON-NLS-1$ //$NON-NLS-2$
                    + type + "." + algorithm + " " + className; //$NON-NLS-1$ //$NON-NLS-2$
            if (aliases != null) {
                result = result + "\nAliases " + aliases.toString(); //$NON-NLS-1$
            }
            if (attributes != null) {
                result = result + "\nAttributes " + attributes.toString(); //$NON-NLS-1$
            }
            return result;
        }
    }
    
    private void readObject(java.io.ObjectInputStream in) throws NotActiveException, IOException, ClassNotFoundException {
    	in.defaultReadObject();
        versionString = String.valueOf(version);
        providerNumber = -1;
    }
}
