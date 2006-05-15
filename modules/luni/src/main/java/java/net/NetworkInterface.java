/* Copyright 2005, 2006 The Apache Software Foundation or its licensors, as applicable
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


import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.harmony.luni.util.Msg;

/**
 * This class provides an methods that are used to get information about the
 * network interfaces supported by the system
 */
public final class NetworkInterface extends Object {

	private static final int CHECK_CONNECT_NO_PORT = -1;

	static final int NO_INTERFACE_INDEX = 0;

	static final int UNSET_INTERFACE_INDEX = -1;

	private String name = null;

	private String displayName = null;

	InetAddress addresses[] = null;

	// The interface index is a positive integer which is non-negative. Where
	// value is zero then we do not have an index for the interface (which
	// occurs in systems which only support IPV4)
	private int interfaceIndex = 0;

	private int hashCode = 0;

	/**
	 * This native answers the list of network interfaces supported by the
	 * system. An array is returned which is easier to generate and which can
	 * easily be converted into the required enumeration on the java side
	 * 
	 * @return an array of zero or more NetworkInterface objects
	 * 
	 * @throws SocketException
	 *             if an error occurs when getting network interface information
	 */
	private static native NetworkInterface[] getNetworkInterfacesImpl()
			throws SocketException;

	/**
	 * This constructor is used by the native method in order to construct the
	 * NetworkInterface objects in the array tht it returns
	 * 
	 * @param name
	 *            internal name associated with the interface
	 * @param displayName
	 *            a user interpretable name for the interface
	 * @param addresses
	 *            the Inet addresses associated with the interface
	 * @param interfaceIndex
	 *            an index for the interface. Only set for platforms that
	 *            support IPV6
	 */
	NetworkInterface(String name, String displayName, InetAddress addresses[],
			int interfaceIndex) {
		this.name = name;
		this.displayName = displayName;
		this.addresses = addresses;
		this.interfaceIndex = interfaceIndex;
	}

	/**
	 * Answers the index for the network interface. Unless the system supports
	 * IPV6 this will be 0.
	 * 
	 * @return the index
	 */
	int getIndex() {
		return interfaceIndex;
	}

	/**
	 * Answers the first address for the network interface. This is used in the
	 * natives when we need one of the addresses for the interface and any one
	 * will do
	 * 
	 * @return the first address if one exists, otherwise null.
	 */
	InetAddress getFirstAddress() {
		if ((addresses != null) && (addresses.length >= 1)) {
			return addresses[0];
		}
		return null;
	}

	/**
	 * Answers the name associated with the network interface
	 * 
	 * @return name associated with the network interface
	 */
	public String getName() {
		return name;
	}

	/**
	 * Answers the list of inet addresses bound to the interface
	 * 
	 * @return list of inet addresses bound to the interface
	 */
	public Enumeration getInetAddresses() {
		// create new vector from which Enumeration to be returned can be
		// generated set the initial capacity to be the number of addresses for
		// the network interface which is the maximum required size

		// return an empty enumeration if there are no addresses associated with the
		// interface
		if (addresses == null) {
			return new Vector(0).elements();
		}

		// for those configuration that support the security manager we only
		// return addresses for which checkConnect returns true
		Vector accessibleAddresses = new Vector(addresses.length);

		// get the security manager. If one does not exist just return
		// the full list
		SecurityManager security = System.getSecurityManager();
		if (security == null) {
			return (new Vector(Arrays.asList(addresses))).elements();
		}

		// ok security manager exists so check each address and return
		// those that pass
		for (int i = 0; i < addresses.length; i++) {
			if (security != null) {
				try {
					// since we don't have a port in this case we pass in
					// NO_PORT
					security.checkConnect(addresses[i].getHostName(),
							CHECK_CONNECT_NO_PORT);
					accessibleAddresses.add(addresses[i]);
				} catch (SecurityException e) {
				}
			}
		}

		Enumeration theAccessibleElements = accessibleAddresses.elements();

		if (theAccessibleElements.hasMoreElements()) {
			return accessibleAddresses.elements();
		}
		
		return new Vector(0).elements();
	}

	/**
	 * Answers the user readable name associated with the network interface
	 * 
	 * @return display name associated with the network interface or null if one
	 *         is not available
	 */
	public String getDisplayName() {
		// we should return the display name unless it is blank in this
		// case return the name so that something is displayed.
		if (!(displayName.equals(""))) {
			return displayName;
		}
		return name;

	}

	/**
	 * Answers the network interface with the specified name, if one exists
	 * 
	 * @return network interface for name specified if it exists, otherwise null
	 * 
	 * @throws SocketException
	 *             if an error occurs when getting network interface information
	 * @throws NullPointerException
	 *             if the interface name passed in is null
	 */
	public static NetworkInterface getByName(String interfaceName)
			throws SocketException {

		if (interfaceName == null) {
			throw new NullPointerException(Msg.getString("K0330"));
		}

		// get the list of interfaces, and then loop through the list to look
		// for one with a matching name
		Enumeration interfaces = getNetworkInterfaces();
		if (interfaces != null) {
			while (interfaces.hasMoreElements()) {
				NetworkInterface netif = (NetworkInterface) interfaces
						.nextElement();
				if (netif.getName().equals(interfaceName)) {
					return netif;
				}
			}
		}
		return null;
	}

