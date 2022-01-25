//Austin Hansen
//1001530325

//this code is based on the text book, version 1, 4 and 6
//https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
//https://www.cs.bu.edu/fac/matta/Teaching/CS552/F99/proj4/
//https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
//https://www.tutorialspoint.com/How-to-convert-Image-to-Byte-Array-in-java
//https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers
//https://www.infoworld.com/article/2853780/socket-programming-for-scalable-systems.html

import java.util.*;
import java.io.*;
import java.net.*;
import java.time.*;

//our basic server
public class TCPServer{
	public static void main(String[] args) throws IOException
	{
        //Assign a port number, lets use the book's
		int portnum = 1200;
        //continuing with the book, lets make a welcome Socket in line with TCP
		ServerSocket  welcomeSocket = new ServerSocket(portnum);
		System.out.println("The Server is Open");
		//listen for a knock from a client, this loop is only for listening
		//and adding new connections
        while(true){

        	//if we recieve a knock, add a socket
        	Socket connectionSocket=welcomeSocket.accept();
        	//make a thread
        	new HandleRequest( connectionSocket ).start();
      
        }

	}
}
class HandleRequest extends Thread
{
	private Socket socket;
	//constructor for the thread
	public HandleRequest(Socket socket)
	{
		this.socket=socket;
	}
    //overiding method
	public void run()
	{
		String clientMessage; //request from the client
		String fName; //file we are attempting to access
		String filetype; //our MIME type of file
		int limitnum; //

		fName=" ";
		filetype=" ";

		limitnum=0;
		try{
        	//get message from Client
        	BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	//set up a way to send message to Client
        	DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
        	//read the recieved client sentence
        	clientMessage=fromClient.readLine();
        	//tokenize the message
        	StringTokenizer tokenString = new StringTokenizer (clientMessage);
        	System.out.println(clientMessage);
        	//if our Strings first token is "GET" then do "GET" request
        	if(tokenString.nextToken().equals("GET"))
        	{
        			//token[1] for file name
        			fName = tokenString.nextToken();
        			//we know it is what we're looking for if it starts with /
        			if(fName.startsWith("/"))
        			{
        				
        				//we need to check the MIME type of the file, so we can send it in the header later
        				filetype=MIMEType(fName);
        				
        				//add a . to the start to work in our cwd
        				fName= "./"+fName.substring(1);
        				
        			}
        			//make a file with our file name, we can also use this for checking if it even exists
        			File file = new File(fName);
        		
        				//code 301, if the request is test (firefox made testing this hard), then test is said to have moved to index
        			
        				if(fName.contains("test")&&!fName.equals("index.html"))
        				{
        					//begin our header for Code 301
        					toClient.writeBytes("HTTP/1.1 301 \r\n");
        					toClient.writeBytes("Location: /Error303.html\r\n");
        					//show on the terminl that we are in this portion of the code
        					System.out.println("Error 301");
        						//this part isn't technically necessary, but it helps prevent an exception
        					    fName="Error303.html";
        						//this is bad practice, but quick
        						File file2 = new File(fName);
        						file=file2;
        					
        				}
        				else
        				{
        					//if the file doesn't exist we have error 404 and we haven't had an error 301, this also starts our header for a 
        					//404 case
        					if(!file.exists())
        					{
        						//tell the terminal that we had a 404 error and am in this portion of the code
        						System.out.println("Error404");
        						//begin writing our header for the code 404
        						toClient.writeBytes("HTTP/1.1 404 Not Found\r\n");

        						fName="Error404.html";
        						//this is bad practice, but quick
        						File file2 = new File(fName);
        						file=file2;
        					}
        					else
        					{
        						//since we didn't encounter the other two errors, we're good
        					   toClient.writeBytes("HTTP/1.1 200 \r\n");


        					}
        					
        				}
        			//I wanted to somewhat generalize how the header was written, so that It would be easy to confirm if things went right or wrong
        			//we first get the actual size of our file
        			int fileSize = (int) file.length();
        			//we open a file input stream, I misspelled the abreviation, but this is to put our file stuff in
        			FileInputStream iFS = new FileInputStream (fName);
        			//make a byte array of size fileSize, this is to transfer over our socket, its name is also a pun
        			byte[] fileByteSize = new byte[fileSize];
        			//read and put our new byte array into the input stream
        			iFS.read(fileByteSize);
					
        			//build rest of header
        			toClient.writeBytes("localhost:1200\r\n");
        			toClient.writeBytes("connection: keep-alive\r\n");
        			toClient.writeBytes("Content-Length: "+ fileSize+"\r\n");
        			toClient.writeBytes("Content-Type: "+ MIMEType(fName)+"\r\n");
        			toClient.writeBytes("\r\n");
        			//send what we specifically were looking for to the Client, in our case the webbrowser, couldn't get chunking to work correctly
        			toClient.write(fileByteSize, 0, fileSize);
        			toClient.flush();
        			
        	}
        	else
        	{
        		//if this happens, something bad happened,as this is out of the scope of this assignment
        		System.out.println("Not implemented");
        	}

        	//close our socket
			socket.close();
		}
		catch(IOException e)
		{
            System.out.println(e.toString());
		}
	}
	//parameter fName: String, name of our file
	public String MIMEType(String fName)
	{
		//if there is no .type, this returns -1, thus avoiding an exception
       int index=fName.lastIndexOf('.');
       	//avoid an exception and return what most unknown file types are: "application/octet-stream"
 		if(index!=-1){
 		//give th MIME type,https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers was very useful here
		switch(fName.substring(index))
		{
			case ".html":
						return "text/html";
			case ".txt":
						return "text/html";
			case ".PNG":
						return "image/PNG";
			default:
						return "application/octet-stream";
		}
	  }
	  else{
	  	return "application/octet-stream";
	  }
	}

}