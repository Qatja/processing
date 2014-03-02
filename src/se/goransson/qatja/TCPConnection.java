package se.goransson.qatja;

/*
 * Copyright (C) 2012 Andreas Goransson, David Cuartielles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Defines a TCP connection
 * 
 * @author andreas
 * 
 */
public class TCPConnection extends Connection {

	private Socket mSocket;

	public TCPConnection(InetAddress addr, int port) throws IOException {
		mSocket = new Socket(addr, port);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return mSocket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return mSocket.getOutputStream();
	}

	@Override
	public void close() throws IOException {
		mSocket.close();
	}

}