	/**
	 * Answers the network interface which has the specified inet address bound
	 * to it, if one exists.
	 * 
	 * @param address
	 *            address of interest
	 * @return network interface for inet address specified if it exists,
	 *         otherwise null
	 * 
	 * @throws SocketException
	 *             if an error occurs when getting network interface information
	 * @throws NullPointerException
	 *             if the address passed in is null
	 */
	public static NetworkInterface getByInetAddress(InetAddress address)
			throws SocketException {

		if (address == null) {
			throw new NullPointerException(Msg.getString("K0331"));
		}

		// get the list of interfaces, and then loop through the list. For each
		// interface loop through the associated set of inet addresses and see
		// if one matches. If so return that network interface
		Enumeration interfaces = getNetworkInterfaces();
		if (interfaces != null) {
			while (interfaces.hasMoreElements()) {
				NetworkInterface netif = (NetworkInterface) interfaces
						.nextElement();
				// to be compatible use the raw addresses without any security
				// filtering
				// Enumeration netifAddresses = netif.getInetAddresses();
				if ((netif.addresses != null) && (netif.addresses.length != 0)) {
					Enumeration netifAddresses = (new Vector(Arrays
							.asList(netif.addresses))).elements();
					if (netifAddresses != null) {
						while (netifAddresses.hasMoreElements()) {
							if (address.equals(netifAddresses
									.nextElement())) {
								return netif;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Answers the list of network interfaces supported by the system or null if
	 * no interfaces are supported by the system
	 * 
	 * @return Enumeration containing one NetworkInterface object for each
	 *         interface supported by the system
	 * 
	 * @throws SocketException
	 *             if an error occurs when getting network interface information
	 */
	public static Enumeration getNetworkInterfaces() throws SocketException {
		NetworkInterface[] interfaces = getNetworkInterfacesImpl();
		if (interfaces == null) {
			return null;
		}
		return (new Vector(Arrays.asList(interfaces))).elements();
	}

	/**
	 * Compares the specified object to this NetworkInterface and answer if they
	 * are equal. The object must be an instance of NetworkInterface with the
	 * same name, displayName and list of network interfaces to be the same
	 * 
	 * @param obj
	 *            the object to compare
	 * @return true if the specified object is equal to this NetworkInterfcae,
	 *         false otherwise
	 * 
	 * @see #hashCode
	 */
	public boolean equals(Object obj) {

		// just return true if it is the exact same object
		if (obj == this)
			return true;

		if (obj instanceof NetworkInterface) {
			// make sure that some simple checks pass. If the name is not the
			// same then we are sure it is not the same one. We don't check the
			// hashcode as it is generated from the name which we check
			NetworkInterface netif = (NetworkInterface) obj;

			if (netif.getIndex() != interfaceIndex) {
				return false;
			}

			if (!(name.equals("")) && (!netif.getName().equals(name))) {
				return false;
			}

			if ((name.equals("")) && (!netif.getName().equals(displayName))) {
				return false;
			}

			// now check that the inet addresses are the same
			Enumeration netifAddresses = netif.getInetAddresses();
			Enumeration localifAddresses = getInetAddresses();
			if ((netifAddresses == null) && (localifAddresses != null)) {
				return false;
			}

			if ((netifAddresses == null) && (localifAddresses == null)) {
				// neither have any addresses so they are the same
				return true;
			}

			if (netifAddresses != null) {
				while (netifAddresses.hasMoreElements()
						&& localifAddresses.hasMoreElements()) {
					if (!((InetAddress) localifAddresses.nextElement())
							.equals(netifAddresses.nextElement())) {
						return false;
					}
				}
				// now make sure that they had the same number of inet
				// addresses, if not
				// they are not the same interfac
				if (netifAddresses.hasMoreElements()
						|| localifAddresses.hasMoreElements()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Answers a hash code for this NetworkInterface object. Since the name
	 * should be unique for each network interface the hash code is generated
	 * using this name
	 * 
	 * @return the hashcode for hashtable indexing
	 */
	public int hashCode() {
		if (hashCode == 0) {
			hashCode = name.hashCode();
		}
		return hashCode;
	}

	/**
	 * Answers a string containing a concise, human-readable description of the
	 * network interface
	 * 
	 * @return a printable representation for the network interface
	 */
	public String toString() {
		StringBuffer stringRepresentation = new StringBuffer("[" + name + "]["
				+ displayName + "]");

		// get the addresses through this call to make sure we only reveal
		// those that we should
		Enumeration theAddresses = getInetAddresses();
		if (theAddresses != null) {
			while (theAddresses.hasMoreElements()) {
				InetAddress nextAddress = (InetAddress) theAddresses
						.nextElement();
				stringRepresentation.append("[" + nextAddress.toString() + "]");
			}
		}
		return stringRepresentation.toString();
	}

}
