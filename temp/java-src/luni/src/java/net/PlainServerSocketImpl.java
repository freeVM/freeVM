/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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


import java.io.FileDescriptor;

/**
 * This class was added so we can create sockets with options that are needed
 * for server sockets. It just overrides create so that we call new natives
 * which only set the options required for server sockets. In order to preserve
 * behaviour of older versions the create PlainSocketImpl was left as is and
 * this new class was added. For newer versions an instance of this class is
 * used, for earlier versions the original PlainSocketImpl is used.
 */
class PlainServerSocketImpl extends PlainSocketImpl {

	/**
	 * Answer the result of attempting to create a server stream socket in the
	 * IP stack. Any special options required for server sockets will be set by
	 * this method.
	 * 
	 * @param aFD
	 *            the socket FileDescriptor
	 * @exception SocketException
	 *                if an error occurs while creating the socket
	 */
	static native void createServerStreamSocketImpl(FileDescriptor aFD,
			boolean preferIPv4Stack) throws SocketException;

	/**
	 * Creates a new unconnected socket. If streaming is true, create a stream
	 * socket, else a datagram socket. The deprecated datagram usage is not
	 * supported and will throw an exception.
	 * 
	 * @param isStreaming
	 *            true, if the socket is type streaming
	 * @exception SocketException
	 *                if an error occurs while creating the socket
	 */
	protected void create(boolean isStreaming) throws SocketException {
		this.streaming = isStreaming;
		if (isStreaming) {
			createServerStreamSocketImpl(fd, Socket.preferIPv4Stack());
		} else {
			createDatagramSocketImpl(fd, Socket.preferIPv4Stack());
		}
	}
}
