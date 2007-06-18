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

#if defined(FREEBSD) || defined(AIX) || defined(MACOSX)
#include <sys/types.h>
#include <netinet/in.h>
#endif
#include <sys/poll.h>
#include <netinet/in_systm.h>
#include<netinet/ip.h>
#include<netinet/ip_icmp.h>
#include "nethelp.h"
#include "harmonyglob.h"
#include "hysock.h"
#include "hyport.h"
#include "jni.h"
#include "OSNetworkSystem.h"
#define NOPRIVILEGE -1
#define UNREACHABLE -2
#define REACHABLE 0
#define INVALID_SOCKET -1
#define SOCKET_ERROR -1

unsigned short ip_checksum(unsigned short * buffer, int size);
void set_icmp_packet(struct icmp * icmp_hdr, int packet_size);

// Alternative Select function
int
selectRead (JNIEnv * env,hysocket_t hysocketP, I_32 uSecTime, BOOLEAN accept){
  PORT_ACCESS_FROM_ENV (env);
  I_32 result = 0;
  struct pollfd my_pollfd;

  my_pollfd.fd = hysocketP->sock;
  my_pollfd.events = POLLIN | POLLPRI;
  my_pollfd.revents = 0;
  result = poll (&my_pollfd, 1, TO_MILLIS(0, uSecTime));
  
  return result;
}

JNIEXPORT jint JNICALL Java_org_apache_harmony_luni_platform_OSNetworkSystem_isReachableByICMPImpl
  (JNIEnv * env, jobject clz, jobject address, jobject localaddr,  jint ttl, jint timeout){
  struct sockaddr_in dest,source,local;
  struct icmp * send_buf = 0;
  struct ip * recv_buf = 0;
  int result,ret=UNREACHABLE;
  struct pollfd my_pollfd;
  int sockadd_size = sizeof (source);
  jbyte host[HYSOCK_INADDR6_LEN];
  U_32 length =  (*env)->GetArrayLength (env,address);

  int sock = socket(AF_INET, SOCK_RAW, IPPROTO_ICMP);
  if (INVALID_SOCKET == sock){
	return NOPRIVILEGE;
  }
  setuid(getuid());
  if (0 < ttl){
	  if (0 > setsockopt(sock, IPPROTO_ICMP, IP_TTL, (char*)&ttl,
	          sizeof(ttl))) {
	        return NOPRIVILEGE;
	  }
  }
  
  memset(&dest, 0, sizeof(dest));

  // set address
  netGetJavaNetInetAddressValue (env, address,(U_8 *)host, &length);
  memset(&dest, 0, sizeof(dest));	  
  memcpy (&dest.sin_addr.s_addr,(U_8 *)host, length);
  dest.sin_family = AF_INET;

  if(NULL != localaddr){
    memset(&local, 0, sizeof(local));
    netGetJavaNetInetAddressValue (env, localaddr,(U_8 *)host, &length);
    memcpy (&local.sin_addr.s_addr,(U_8 *)host, length);
    bind(sock, (struct sockaddr *)& local, sizeof(local));
  }

  send_buf = (struct icmp*)malloc(sizeof(char)*ICMP_SIZE);
  recv_buf = (struct ip*)malloc(sizeof(char)*PACKET_SIZE);
  if (NULL == send_buf || NULL == recv_buf){
	  ret = NOPRIVILEGE;
      goto cleanup;
  }
  set_icmp_packet(send_buf, ICMP_SIZE);

  if(SOCKET_ERROR == sendto(sock, (char*)send_buf, ICMP_SIZE, 0,
            (struct sockaddr*)&dest, sizeof(dest))){
            goto cleanup;
  }

  //don't ask what is it - just kinda sleep
  my_pollfd.fd = 0;
  my_pollfd.events = 0;
  result = poll(&my_pollfd, 1, timeout);

  my_pollfd.fd = sock;
  my_pollfd.events = POLLIN | POLLPRI;
  result = poll(&my_pollfd, 1, timeout);

  if (SOCKET_ERROR == result || 0 == result){
  	goto cleanup;
  }  
  result = recvfrom(sock, (char*)recv_buf,
            PACKET_SIZE, 0,
            (struct sockaddr*)&source, (unsigned int *)&sockadd_size);

  if (SOCKET_ERROR == result){
  	goto cleanup;
  }  
			    
  unsigned short header_len = recv_buf->ip_hl << 2;
  struct icmp* icmphdr = (struct icmp*)((char*)recv_buf + header_len);
  if ((result < header_len + ICMP_SIZE)||
	(icmphdr->icmp_type != ICMP_ECHO_REPLY)||
	(icmphdr->icmp_id != getpid())) {	
	if (!(icmphdr->icmp_type == ICMP_ECHO_REQUEST && icmphdr->icmp_seq == 0))
		goto cleanup;
  }
  ret = REACHABLE;
cleanup:

  if (send_buf != NULL) {
      free(send_buf);
  }

  if (recv_buf != NULL) {
      free(recv_buf);
  }

  return ret;
}

