/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.net;

import java.io.IOException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/*
 * Default implementation for ProxySelector
 */
class ProxySelectorImpl extends ProxySelector {

	private static final int HTTP_PROXY_PORT = 80;

	private static final int HTTPS_PROXY_PORT = 443;

	private static final int FTP_PROXY_PORT = 80;

	private static final int SOCKS_PROXY_PORT = 1080;

	/*
	 * Constructor method.
	 */
	public ProxySelectorImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.ProxySelector#connectFailed(java.net.URI,
	 *      java.net.SocketAddress, java.io.IOException)
	 */
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		if (null == uri || null == sa || null == ioe) {
            // "KA001=Argument must not be null"
			throw new IllegalArgumentException(Msg.getString("KA001")); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.ProxySelector#select(java.net.URI)
	 */
	public List<Proxy> select(URI uri) {
		// argument check
		if (null == uri) {
            // "KA001=Argument must not be null"
            throw new IllegalArgumentException(Msg.getString("KA001")); //$NON-NLS-1$
		}
		// check scheme
		String scheme = uri.getScheme();
		if (null == scheme) {
			throw new IllegalArgumentException();
		}
        
		String host = uri.getHost();
		Proxy proxy = Proxy.NO_PROXY;

		if ("http".equals(scheme)) { //$NON-NLS-1$
			proxy = selectHttpProxy(host);
		} else if ("https".equals(scheme)) { //$NON-NLS-1$
			proxy = selectHttpsProxy();
		} else if ("ftp".equals(scheme)) { //$NON-NLS-1$
			proxy = selectFtpProxy(host);
		} else if ("socket".equals(scheme)) { //$NON-NLS-1$
			proxy = selectSocksProxy();
		}
		List<Proxy> proxyList = new ArrayList<Proxy>(1);
		proxyList.add(proxy);
		return proxyList;
	}

	/*
	 * Gets proxy for http request. 1. gets from "http.proxyHost", then gets
	 * port from "http.proxyPort", or from "proxyPort" if "http.proxyPort" is
	 * unavailable. 2. gets from "proxyHost" if 1 is unavailable,then get port
	 * from "proxyPort", or from "http.proxyPort" if "proxyPort" is unavailable.
	 * 3. gets from "socksProxyHost" if 2 is unavailable.
	 */

	private Proxy selectHttpProxy(String uriHost) {
		String host; 
		String port = null;
		Proxy.Type type = Proxy.Type.DIRECT;

		String nonProxyHosts = getSystemProperty("http.nonProxyHosts"); //$NON-NLS-1$
		// if host is in non proxy host list, returns Proxy.NO_PROXY
		if (isNonProxyHost(uriHost, nonProxyHosts)) {
			return Proxy.NO_PROXY;
		}
		
		host = getSystemProperty("http.proxyHost"); //$NON-NLS-1$
		if (null != host) {
			// case 1: http.proxyHost is set, use exact http proxy
			type = Proxy.Type.HTTP;
			port = getSystemPropertyOrAlternative("http.proxyPort", //$NON-NLS-1$
					"proxyPort", String.valueOf(HTTP_PROXY_PORT)); //$NON-NLS-1$
		} else if((host = getSystemProperty("proxyHost", null)) !=  null) {
			// case 2: proxyHost is set, use exact http proxy
			type = Proxy.Type.HTTP;
			port = getSystemPropertyOrAlternative("proxyPort", //$NON-NLS-1$
					"http.proxyPort", String.valueOf(HTTP_PROXY_PORT)); //$NON-NLS-1$
			
		}else if((host = getSystemProperty("socksProxyHost"))!=null){
			// case 3: use socks proxy instead
			type = Proxy.Type.SOCKS;
			port = getSystemProperty(
					"socksProxyPort", String.valueOf(SOCKS_PROXY_PORT)); //$NON-NLS-1$
		}
		int defaultPort = (type == Proxy.Type.SOCKS) ? SOCKS_PROXY_PORT
				: HTTP_PROXY_PORT;
		return createProxy(type, host, port, defaultPort);
	}

	/*
	 * Gets proxy for https request.
	 */
	private Proxy selectHttpsProxy() {
		String host ; 
		String port = null;
		Proxy.Type type = Proxy.Type.DIRECT;
		
		host = getSystemProperty("https.proxyHost");
		if (null != host) {
			// case 1: use exact https proxy
			type = Proxy.Type.HTTP;
			port = getSystemProperty(
					"https.proxyPort", String.valueOf(HTTPS_PROXY_PORT)); //$NON-NLS-1$
		} else {
			host = getSystemProperty("socksProxyHost"); //$NON-NLS-1$
			if (null != host) {
				// case 2: use socks proxy instead
				type = Proxy.Type.SOCKS;
				port = getSystemProperty(
						"socksProxyPort", String.valueOf(SOCKS_PROXY_PORT)); //$NON-NLS-1$
			}
		}
		int defaultPort = (type == Proxy.Type.SOCKS) ? SOCKS_PROXY_PORT
				: HTTPS_PROXY_PORT;
		return createProxy(type, host, port, defaultPort);
	}

	/*
	 * Gets proxy for ftp request.
	 */

	private Proxy selectFtpProxy(String uriHost) {
		String host;
		String port = null;
		Proxy.Type type = Proxy.Type.DIRECT;
		String nonProxyHosts = getSystemProperty("ftp.nonProxyHosts"); //$NON-NLS-1$
		// if host is in non proxy host list, returns Proxy.NO_PROXY
		if (isNonProxyHost(uriHost, nonProxyHosts)) {
			return Proxy.NO_PROXY;
		}
		
		host = getSystemProperty("ftp.proxyHost");
		if (null != host) {
			// case 1: use exact ftp proxy
			type = Proxy.Type.HTTP;
			port = getSystemProperty(
					"ftp.proxyPort", String.valueOf(FTP_PROXY_PORT)); //$NON-NLS-1$
		} else {
			host = getSystemProperty("socksProxyHost"); //$NON-NLS-1$
			if (null != host) {
				// case 2: use socks proxy instead
				type = Proxy.Type.SOCKS;
				port = getSystemProperty(
						"socksProxyPort", String.valueOf(SOCKS_PROXY_PORT)); //$NON-NLS-1$
			}
		}
		int defaultPort = (type == Proxy.Type.SOCKS) ? SOCKS_PROXY_PORT
				: FTP_PROXY_PORT;
		return createProxy(type, host, port, defaultPort);
	}

	/*
	 * Gets proxy for socks request.
	 */
	private Proxy selectSocksProxy() {
		String host;
		String port = null;
		Proxy.Type type = Proxy.Type.DIRECT;
		
		host = getSystemProperty("socksProxyHost"); //$NON-NLS-1$
		if (null != host) {
			type = Proxy.Type.SOCKS;
			port = getSystemProperty(
					"socksProxyPort", String.valueOf(SOCKS_PROXY_PORT)); //$NON-NLS-1$
		}
		return createProxy(type, host, port, SOCKS_PROXY_PORT);
	}

	/*
	 * checks whether the host needs proxy. return true if it doesn't need a
	 * proxy.
	 */
	private boolean isNonProxyHost(String host, String nonProxyHosts) {
		// nonProxyHosts is not set
		if (null == nonProxyHosts) {
			return false;
		}
		// Construct regex expression of nonProxyHosts
		int length = nonProxyHosts.length();
		char ch;
		StringBuilder buf = new StringBuilder(length);
		for (int i = 0; i < nonProxyHosts.length(); i++) {
			ch = nonProxyHosts.charAt(i);
			switch (ch) {
			case '.':
				buf.append("\\.");
				break;
			case '*':
				buf.append(".*");
				break;
			default:
				buf.append(ch);
			}
		}
		String nonProxyHostsReg = buf.toString();
		// check whether the host is the nonProxyHosts.
		return host.matches(nonProxyHostsReg);
	}

	/*
	 * Create Proxy by "type","host" and "port".
	 */
	private Proxy createProxy(Proxy.Type type, String host, String port,
			int defaultPort) {
		Proxy proxy;
		if (type == Proxy.Type.DIRECT) {
			proxy = Proxy.NO_PROXY;
		} else {
			int iPort;
			try {
				iPort = Integer.valueOf(port).intValue();
			} catch (NumberFormatException e) {
				iPort = defaultPort;
			}
			proxy = new Proxy(type, InetSocketAddress.createUnresolved(host,
					iPort));
		}
		return proxy;
	}

	/*
	 * gets system property, privileged operation. If the value of the property
	 * is null or empty String, it returns defaultValue.
	 */
	private String getSystemProperty(final String property) {
		return getSystemProperty(property, null);
	}

	/*
	 * gets system property, privileged operation. If the value of the property
	 * is null or empty String, it returns defaultValue.
	 */
	private String getSystemProperty(final String property,
			final String defaultVaule) {
		String value = AccessController.doPrivileged(
                new PriviAction<String>(property));
        if (null == value || "".equals(value)) {
            value = defaultVaule;
        }
        return value;
	}

	/*
	 * gets system property, privileged operation. If the value of "key"
	 * property is null, then retrieve value from "alternative" property.
	 * Finally, if the value is null or empty String, it returns defaultValue.
	 */
	private String getSystemPropertyOrAlternative(final String key,
			final String alternativeKey, final String defaultValue) {
		String value = getSystemProperty(key);
		if (value == null) {
			value = getSystemProperty(alternativeKey);
			if (null == value) {
				value = defaultValue;
			}
		}
		return value;
	}

}
