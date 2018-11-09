# DiagnosticsServer
Example of FTP server for MySQL database update
  
This code simulate a diagnostics server that listen data from users and store them updating a database . 
Data is sent and received as Record objects (extends JSONObject) through the socket (InputStream/OutputStream).  
In this case the data consists in two IDs and a timestamp.  
The couple of IDs are stored in the Database, counting how many times the couple occurs;  

The connection parameters are read from file config/diagnostics.conf (not included).  
The format is the following:  
  DB_URL : \<database host\>  
  DB_NAME : \<database name\>  
  DB_USER : \<database user\>  
  DB_PASSWD : \<database user password\>  
  PORT: <listening port>  
    
Is included a Test class that acts like a client (you need to set the same port and the server ip/domain  
