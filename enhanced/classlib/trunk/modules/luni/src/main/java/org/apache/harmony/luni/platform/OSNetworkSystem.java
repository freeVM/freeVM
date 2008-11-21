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

package org.apache.harmony.luni.platform;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.Channel;

/**
 * This wraps native code that implements the INetworkSystem interface.
 */
final class OSNetworkSystem implements INetworkSystem {

    private static final int ERRORCODE_SOCKET_TIMEOUT = -209;

    private static final int INETADDR_REACHABLE = 0;

    private static boolean isNetworkInited = false;

    private static OSNetworkSystem ref = new OSNetworkSystem();

    // Can not be instantiated.
    private OSNetworkSystem() {
        super();
    }

    /**
     * Answers the unique instance of the OSNetworkSystem.
     * 
     * @return the network system interface instance
     */
    public static OSNetworkSystem getOSNetworkSystem() {
        return ref;
    }

    public void createSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws IOException {
        createSocketImpl(fd, preferIPv4Stack);
    }

    public void createDatagramSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws SocketException {
        createDatagramSocketImpl(fd, preferIPv4Stack);
    }

    public int read(FileDescriptor aFD, byte[] data, int offset, int count,
            int timeout) throws IOException {
        return readSocketImpl(aFD, data, offset, count, timeout);
    }

    public int readDirect(FileDescriptor aFD, long address, int count,
            int timeout) throws IOException {
        return readSocketDirectImpl(aFD, address, count, timeout);
    }

    public int write(FileDescriptor aFD, byte[] data, int offset, int count)
            throws IOException {
        return writeSocketImpl(aFD, data, offset, count);
    }

    public int writeDirect(FileDescriptor aFD, long address, int count)
            throws IOException {
        return writeSocketDirectImpl(aFD, address, count);
    }

    public void setNonBlocking(FileDescriptor aFD, boolean block)
            throws IOException {
        setNonBlockingImpl(aFD, block);
    }

    public void connectDatagram(FileDescriptor aFD, int port, int trafficClass,
            InetAddress inetAddress) throws SocketException {
        connectDatagramImpl2(aFD, port, trafficClass, inetAddress);
    }

    public int connect(FileDescriptor aFD, int trafficClass,
            InetAddress inetAddress, int port) throws IOException {
        return connectSocketImpl(aFD, trafficClass, inetAddress, port);
    }

    public int connectWithTimeout(FileDescriptor aFD, int timeout,
            int trafficClass, InetAddress inetAddress, int port, int step,
            Long context) throws IOException {
        return connectWithTimeoutSocketImpl(aFD, timeout, trafficClass,
                inetAddress, port, step, context);
    }