// typical ip checksum
unsigned short ip_checksum(unsigned short* buffer, int size)
{
	register unsigned short * buf = buffer;
    register int bufleft = size;
    register unsigned long sum = 0;
    
    while (bufleft > 1) {
        sum = sum + (*buf++);
        bufleft = bufleft - sizeof(unsigned short );
    }
    if (bufleft) {
        sum = sum + (*(unsigned char*)buf);
    }
    sum = (sum >> 16) + (sum & 0xffff);
    sum += (sum >> 16);
   
    return (unsigned short )(~sum);
}

void set_icmp_packet(struct icmp* icmp_hdr, int packet_size)
{
    icmp_hdr->icmp_type = ICMP_ECHO_REQUEST;
    icmp_hdr->icmp_code = 0;
    icmp_hdr->icmp_cksum = 0;
    icmp_hdr->icmp_id = getpid();
    icmp_hdr->icmp_seq = 0;

    // Calculate a checksum on the result
    icmp_hdr->icmp_cksum = ip_checksum((unsigned short*)icmp_hdr, packet_size);
}

/*
 * Class:     org_apache_harmony_luni_platform_OSNetworkSystem
 * Method:    selectImpl
 * Signature: ([Ljava/io/FileDescriptor;[Ljava/io/FileDescriptor;II[IJ)I
 */
JNIEXPORT jint JNICALL Java_org_apache_harmony_luni_platform_OSNetworkSystem_selectImpl	
  (JNIEnv * env, jclass	thisClz, jobjectArray readFDArray, jobjectArray	writeFDArray,
   jint	countReadC, jint countWriteC, jintArray	outFlags, jlong	timeout){
  PORT_ACCESS_FROM_ENV (env);
  I_32 result =	0;		
  hysocket_t hysocketP;		
  jboolean isCopy ;
  jint *flagArray;
  int val;
  struct pollfd * my_pollfds;
  int n_pollfds;
  jobject gotFD;

  n_pollfds = countReadC + countWriteC;

  my_pollfds = hymem_allocate_memory(sizeof(struct pollfd) * n_pollfds);
  if (my_pollfds == NULL) {
      return HYPORT_ERROR_SYSTEMFULL;
  }

  for (val=0; val<countReadC; val++) {
	  gotFD	= (*env)->GetObjectArrayElement(env, readFDArray, val);
	  hysocketP = getJavaIoFileDescriptorContentsAsAPointer	(env, gotFD);

      my_pollfds[val].fd = hysocketP->sock;
      my_pollfds[val].events = POLLIN | POLLPRI;
      my_pollfds[val].revents = 0;
  }

  for (val=0; val<countWriteC; val++) {
	  gotFD	= (*env)->GetObjectArrayElement(env, writeFDArray, val);
	  hysocketP = getJavaIoFileDescriptorContentsAsAPointer	(env, gotFD);

      my_pollfds[countReadC + val].fd = hysocketP->sock;
      my_pollfds[countReadC + val].events = POLLOUT;
      my_pollfds[countReadC + val].revents = 0;
  }

  result = poll(my_pollfds, n_pollfds, timeout);

  if (result > 0) {
	  /* output result to int array */
	  flagArray = (*env)->GetIntArrayElements(env,outFlags,	&isCopy);
	  for (val=0; val<countReadC; val++) {
          if (my_pollfds[val].revents & (POLLIN | POLLPRI)) {
              flagArray[val] = SOCKET_OP_READ;
          } else {
              flagArray[val] = SOCKET_OP_NONE;
          }
      }

	  for (val=0; val<countWriteC; val++) {
          if (my_pollfds[val+countReadC].revents & POLLOUT) {
              flagArray[val+countReadC] = SOCKET_OP_WRITE;
          } else {
              flagArray[val+countReadC] = SOCKET_OP_NONE;
          }
      }
      (*env)->ReleaseIntArrayElements(env, outFlags, flagArray, 0);
  }
  hymem_free_memory(my_pollfds);
  
  /* return both correct and error result, let java code handle	exceptions */
  return result;
};

