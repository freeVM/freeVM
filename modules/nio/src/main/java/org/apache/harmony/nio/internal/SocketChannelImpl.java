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
package org.apache.harmony.nio.internal;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;

import org.apache.harmony.luni.net.SocketImplProvider;
import org.apache.harmony.luni.platform.FileDescriptorHandler;
import org.apache.harmony.luni.platform.INetworkSystem;
import org.apache.harmony.luni.platform.Platform;
import org.apache.harmony.luni.util.Msg;


/*
 * 
 * The default implementation class of java.nio.channels.SocketChannel.
 * 
 */
class SocketChannelImpl extends SocketChannel implements FileDescriptorHandler {

    // -------------------------------------------------------------------
    // Class variables
    // -------------------------------------------------------------------

    private static final String ERRORMSG_SOCKET_INVALID = "The socket argument is not a socket";

    private static final int MAX_PORT_NUMBER = 65535;

    private static final int EOF = -1;

    private static final String ERRMSG_SOCKET_NONBLOCKING_WOULD_BLOCK = "The socket is marked as nonblocking operation would block";

    // The singleton to do the native network operation.
    static final INetworkSystem networkSystem = Platform.getNetworkSystem();

    // status un-init, not initialized.
    static final int SOCKET_STATUS_UNINIT = EOF;

    // status before connect.
    static final int SOCKET_STATUS_UNCONNECTED = 0;

    // status connection pending
    static final int SOCKET_STATUS_PENDING = 1;

    // status after connection success
    static final int SOCKET_STATUS_CONNECTED = 2;

    // status closed.
    static final int SOCKET_STATUS_CLOSED = 3;

    // timeout used for non-block mode.
    private static final int TIMEOUT_NONBLOCK = 0;

    // timeout used for block mode.
    private static final int TIMEOUT_BLOCK = EOF;

    // step used for connect
    private static final int HY_SOCK_STEP_START = 0;

    // step used for finishConnect
    private static final int HY_PORT_SOCKET_STEP_CHECK = 1;

    // connect success
    private static final int CONNECT_SUCCESS = 0;

    // error msg
    private static final String ERRCODE_PORT_ERROR = "K0032"; //$NON-NLS-1$

    // error messages, for native dependent.
    private static final String ERRORMSG_ASYNCHRONOUSCLOSE = "The call was cancelled";

    // a address of localhost
    private static final byte[] localAddrArray = { 127, 0, 0, 1 };

    // -------------------------------------------------------------------
    // Instance Variables
    // -------------------------------------------------------------------

    // The fd to interact with native code
    FileDescriptor fd;

    // Our internal Socket.
    private Socket socket = null;

    // The address to be connected.
    InetSocketAddress connectAddress = null;

    // Local address of the this socket (package private for adapter)
    InetAddress localAddress = null;

    // local port
    int localPort;

    // At first, uninitialized.
    int status = SOCKET_STATUS_UNINIT;

    // whether the socket is bound
    boolean isBound = false;

    // lock for read and write
    private final Object readLock = new Object();

    private final Object writeLock = new Object();

    // lock for status
    // private final Object statusLock = new Object();

    // this content is a point used to set in connect_withtimeout() in pending
    // mode
    private Long connectContext = Long.valueOf("0"); //$NON-NLS-1$

    // used to store the trafficClass value which is simply returned
    // as the value that was set. We also need it to pass it to methods
    // that specify an address packets are going to be sent to
    private int trafficClass = 0;

    // -------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------

    /*
     * Constructor
     */
    public SocketChannelImpl(SelectorProvider selectorProvider)
            throws IOException {
        super(selectorProvider);
        fd = new FileDescriptor();
        status = SOCKET_STATUS_UNCONNECTED;
        networkSystem.createSocket(fd, true);
    }

    // Keep this to see if need next version
    // SocketChannelImpl(SelectorProvider selectorProvider, FileDescriptor fd,
    // SocketImpl si) {
    // super(selectorProvider);
    // fd = fd;
    // networkSystem = OSNetworkSystem.getOSNetworkSystem();
    // status = SOCKET_STATUS_UNCONNECTED;
    // networkSystem.createSocket(fd, true);
    // }

    /*
     * Package private constructor.
     */
    SocketChannelImpl(Socket aSocket, FileDescriptor aFd) {
        super(SelectorProvider.provider());
        socket = aSocket;
        fd = aFd;
        status = SOCKET_STATUS_UNCONNECTED;
    }