    public void connectStreamWithTimeoutSocket(FileDescriptor aFD, int aport,
            int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException {
        connectStreamWithTimeoutSocketImpl(aFD, aport, timeout, trafficClass,
                inetAddress);
    }

    public void bind(FileDescriptor aFD, int port, InetAddress inetAddress)
            throws SocketException {
        socketBindImpl(aFD, port, inetAddress);
    }

    public boolean bind2(FileDescriptor aFD, int port, boolean bindToDevice,
            InetAddress inetAddress) throws SocketException {
        return socketBindImpl2(aFD, port, bindToDevice, inetAddress);
    }

    public void accept(FileDescriptor fdServer, SocketImpl newSocket,
            FileDescriptor fdnewSocket, int timeout) throws IOException {
        acceptSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    }

    public int sendDatagram(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException {
        return sendDatagramImpl(fd, data, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }

    public int sendDatagramDirect(FileDescriptor fd, long address, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException {
        return sendDatagramDirectImpl(fd, address, offset, length, port,
                bindToDevice, trafficClass, inetAddress);
    }

    public int sendDatagram2(FileDescriptor fd, byte[] data, int offset,
            int length, int port, InetAddress inetAddress) throws IOException {
        return sendDatagramImpl2(fd, data, offset, length, port, inetAddress);
    }

    public int receiveDatagram(FileDescriptor aFD, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return receiveDatagramImpl(aFD, packet, data, offset, length,
                receiveTimeout, peek);
    }

    public int receiveDatagramDirect(FileDescriptor aFD, DatagramPacket packet,
            long address, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return receiveDatagramDirectImpl(aFD, packet, address, offset, length,
                receiveTimeout, peek);
    }

    public int recvConnectedDatagram(FileDescriptor aFD, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return recvConnectedDatagramImpl(aFD, packet, data, offset, length,
                receiveTimeout, peek);
    }

    public int recvConnectedDatagramDirect(FileDescriptor aFD,
            DatagramPacket packet, long address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException {
        return recvConnectedDatagramDirectImpl(aFD, packet, address, offset,
                length, receiveTimeout, peek);
    }

    public int peekDatagram(FileDescriptor aFD, InetAddress sender,
            int receiveTimeout) throws IOException {
        return peekDatagramImpl(aFD, sender, receiveTimeout);
    }

    public int sendConnectedDatagram(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException {
        return sendConnectedDatagramImpl(fd, data, offset, length, bindToDevice);
    }

    public int sendConnectedDatagramDirect(FileDescriptor fd, long address,
            int offset, int length, boolean bindToDevice) throws IOException {
        return sendConnectedDatagramDirectImpl(fd, address, offset, length,
                bindToDevice);
    }

    public void disconnectDatagram(FileDescriptor aFD) throws SocketException {
        disconnectDatagramImpl(aFD);
    }

    public void createMulticastSocket(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException {
        createMulticastSocketImpl(aFD, preferIPv4Stack);
    }

    public void createServerStreamSocket(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException {
        createServerStreamSocketImpl(aFD, preferIPv4Stack);
    }

    public int receiveStream(FileDescriptor aFD, byte[] data, int offset,
            int count, int timeout) throws IOException {
        return receiveStreamImpl(aFD, data, offset, count, timeout);
    }

    public int sendStream(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException {
        return sendStreamImpl(fd, data, offset, count);
    }

    public void shutdownInput(FileDescriptor descriptor) throws IOException {
        shutdownInputImpl(descriptor);
    }

    public void shutdownOutput(FileDescriptor descriptor) throws IOException {
        shutdownOutputImpl(descriptor);
    }

    public boolean supportsUrgentData(FileDescriptor fd) {
        return supportsUrgentDataImpl(fd);
    }

    public void sendUrgentData(FileDescriptor fd, byte value) {
        sendUrgentDataImpl(fd, value);
    }

    public int availableStream(FileDescriptor aFD) throws SocketException {
        return availableStreamImpl(aFD);
    }

    public void acceptStreamSocket(FileDescriptor fdServer,
            SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
            throws IOException {
        acceptStreamSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    }

    public void createStreamSocket(FileDescriptor aFD, boolean preferIPv4Stack)
            throws SocketException {
        createStreamSocketImpl(aFD, preferIPv4Stack);
    }

    public void listenStreamSocket(FileDescriptor aFD, int backlog)
            throws SocketException {
        listenStreamSocketImpl(aFD, backlog);
    }

    public boolean isReachableByICMP(final InetAddress dest,
            InetAddress source, final int ttl, final int timeout) {
        return INETADDR_REACHABLE == isReachableByICMPImpl(dest, source, ttl,
                timeout);
    }

    /**
     * Select the given file descriptors for read and write operations.
     * 
     * The file descriptors passed in as readFDs will be selected for read-ready
     * operations, and those in the writeFDs will be selected for write-ready
     * operations. A file descriptor can appear in either or both array, and
     * must not be <code>null</code>. If the file descriptor is closed during
     * the select the behavior depends upon the underlying OS.
     * 
     * Upon return the result is a single array of length
     * <code>readFDs.length</code> + <code>writeFDs.length</code> laid out as
     * the result of the select operation on the corresponding file descriptors.
     * 
     * @param readChannels
     *            all channels interested in read and accept
     * @param writeChannels
     *            all channels interested in write and connect
     * @param timeout
     *            timeout in millis
     * @returns int array, each element describes the corresponding state of the
     *          descriptor in the read and write arrays.
     * @throws SocketException
     */
    public int[] select(FileDescriptor[] readFDs, FileDescriptor[] writeFDs,
            long timeout) throws SocketException {
        int countRead = readFDs.length;
        int countWrite = writeFDs.length;
        int result = 0;
        if (0 == countRead + countWrite) {
            return (new int[0]);
        }
        int[] flags = new int[countRead + countWrite];

        assert validateFDs(readFDs, writeFDs) : "Invalid file descriptor arrays";

        // handle timeout in native
        result = selectImpl(readFDs, writeFDs, countRead, countWrite, flags,
                timeout);

        if (0 <= result) {
            return flags;
        }
        if (ERRORCODE_SOCKET_TIMEOUT == result) {
            return new int[0];
        }
        throw new SocketException();
    }

    /*
     * Used to check if the file descriptor arrays are valid before passing them
     * into the select native call.
     */
    private boolean validateFDs(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs) {
        for (FileDescriptor fd : readFDs) {
            // Also checks fd not null
            if (!fd.valid()) {
                return false;
            }
        }
        for (FileDescriptor fd : writeFDs) {
            if (!fd.valid()) {
                return false;
            }
        }
        return true;
    }

    public InetAddress getSocketLocalAddress(FileDescriptor aFD,
            boolean preferIPv6Addresses) {
        return getSocketLocalAddressImpl(aFD, preferIPv6Addresses);
    }

    /**
     * Query the IP stack for the local port to which this socket is bound.
     * 
     * @param aFD
     *            the socket descriptor @param preferIPv6Addresses address
     *            preference for nodes that support both IPv4 and IPv6 @return
     *            int the local port to which the socket is bound
     */
    public int getSocketLocalPort(FileDescriptor aFD,
            boolean preferIPv6Addresses) {
        return getSocketLocalPortImpl(aFD, preferIPv6Addresses);
    }

    /**
     * Query the IP stack for the nominated socket option.
     * 
     * @param aFD
     *            the socket descriptor @param opt the socket option type
     * @return the nominated socket option value
     * 
     * @throws SocketException
     *             if the option is invalid
     */
    public Object getSocketOption(FileDescriptor aFD, int opt)
            throws SocketException {
        return getSocketOptionImpl(aFD, opt);
    }

    /**
     * Set the nominated socket option in the IP stack.
     * 
     * @param aFD
     *            the socket descriptor @param opt the option selector @param
     *            optVal the nominated option value
     * 
     * @throws SocketException
     *             if the option is invalid or cannot be set
     */
    public void setSocketOption(FileDescriptor aFD, int opt, Object optVal)
            throws SocketException {
        setSocketOptionImpl(aFD, opt, optVal);
    }

    public int getSocketFlags() {
        return getSocketFlagsImpl();
    }

    /**
     * Close the socket in the IP stack.
     * 
     * @param aFD
     *            the socket descriptor
     */
    public void socketClose(FileDescriptor aFD) throws IOException {
        socketCloseImpl(aFD);
    }

    public InetAddress getHostByAddr(byte[] addr) throws UnknownHostException {
        return getHostByAddrImpl(addr);
    }

    public InetAddress getHostByName(String addr, boolean preferIPv6Addresses)
            throws UnknownHostException {
        return getHostByNameImpl(addr, preferIPv6Addresses);
    }

    public void setInetAddress(InetAddress sender, byte[] address) {
        setInetAddressImpl(sender, address);
    }

    static native void createSocketImpl(FileDescriptor fd,
            boolean preferIPv4Stack);

    /**
     * Allocate a datagram socket in the IP stack. The socket is associated with
     * the <code>aFD</code>.
     * 
     * @param aFD
     *            the FileDescriptor to associate with the socket @param
     *            preferIPv4Stack IP stack preference if underlying platform is
     *            V4/V6
     * @exception SocketException
     *                upon an allocation error
     */
    static native void createDatagramSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    static native int readSocketImpl(FileDescriptor aFD, byte[] data,
            int offset, int count, int timeout) throws IOException;

    static native int readSocketDirectImpl(FileDescriptor aFD, long address,
            int count, int timeout) throws IOException;

    static native int writeSocketImpl(FileDescriptor fd, byte[] data,
            int offset, int count) throws IOException;

    static native int writeSocketDirectImpl(FileDescriptor fd, long address,
            int count) throws IOException;

    static native void setNonBlockingImpl(FileDescriptor aFD, boolean block);

    static native int connectSocketImpl(FileDescriptor aFD, int trafficClass,
            InetAddress inetAddress, int port);

    static native int connectWithTimeoutSocketImpl(FileDescriptor aFD,
            int timeout, int trafficClass, InetAddress hostname, int port,
            int step, Long context);

    static native void connectStreamWithTimeoutSocketImpl(FileDescriptor aFD,
            int aport, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException;

    static native void socketBindImpl(FileDescriptor aFD, int port,
            InetAddress inetAddress) throws SocketException;

    static native void listenStreamSocketImpl(FileDescriptor aFD, int backlog)
            throws SocketException;

    static native int availableStreamImpl(FileDescriptor aFD)
            throws SocketException;

    static native void acceptSocketImpl(FileDescriptor fdServer,
            SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
            throws IOException;

    static native boolean supportsUrgentDataImpl(FileDescriptor fd);

    static native void sendUrgentDataImpl(FileDescriptor fd, byte value);

    /**
     * Connect the socket to a port and address
     * 
     * @param aFD
     *            the FileDescriptor to associate with the socket @param port
     *            the port to connect to @param trafficClass the traffic Class
     *            to be used then the connection is made @param inetAddress
     *            address to connect to.
     * 
     * @exception SocketException
     *                if the connect fails
     */
    static native void connectDatagramImpl2(FileDescriptor aFD, int port,
            int trafficClass, InetAddress inetAddress) throws SocketException;

    /**
     * Disconnect the socket to a port and address
     * 
     * @param aFD
     *            the FileDescriptor to associate with the socket
     * 
     * @exception SocketException
     *                if the disconnect fails
     */
    static native void disconnectDatagramImpl(FileDescriptor aFD)
            throws SocketException;

    /**
     * Allocate a datagram socket in the IP stack. The socket is associated with
     * the <code>aFD</code>.
     * 
     * @param aFD
     *            the FileDescriptor to associate with the socket @param
     *            preferIPv4Stack IP stack preference if underlying platform is
     *            V4/V6
     * @exception SocketException
     *                upon an allocation error
     */

    /**
     * Bind the socket to the port/localhost in the IP stack.
     * 
     * @param aFD
     *            the socket descriptor @param port the option selector @param
     *            bindToDevice bind the socket to the specified interface @param
     *            inetAddress address to connect to. @return if bind successful @exception
     *            SocketException thrown if bind operation fails
     */
    static native boolean socketBindImpl2(FileDescriptor aFD, int port,
            boolean bindToDevice, InetAddress inetAddress)
            throws SocketException;

    /**
     * Peek on the socket, update <code>sender</code> address and answer the
     * sender port.
     * 
     * @param aFD
     *            the socket FileDescriptor @param sender an InetAddress, to be
     *            updated with the sender's address @param receiveTimeout the
     *            maximum length of time the socket should block, reading @return
     *            int the sender port
     * 
     * @exception IOException
     *                upon an read error or timeout
     */
    static native int peekDatagramImpl(FileDescriptor aFD, InetAddress sender,
            int receiveTimeout) throws IOException;

    /**
     * Recieve data on the socket into the specified buffer. The packet fields
     * <code>data</code> & <code>length</code> are passed in addition to
     * <code>packet</code> to eliminate the JNI field access calls.
     * 
     * @param aFD
     *            the socket FileDescriptor @param packet the DatagramPacket to
     *            receive into @param data the data buffer of the packet @param
     *            offset the offset in the data buffer @param length the length
     *            of the data buffer in the packet @param receiveTimeout the
     *            maximum length of time the socket should block, reading @param
     *            peek indicates to peek at the data @return number of data
     *            received @exception IOException upon an read error or timeout
     */
    static native int receiveDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    static native int receiveDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, long address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    /**
     * Recieve data on the connected socket into the specified buffer. The
     * packet fields <code>data</code> & <code>length</code> are passed in
     * addition to <code>packet</code> to eliminate the JNI field access calls.
     * 
     * @param aFD
     *            the socket FileDescriptor @param packet the DatagramPacket to
     *            receive into @param data the data buffer of the packet @param
     *            offset the offset in the data buffer @param length the length
     *            of the data buffer in the packet @param receiveTimeout the
     *            maximum length of time the socket should block, reading @param
     *            peek indicates to peek at the data @return number of data
     *            received @exception IOException upon an read error or timeout
     */
    static native int recvConnectedDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    static native int recvConnectedDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, long address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    /**
     * Send the <code>data</code> to the nominated target <code>address</code>
     * and <code>port</code>. These values are derived from the DatagramPacket
     * to reduce the field calls within JNI.
     * 
     * @param fd
     *            the socket FileDescriptor @param data the data buffer of the
     *            packet @param offset the offset in the data buffer @param
     *            length the length of the data buffer in the packet @param port
     *            the target host port
     * @param bindToDevice
     *            if bind to device @param trafficClass the traffic class to be
     *            used when the datagram is sent @param inetAddress address to
     *            connect to. @return number of data send
     * 
     * @exception IOException
     *                upon an read error or timeout
     */
    static native int sendDatagramImpl(FileDescriptor fd, byte[] data,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException;

    static native int sendDatagramDirectImpl(FileDescriptor fd, long address,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException;

    /**
     * Send the <code>data</code> to the address and port to which the was
     * connnected and <code>port</code>.
     * 
     * @param fd
     *            the socket FileDescriptor @param data the data buffer of the
     *            packet @param offset the offset in the data buffer @param
     *            length the length of the data buffer in the packet @param
     *            bindToDevice not used, current kept in case needed as was the
     *            case for sendDatagramImpl @return number of data send @exception
     *            IOException upon an read error or timeout
     */
    static native int sendConnectedDatagramImpl(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException;

    static native int sendConnectedDatagramDirectImpl(FileDescriptor fd,
            long address, int offset, int length, boolean bindToDevice)
            throws IOException;

    /**
     * Answer the result of attempting to create a server stream socket in the
     * IP stack. Any special options required for server sockets will be set by
     * this method.
     * 
     * @param aFD
     *            the socket FileDescriptor @param preferIPv4Stack if use IPV4
     * @exception SocketException
     *                if an error occurs while creating the socket
     */
    static native void createServerStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    /**
     * Answer the result of attempting to create a multicast socket in the IP
     * stack. Any special options required for server sockets will be set by
     * this method.
     * 
     * @param aFD
     *            the socket FileDescriptor @param preferIPv4Stack if use IPV4
     * @exception SocketException
     *                if an error occurs while creating the socket
     */
    static native void createMulticastSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    /**
     * Recieve at most <code>count</code> bytes into the buffer
     * <code>data</code> at the <code>offset</code> on the socket.
     * 
     * @param aFD
     *            the socket FileDescriptor @param data the receive buffer
     * @param offset
     *            the offset into the buffer @param count the max number of
     *            bytes to receive @param timeout the max time the read
     *            operation should block waiting for data @return int the actual
     *            number of bytes read
     * @throws IOException
     * @exception SocketException
     *                if an error occurs while reading
     */
    static native int receiveStreamImpl(FileDescriptor aFD, byte[] data,
            int offset, int count, int timeout) throws IOException;

    /**
     * Send <code>count</code> bytes from the buffer <code>data</code> at the
     * <code>offset</code>, on the socket.
     * 
     * @param fd
     * 
     * @param data
     *            the send buffer @param offset the offset into the buffer
     * @param count
     *            the number of bytes to receive @return int the actual number
     *            of bytes sent @throws IOException @exception SocketException
     *            if an error occurs while writing
     */
    static native int sendStreamImpl(FileDescriptor fd, byte[] data,
            int offset, int count) throws IOException;

    private native void shutdownInputImpl(FileDescriptor descriptor)
            throws IOException;

    private native void shutdownOutputImpl(FileDescriptor descriptor)
            throws IOException;

    static native void acceptStreamSocketImpl(FileDescriptor fdServer,
            SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
            throws IOException;

    static native void createStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    static native int sendDatagramImpl2(FileDescriptor fd, byte[] data,
            int offset, int length, int port, InetAddress inetAddress)
            throws IOException;

    static native int selectImpl(FileDescriptor[] readfd,
            FileDescriptor[] writefd, int cread, int cwirte, int[] flags,
            long timeout);

    static native InetAddress getSocketLocalAddressImpl(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /**
     * Query the IP stack for the local port to which this socket is bound.
     * 
     * @param aFD
     *            the socket descriptor @param preferIPv6Addresses address
     *            preference for nodes that support both IPv4 and IPv6 @return
     *            int the local port to which the socket is bound
     */
    static native int getSocketLocalPortImpl(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /**
     * Query the IP stack for the nominated socket option.
     * 
     * @param aFD
     *            the socket descriptor @param opt the socket option type
     * @return the nominated socket option value
     * 
     * @throws SocketException
     *             if the option is invalid
     */
    static native Object getSocketOptionImpl(FileDescriptor aFD, int opt)
            throws SocketException;

    /**
     * Set the nominated socket option in the IP stack.
     * 
     * @param aFD
     *            the socket descriptor @param opt the option selector @param
     *            optVal the nominated option value
     * 
     * @throws SocketException
     *             if the option is invalid or cannot be set
     */
    static native void setSocketOptionImpl(FileDescriptor aFD, int opt,
            Object optVal) throws SocketException;

    static native int getSocketFlagsImpl();

    /**
     * Close the socket in the IP stack.
     * 
     * @param aFD
     *            the socket descriptor
     */
    static native void socketCloseImpl(FileDescriptor aFD);

    static native InetAddress getHostByAddrImpl(byte[] addr)
            throws UnknownHostException;

    static native InetAddress getHostByNameImpl(String addr,
            boolean preferIPv6Addresses) throws UnknownHostException;

    native void setInetAddressImpl(InetAddress sender, byte[] address);

    native int isReachableByICMPImpl(InetAddress addr, InetAddress local,
            int ttl, int timeout);

    native Channel inheritedChannelImpl();

    public Channel inheritedChannel() {
        return inheritedChannelImpl();
    }

    public void oneTimeInitialization(boolean jcl_supports_ipv6) {
        if (!isNetworkInited) {
            oneTimeInitializationImpl(jcl_supports_ipv6);
            isNetworkInited = true;
        }
    }

    native void oneTimeInitializationImpl(boolean jcl_supports_ipv6);
}
