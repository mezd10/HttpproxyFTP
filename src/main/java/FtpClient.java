import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FtpClient {

    private String server, user, password;
    private Scanner externalScanner;
    private PrintWriter externalWriter;
    Socket socket;

    private int counter = 0;
    private ArrayList<String> allFiles;
    private String stringList = "";
    private String inccorectLogin = "";




    public FtpClient(String server) {
        this.server = server;
        connect(this.server);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean authentication() {
        login();
        return checkCorrectLogin();
    }

    public ArrayList<String> getListAllFile() {
        if (responseCode(getList()) == 226) {
            return allFiles;
        }
        else return null;
    }

    public String correctList() {
        return stringList;
    }

    public String getCurrentFile(String file)  {
        return getFile(file);
    }

    public String loadFileToServer(String fileName) {
        return sendFile(fileName);
    }

    public String incorrectLogin() {
        return inccorectLogin;
    }

    public void quit() {
        externalWriter.println("QUIT");
    }


    private void connect(String server) {
        try {
            socket = new Socket(server, 21);
            externalScanner = new Scanner(socket.getInputStream());
            externalWriter = new PrintWriter(socket.getOutputStream(), true);
            counter += 3;

        }catch (ConnectException e){
            System.out.println("Connect exception");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //change list of files or Exception String
    private String getList() {
        allFiles = new ArrayList();
        String response = "";
        try {

            int dataPort = pasv();
            Socket dataSocket = new Socket(server, dataPort);
            InputStream dataIs = dataSocket.getInputStream();
            Scanner dataScanner = new Scanner(dataIs);

            externalWriter.println("LIST");
            counter++;
            response = externalScanner.nextLine();
            //arrayListener.add(currentString);
            int code = Integer.parseInt(response.substring(0, 3));
            if (code == 150) {
                while (dataScanner.hasNext()) {
                    String fileName = dataScanner.nextLine();
                    //System.out.println(fileName);
                    allFiles.add(fileName);
                }
                counter++;
                response = externalScanner.nextLine();
                // arrayListener.add(externalScanner.nextLine());
            }

        }catch (IOException e) {
            System.out.println("There was a problem:" + e);
            e.printStackTrace();
        }
        stringList = response;
        return response;
    }

    private String checkList(String response) {
        return response;
    }

    //change code
    private String getFile(String fileName) {
        int code = 0;
        String response = "";
        try {

            int dataPort = pasv();
            Socket dataSocket = new Socket(server, dataPort);
            InputStream dataIs = dataSocket.getInputStream();
            Scanner dataScanner = new Scanner(dataIs);

            File outFile = new File(fileName);


            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();

            externalWriter.println("RETR " + fileName);
            counter++;
            response = externalScanner.nextLine();

            code = responseCode(response);
            if (code == 150) {
                FileWriter fileWriter = new FileWriter(outFile, true);
                while (dataScanner.hasNext()) {
                    String line = dataScanner.nextLine();
                    fileWriter.write(line + '\n');
                }
                fileWriter.close();
                System.out.println("Received file " + fileName);
                counter++;
                response = externalScanner.nextLine();
            }

        } catch (IOException e) {
            System.out.println("There was a problem:" + e);
            e.printStackTrace();
        }
        return response;
    }

    private String sendFile(String fileName) {
        File file = new File(fileName);
        DataInputStream dis;
        String responseCode = "";
        byte[] data = new byte[0x8000];
        String commandName = parseNameForDownload(fileName);
        try {
            int dataPort = pasv();
            Socket dataSocket = new Socket(server, dataPort);
            DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());


            FileInputStream fis = new FileInputStream(file);

            dis = new DataInputStream(fis);
            externalWriter.println("STOR " + commandName);
            responseCode = externalScanner.nextLine();
            if (responseCode(responseCode) == 150) {
                while (true) {
                    int rcount = dis.read(data);
                    if (rcount <= 0) break;
                    dataOut.write(data, 0, rcount);

                }
                dis.close();
                dataOut.close();
                responseCode = externalScanner.nextLine();
            }

        }catch (IOException e) {
            System.out.println("There was a problem:" + e);
            e.printStackTrace();
        }
        return responseCode;
    }


    private int responseCode(String response) {
        return Integer.parseInt(response.substring(0, 3));
    }

    private int pasv() {
        externalWriter.println("PASV");
        counter ++;
        String currentString = externalScanner.nextLine();
        // arrayListener.add(currentString);
        List<String> arrayList = Arrays.asList(currentString.split(" "));
        String s = arrayList.get(arrayList.size() - 1);
        arrayList = Arrays.asList(s.split(","));
        int port2 = Integer.parseInt(arrayList.get(arrayList.size() - 2)) * 256;
        s = arrayList.get(arrayList.size() - 1).substring(0, arrayList.get(arrayList.size() - 1).length() - 1);
        int port3 = Integer.parseInt(s);
        return port2 + port3;
    }

    private String getNeedServerString() {
        int k = 0;
        String currentString = "";
        while (k != counter - 1) {
            currentString = externalScanner.nextLine();
            k++;
        }
        return externalScanner.nextLine();
    }

    private void login() {
        externalWriter.println("USER " + user);
        externalWriter.println("PASS " + password);

        counter += 2;

    }

    private String parseNameForDownload(String name) {
        String[] array = name.split("/");
        String response = "";
        for (int i = 0; i < array.length; i ++) {
            response = array[i];
        }
        return response;
    }


    private boolean checkCorrectLogin() {

        String login = getNeedServerString();
        int loginCode = Integer.parseInt(login.substring(0, 3));

        if (loginCode == 230) {
            System.out.println(login);
            return true;
        }
        else {
            System.out.println(login);
            inccorectLogin = login;
            return false;
        }
    }
}

