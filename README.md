# JavaChatApp-Jarkom_KOMA
Final Project Socket Programming Jaringan Komputer KOM A Universitas Gadjah Mada.

## Anggota Kelompok
**1. Andrian Danar Perdana (23/513040/PA/21917)**<br/>
**2. Andreandhiki Riyanta Putra (23/517511/PA/22191)**<br/>
**3. Daffa Indra Wibowo (23/518514/PA/22253)**<br/>
**4. Rayhan Firdaus Ardian (23/519095/PA/22279)**<br/>
**5. Muhammad Argya Vityasy (23/522547/PA/22475)**<br/>

## Requirements
- Java Development Kit (JDK) version 8 or higher
- A code editor or IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code) or a terminal to compile and run the code
- Properly set `JAVA_HOME` environment variable (for running `javac` and `java` commands)

## Steps to Run
1. **Compile the Server and Client Code**  
   Make sure your `Server.java` and `Client.java` files are in the same directory.  
   Open a terminal, navigate to the directory where the files are located, and compile the code:  
   ```bash
   javac Server.java Client.java
2. **Run the server** <br/>
   Run with the default port (6942)
   ```java Server```
   OR run with a custom port
   ```java Server <port_number>```
3. **Open a new terminal** <br/>
   ```java Client <server_ip> <port_number>```
4. **Run the Client** <br/>
   ```java Client <server_ip> <port_number> or java Client``` 

## Commit message
<**header_commit**>[**deskripsi_singkat**]: <**commit_message**> <br/> <br/>
**header_commit** = feat (ini kalo nambahin fitur), fix (dipakai pas benerin sesuatu), init (initialization), chore (benerin hal kecil kaya readme.md dsb), dan deskripsi lain sesuai kebutuhan <br/> <br/>
**deskripsi_singkat** = client, server, dsb <br/> <br/>
**commit_message** = untuk mendeskripsikan apa yang akan dicommit <br/> <br/>
**Contoh commit message** -> feat[server]: menambahkan fitur baru ke Server.java <br/>