    // -------------------------------------------------------------------
    // Methods for getting internal Socket.
    // -------------------------------------------------------------------

    /*
     * Getting the internal Socket If we have not the socket, we create a new
     * one.
     */
    synchronized public Socket socket() {
        if (null == socket) {
            try {
                InetAddress addr = null;
                int port = 0;
                if (connectAddress != null) {
                    addr = connectAddress.getAddress();
                    port = connectAddress.getPort();
                }
                socket = new SocketAdapter(SocketImplProvider.getSocketImpl(fd,
                        localPort, addr, port), this);
            } catch (SocketException e) {
                return null;
            }
        }
        return socket;
    }

    // -------------------------------------------------------------------
    // Methods for connect and finishConnect
    // -------------------------------------------------------------------

    /*
     * @see java.nio.channels.SocketChannel#isConnected()
     */
    synchronized public boolean isConnected() {
        return status == SOCKET_STATUS_CONNECTED;
    }

    /*
     * status setting used by other class.
     */
    synchronized void setConnected() {
        status = SOCKET_STATUS_CONNECTED;
    }

    /*
     * @see java.nio.channels.SocketChannel#isConnectionPending()
     */
    synchronized public boolean isConnectionPending() {
        return status == SOCKET_STATUS_PENDING;
    }

    /*
     * @see java.nio.channels.SocketChannel#connect(java.net.SocketAddress)
     */
    public boolean connect(SocketAddress socketAddress) throws IOException {
        // status must be open and unconnected
        checkUnconnected();

        // check the address
        InetSocketAddress inetSocketAddress = validateAddress(socketAddress);

        int port = inetSocketAddress.getPort();
        String hostName = inetSocketAddress.getAddress().getHostName();
        if (port < 0 || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException(Msg
                    .getString(ERRCODE_PORT_ERROR));
        }
        // security check
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkConnect(hostName, port);
        }

        // connect result
        int result = EOF;
        boolean success = false;

        try {
            begin();
            if (!isBound) {
                // bind
                networkSystem.bind2(fd, 0, true, InetAddress
                        .getByAddress(localAddrArray));
            }

            localPort = networkSystem.getSocketLocalPort(fd, false);
            localAddress = networkSystem.getSocketLocalAddress(fd, false);

            if (isBlocking()) {
                result = networkSystem.connect(fd, trafficClass,
                        inetSocketAddress.getAddress(), inetSocketAddress
                                .getPort());

            } else {
                result = networkSystem.connectWithTimeout(fd, 0, trafficClass,
                        inetSocketAddress.getAddress(), inetSocketAddress
                                .getPort(), HY_SOCK_STEP_START, connectContext);
            }

            success = (CONNECT_SUCCESS == result);

            isBound = success;
        } catch (IOException e) {
            if (e instanceof ConnectException && !isBlocking()) {
                status = SOCKET_STATUS_PENDING;
            } else {
                close();
                throw e;
            }
        } finally {
            end(success);
        }