JNIEXPORT jobject JNICALL Java_org_apache_harmony_luni_platform_OSNetworkSystem_inheritedChannelImpl
  (JNIEnv * env , jobject clz){
    int socket = 0;
    int opt;
    int length = sizeof(opt);
    int socket_type;
    struct sockaddr_in local_addr;
    struct sockaddr_in remote_addr;
    jclass channel_class, socketaddr_class,serverSocket_class,socketImpl_class;
    jobject channel_object = NULL, socketaddr_object, serverSocket_object,socketImpl_object;
	jfieldID port_field, socketaddr_field, bound_field;
	jfieldID serverSocket_field,socketImpl_field;
	hysocket_t sock;
	jbyte * address;
	jbyte * localAddr;
	jboolean jtrue = TRUE;

	if(0 != getsockopt(socket,SOL_SOCKET,SO_TYPE,&opt,(unsigned int *)&length)){
		return NULL;
	}
	if(SOCK_STREAM !=opt && SOCK_DGRAM !=opt){
		return NULL;
	}
	socket_type = opt;

	length  = sizeof(struct sockaddr);
	if(0 != getsockname(socket,(struct sockaddr *)&local_addr,(unsigned int *)&length)){
		return NULL;
	} else {
		if(AF_INET != local_addr.sin_family || length != sizeof(struct sockaddr)){
			return NULL;
		}
		localAddr = malloc(sizeof(jbyte)*4);
		if (NULL == localAddr){
			return NULL;
		}
		memcpy (localAddr, &(local_addr.sin_addr.s_addr), 4); 
	}
	if(0 != getpeername(socket,(struct sockaddr *)&remote_addr,(unsigned int *)&length))	{
		remote_addr.sin_port = 0;
                remote_addr.sin_addr.s_addr = 0;
		address = malloc(sizeof(jbyte)*4);
		bzero(address,sizeof(jbyte)*4);
	} else {
		if(AF_INET != remote_addr.sin_family || length != sizeof(struct sockaddr))	{
			return NULL;
		}
		address = malloc(sizeof(jbyte)*4);
		memcpy (address, &(remote_addr.sin_addr.s_addr), 4);
	}
	sock = malloc(sizeof(hysocket_struct));
	sock->sock = socket;
	sock->family = AF_INET;

	// analysis end, begin pack to java
        if(SOCK_STREAM == opt)
        {	  
	  if(remote_addr.sin_port!=0){
		//socket
		channel_class = (*env)->FindClass(env,"org/apache/harmony/nio/internal/SocketChannelImpl");
        	if(NULL == channel_class) {
        	    goto clean;
	        }
		channel_object = getJavaNioChannelsSocketChannelImplObj(env,channel_class);
          	if(NULL == channel_object) {
	            goto clean;
        	}
		// new and set FileDescript
		setFDContent(env, channel_class, channel_object,&sock);
		// local port
		setJavaNioChannelsLocalPort(env,channel_class,channel_object,ntohs(local_addr.sin_port));
	  	// new and set remote addr
		setSocketAddressContent(env, channel_class,channel_object,address);
		// localAddr
		socketaddr_class = (*env)->FindClass(env,"java/net/InetSocketAddress");
		socketaddr_field = (*env)->GetFieldID(env,channel_class,"connectAddress","Ljava/net/InetSocketAddress;");
		socketaddr_object = (*env)->GetObjectField(env,channel_object,socketaddr_field);
		setSocketLocalAddressContent(env,channel_class,channel_object,localAddr);
		// set port
		port_field = (*env)->GetFieldID(env,socketaddr_class,"port","I");
		(*env)->SetIntField(env,socketaddr_object, port_field, ntohs(remote_addr.sin_port));
		// set bound
		if (0 != local_addr.sin_port){
			bound_field = (*env)->GetFieldID(env,channel_class,"isBound","Z");
			(*env)->SetBooleanField(env,channel_object, bound_field,jtrue);
		}
	  } else {
		//serverSocket	
		channel_class = (*env)->FindClass(env,"org/apache/harmony/nio/internal/ServerSocketChannelImpl");
        	if(NULL == channel_class) {
        	    goto clean;
	        }
		channel_object = getJavaNioChannelsSocketChannelImplObj(env,channel_class);
          	if(NULL == channel_object) {
	            goto clean;
        	}
		serverSocket_field = (*env)->GetFieldID(env,channel_class,"socket","Ljava/net/ServerSocket;");	
		serverSocket_class = (*env)->FindClass(env,"Ljava/net/ServerSocket;");
		serverSocket_object = (*env)->GetObjectField(env,channel_object,serverSocket_field);	
		// set bound
		if (0 != local_addr.sin_port){		
			bound_field = (*env)->GetFieldID(env,channel_class,"isBound","Z");
			(*env)->SetBooleanField(env,channel_object, bound_field,jtrue);			
			bound_field = (*env)->GetFieldID(env,serverSocket_class,"isBound","Z");
			(*env)->SetBooleanField(env,serverSocket_object, bound_field,jtrue);
		}
		// localAddr
		socketImpl_class = (*env)->FindClass(env,"java/net/SocketImpl");
		socketImpl_field = (*env)->GetFieldID(env,channel_class,"impl","Ljava/net/SocketImpl;");
		socketImpl_object =  (*env)->GetObjectField(env,channel_object,socketImpl_field);
		if(NULL == socketImpl_object) {
	             goto clean;
        	}
		setServerSocketLocalAddressContent(env, socketImpl_class, socketImpl_object, localAddr);
		// set port
		port_field = (*env)->GetFieldID(env,socketImpl_class,"localport","I");
		(*env)->SetIntField(env,socketImpl_object, port_field, ntohs(local_addr.sin_port));
	  }	
        }
        else
        {
          //Datagram Socket
	  // new DatagramChannel
	  channel_class = (*env)->FindClass(env,"org/apache/harmony/nio/internal/DatagramChannelImpl");
          if(NULL == channel_class) {
              goto clean;
          }
          channel_object = getJavaNioChannelsSocketChannelImplObj(env,channel_class);
          if(NULL == channel_object) {
              goto clean;
          }
	  // new and set FileDescript
	  setFDContent(env,channel_class,channel_object,&sock);
	  setJavaNioChannelsLocalPort(env,channel_class,channel_object,ntohs(local_addr.sin_port));
	  // new and set remote addr
	  setSocketAddressContent(env, channel_class,channel_object,address);
	  // set bound
	  if (0 != local_addr.sin_port){		
		bound_field = (*env)->GetFieldID(env,channel_class,"isBound","Z");
		(*env)->SetBooleanField(env,channel_object, bound_field,jtrue);			
	  }
        }	
clean:
	free(address);
	free(localAddr);
	return channel_object;
}