        // set the connected address.
        connectAddress = inetSocketAddress;
        synchronized (this) {
            if (isBlocking()) {
                status = (success ? SOCKET_STATUS_CONNECTED
                        : SOCKET_STATUS_UNCONNECTED);
            } else {
                status = SOCKET_STATUS_PENDING;
            }
        }
        return success;
    }

    /*
     * @see java.nio.channels.SocketChannel#finishConnect()
     */
    public boolean finishConnect() throws IOException {
        // status check
        synchronized (this) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            if (status == SOCKET_STATUS_CONNECTED) {
                return true;
            }
            if (status != SOCKET_STATUS_PENDING) {
                throw new NoConnectionPendingException();
            }
        }

        // finish result
        int result = EOF;
        boolean success = false;

        try {
            begin();
            if (isBlocking()) {
                result = networkSystem.connect(fd, trafficClass, connectAddress
                        .getAddress(), connectAddress.getPort());

            } else {
                result = networkSystem.connectWithTimeout(fd, 0, trafficClass,
                        connectAddress.getAddress(), connectAddress.getPort(),
                        HY_PORT_SOCKET_STEP_CHECK, connectContext);
            }
            success = (result == CONNECT_SUCCESS);
        } catch (ConnectException e) {
            close();
            throw e;
        } finally {
            end(success);
        }

        synchronized (this) {
            status = (success ? SOCKET_STATUS_CONNECTED : status);
            isBound = success;
        }
        return success;
    }

    // -------------------------------------------------------------------
    // Methods for read and write
    // -------------------------------------------------------------------
    /*
     * @see java.nio.channels.SocketChannel#read(java.nio.ByteBuffer)
     */
    public int read(ByteBuffer target) throws IOException {
        if (null == target) {
            throw new NullPointerException();
        }
        checkOpenConnected();

        synchronized (readLock) {
            return readImpl(target);
        }
    }

    /*
     * @see java.nio.channels.SocketChannel#read(java.nio.ByteBuffer[], int,
     *      int)
     */
    public long read(ByteBuffer[] targets, int offset, int length)
            throws IOException {
        if (isIndexValid(targets, offset, length)) {
            checkOpenConnected();
            synchronized (readLock) {
                long totalCount = 0;
                for (int val = offset; val < offset + length; val++) {
                    int readCount = readImpl(targets[val]);
                    // only -1 or a integer >=0 may return
                    if (EOF != readCount) {
                        totalCount = totalCount + readCount;
                    } else {
                        break;
                    }
                }
                return totalCount;
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private boolean isIndexValid(ByteBuffer[] targets, int offset, int length) {
        return (length >= 0) && (offset >= 0)
                && (length + offset <= targets.length);
    }

    /*
     * read from channel, and store the result in the target.
     * 
     */
    private int readImpl(ByteBuffer target) throws IOException {
        if (!target.hasRemaining()) {
            return 0;
        }

        int readCount = 0;

        try {
            begin();
            byte[] readArray = new byte[target.remaining()];
            readCount = networkSystem.read(fd, readArray, 0, readArray.length,
                    (isBlocking() ? TIMEOUT_BLOCK : TIMEOUT_NONBLOCK));
            if (EOF != readCount) {
                target.put(readArray, 0, readCount);
            }
            return readCount;
        } catch (SocketException e) {
            // FIXME improve native code
            if (ERRORMSG_ASYNCHRONOUSCLOSE.equals(e.getMessage())
                    || ERRORMSG_SOCKET_INVALID.equals(e.getMessage())) {
                throw new AsynchronousCloseException();
            }
            throw e;
        } finally {
            end(readCount > 0);
        }
    }

    /*
     * 
     * @see java.nio.channels.SocketChannel#write(java.nio.ByteBuffer)
     */
    public int write(ByteBuffer source) throws IOException {
        checkOpenConnected();
        synchronized (writeLock) {
            return writeImpl(source);
        }
    }

    /*
     * @see java.nio.channels.SocketChannel#write(java.nio.ByteBuffer[], int,
     *      int)
     */
    public long write(ByteBuffer[] sources, int offset, int length)
            throws IOException {
        if (isIndexValid(sources, offset, length)) {
            checkOpenConnected();
            synchronized (writeLock) {
                long writeCount = 0;
                for (int val = offset; val < offset + length; val++) {
                    writeCount = writeCount + writeImpl(sources[val]);
                }
                return writeCount;
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /*
     * wirte the source. return the count of bytes written.
     */
    private int writeImpl(ByteBuffer source) throws IOException {
        if (!source.hasRemaining()) {
            return 0;
        }
        int writeCount = 0;
        try {
            begin();
            int pos = source.position();
            byte[] array;
            // FIXME enhance the perform
            if (source.hasArray()) {
                array = source.array();
            } else {
                array = new byte[source.remaining()];
                source.get(array);
            }
            networkSystem.setNonBlocking(fd, !this.isBlocking());
            writeCount = networkSystem.write(fd, array, 0, array.length);
            source.position(pos + writeCount);
            return writeCount;
        } catch (SocketException e) {
            if (ERRMSG_SOCKET_NONBLOCKING_WOULD_BLOCK.equals(e.getMessage())) {
                return writeCount;
            }
            if (ERRORMSG_ASYNCHRONOUSCLOSE.equals(e.getMessage())) {
                throw new AsynchronousCloseException();
            }
            throw e;
        } finally {
            end(writeCount >= 0);
        }
    }

    // -------------------------------------------------------------------
    // Shared methods
    // -------------------------------------------------------------------

    /*
     * status check, open and "connected", when read and write.
     */
    synchronized private void checkOpenConnected()
            throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!isConnected()) {
            throw new NotYetConnectedException();
        }
    }

    /*
     * status check, open and "unconnected", before connection.
     */
    synchronized private void checkUnconnected() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (status == SOCKET_STATUS_CONNECTED) {
            throw new AlreadyConnectedException();
        }
        if (status == SOCKET_STATUS_PENDING) {
            throw new ConnectionPendingException();
        }
    }

    /*
     * shared by this class and DatagramChannelImpl, to do the address transfer
     * and check.
     */
    static InetSocketAddress validateAddress(SocketAddress socketAddress) {
        if (null == socketAddress) {
            throw new IllegalArgumentException();
        }
        if (!(socketAddress instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        if (inetSocketAddress.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
        return inetSocketAddress;
    }

    /*
     * get local address
     */
    public InetAddress getLocalAddress() throws UnknownHostException {
        byte[] any_bytes = { 0, 0, 0, 0 };
        if (!isBound) {
            return InetAddress.getByAddress(any_bytes);
        }
        return localAddress;
    }

    // -------------------------------------------------------------------
    // Protected inherited methods
    // -------------------------------------------------------------------
    /*
     * do really closing action here
     */
    synchronized protected void implCloseSelectableChannel() throws IOException {
        if (SOCKET_STATUS_CLOSED != status) {
            status = SOCKET_STATUS_CLOSED;
            if (null != socket && !socket.isClosed()) {
                socket.close();
            } else {
                networkSystem.socketClose(fd);
            }
        }
    }

    /*
     * @see java.nio.channels.spi.AbstractSelectableChannel#implConfigureBlocking(boolean)
     */
    protected void implConfigureBlocking(boolean blockMode) throws IOException {
        synchronized (blockingLock()) {
            networkSystem.setNonBlocking(fd, !blockMode);
        }
    }

    // -------------------------------------------------------------------
    // Adapter classes for internal socket.
    // -------------------------------------------------------------------

    /*
     * get the fd
     */
    public FileDescriptor getFD() {
        return fd;
    }

    public InetAddress getAddress() {
        return (null == this.connectAddress) ? null : this.connectAddress
                .getAddress();
    }

    public int getPort() {
        return (null == this.connectAddress) ? 0 : this.connectAddress
                .getPort();
    }

    public int getLocalPort() {
        return this.localPort;
    }

    private static class SocketAdapter extends Socket {

        // ----------------------------------------------------
        // Class Variables
        // ----------------------------------------------------

        private static final String ERRCODE_CHANNEL_NOT_CONNECTED = "K0320"; //$NON-NLS-1$

        private static final String ERRCODE_CHANNEL_CLOSED = "K003d"; //$NON-NLS-1$

        SocketChannelImpl channel;

        SocketImpl socketImpl;

        // ----------------------------------------------------
        // Methods
        // ----------------------------------------------------

        SocketAdapter(SocketImpl socketimpl, SocketChannelImpl channel)
                throws SocketException {
            super(socketimpl);
            socketImpl = socketimpl;
            this.channel = channel;
        }

        /*
         * 
         * @see java.net.Socket#getChannel()
         */
        public SocketChannel getChannel() {
            return channel;
        }

        /*
         * 
         * @see java.net.Socket#isBound()
         */
        public boolean isBound() {
            return channel.isBound;
        }

        /*
         * 
         * @see java.net.Socket#isConnected()
         */
        public boolean isConnected() {
            return channel.isConnected();
        }

        /*
         * 
         * @see java.net.Socket#getLocalAddress()
         */
        public InetAddress getLocalAddress() {
            try {
                return channel.getLocalAddress();
            } catch (UnknownHostException e) {
                return null;
            }
        }

        /*
         * 
         * @see java.net.Socket#connect(java.net.SocketAddress, int)
         */
        public void connect(SocketAddress remoteAddr, int timeout)
                throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            if (isConnected()) {
                throw new AlreadyConnectedException();
            }
            super.connect(remoteAddr, timeout);
            channel.localAddress = networkSystem.getSocketLocalAddress(
                    channel.fd, false);
            if (super.isConnected()) {
                channel.setConnected();
                channel.isBound = super.isBound();
            }
        }

        /*
         * 
         * @see java.net.Socket#bind(java.net.SocketAddress)
         */
        public void bind(SocketAddress localAddr) throws IOException {
            if (channel.isConnected()) {
                throw new AlreadyConnectedException();
            }
            if (SocketChannelImpl.SOCKET_STATUS_PENDING == channel.status) {
                throw new ConnectionPendingException();
            }
            super.bind(localAddr);
            // keep here to see if need next version
            // channel.Address = getLocalSocketAddress();
            // channel.localport = getLocalPort();
            channel.isBound = true;

        }

        /*
         * 
         * @see java.net.Socket#close()
         */
        public void close() throws IOException {
            synchronized (channel) {
                if (channel.isOpen()) {
                    channel.close();
                } else {
                    super.close();
                }
                channel.status = SocketChannelImpl.SOCKET_STATUS_CLOSED;
            }
        }

        /*
         * 
         * @see java.net.Socket#getKeepAlive()
         */
        public boolean getKeepAlive() throws SocketException {
            checkClosedAndCreate(true);
            return ((Boolean) socketImpl.getOption(SocketOptions.SO_KEEPALIVE))
                    .booleanValue();
        }

        /*
         * 
         * @see java.net.Socket#getOOBInline()
         */
        public boolean getOOBInline() throws SocketException {
            checkClosedAndCreate(true);
            return ((Boolean) socketImpl.getOption(SocketOptions.SO_OOBINLINE))
                    .booleanValue();
        }

        /*
         * 
         * @see java.net.Socket#getSoLinger()
         */
        public int getSoLinger() throws SocketException {
            checkClosedAndCreate(true);
            return ((Integer) socketImpl.getOption(SocketOptions.SO_LINGER))
                    .intValue();
        }

        /*
         * 
         * @see java.net.Socket#getTcpNoDelay()
         */
        public boolean getTcpNoDelay() throws SocketException {
            checkClosedAndCreate(true);
            return ((Boolean) socketImpl.getOption(SocketOptions.TCP_NODELAY))
                    .booleanValue();
        }

        /*
         * 
         * @see java.net.Socket#getOutputStream()
         */
        public OutputStream getOutputStream() throws IOException {
            return new SocketChannelOutputStream(super.getOutputStream(),
                    channel);
        }

        /*
         * 
         * @see java.net.Socket#getInputStream()
         */
        public InputStream getInputStream() throws IOException {
            return new SocketChannelInputStream(super.getInputStream(), channel);
        }

        /*
         * checl if channel is close or create a new one.
         */
        private void checkClosedAndCreate(boolean create)
                throws SocketException {
            if (isClosed()) {
                throw new SocketException(Msg.getString(ERRCODE_CHANNEL_CLOSED));
            }
            if (!create && !isConnected()) {
                throw new SocketException(Msg
                        .getString(ERRCODE_CHANNEL_NOT_CONNECTED));
            }
            // FIXME check if need cread fd
        }

        /*
         * used for net and nio exchange
         */
        public SocketImpl getImpl() {
            return socketImpl;
        }
    }

    private static class SocketChannelOutputStream extends OutputStream {
        SocketChannel channel;

        OutputStream wrapped;

        public SocketChannelOutputStream(OutputStream wrapped,
                SocketChannel channel) {
            this.channel = channel;
            this.wrapped = wrapped;
        }

        /*
         * 
         * @see com.ibm.io.nio.SocketOutputStream#write(byte[], int, int)
         */
        public void write(byte[] buffer, int offset, int count)
                throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            wrapped.write(buffer, offset, count);
        }

        /*
         * 
         * @see com.ibm.io.nio.SocketOutputStream#write(byte[])
         */
        public void write(byte[] buffer) throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            wrapped.write(buffer);
        }

        /*
         * 
         * @see com.ibm.io.nio.SocketOutputStream#write(int)
         */
        public void write(int oneByte) throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            wrapped.write(oneByte);
        }
    }

    private static class SocketChannelInputStream extends InputStream {
        SocketChannel channel;

        InputStream wrapped;

        public SocketChannelInputStream(InputStream wrapped,
                SocketChannel channel) {
            this.channel = channel;
            this.wrapped = wrapped;
        }

        /*
         * 
         * @see com.ibm.io.nio.SocketInputStream#read()
         */
        public int read() throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            return wrapped.read();
        }

        /*
         * 
         * @see com.ibm.io.nio.SocketInputStream#read(byte[], int, int)
         */
        public int read(byte[] buffer, int offset, int count)
                throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            return wrapped.read(buffer, offset, count);
        }

        /*
         * 
         * @see com.ibm.io.nio.SocketInputStream#read(byte[])
         */
        public int read(byte[] buffer) throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            return wrapped.read(buffer);
        }
    }
}
