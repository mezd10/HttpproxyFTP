import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ProxyRequestHandler implements Runnable {


    private Scanner proxyToClientScanner;

    private PrintWriter proxyToClientWriter;



    private String user, password;
    private Logger logger = new Logger("Logger.txt");
    private String postBody, authorization;

    public ProxyRequestHandler(Socket clientSocket){

        try{

            proxyToClientScanner = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
            proxyToClientWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        String requestString;
        requestString = proxyToClientScanner.nextLine();
        try {
            parseUrl(requestString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseUrl(String request) throws IOException {
        System.out.println(request);
        logger.setLog("HTTP", request);
        String header = request.substring(0,request.indexOf(' '));
        String ftprequest = request.substring(request.indexOf(' ')+2).split(" ")[0];
        goToAuthString(header);
        if (header.equals("GET")) {
            getRequest(authorization, ftprequest);
        }
        else {
            postRequest(authorization, ftprequest, postBody);
        }

    }

    private void getRequest(String authorization, String ftprequest) throws IOException {
        System.out.println(authorization);
        String[] array = authorization.split(" ");
        String[] arrayftp = ftprequest.split("/");
        String ftpHost = arrayftp[0];
        String command = arrayftp[1];
        FtpClient ftpClient = new FtpClient(ftpHost);
        if (array.length == 3) {
            user = array[1];
            ftpClient.setUser(user);
            password = array[2];
            ftpClient.setPassword(password);
            if (ftpClient.authentication()) {
                if (command.equals("list")) {
                    ArrayList<String> arrayList = ftpClient.getListAllFile();
                    ftpClient.quit();
                    logger.setLog("FTP", ftpClient.correctList());
                    String response = "HTTP/1.0 200 OK\n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientWriter.write(response);
                    for (int i = 0; i < arrayList.size(); i++) {
                        proxyToClientWriter.write(arrayList.get(i) + "\n");
                    }
                    proxyToClientWriter.close();
                }
                else if (command.substring(0,8).equals("download")) {
                    String fileName = command.split("=")[1];
                    String responseFtp = ftpClient.getCurrentFile(fileName);
                    ftpClient.quit();
                    logger.setLog("FTP", responseFtp);

                    File file = new File(fileName);
                    String response = "HTTP/1.0 200 OK\n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientWriter.write(response);

                    Scanner sc = new Scanner(file);

                    while (sc.hasNextLine()) {

                        proxyToClientWriter.write(sc.nextLine() + "\n");
                    }
                    sc.close();
                    proxyToClientWriter.close();

                }
            }else {
                String responseHTTP = "HTTP/1.0 401 Unauthorized \n" +
                        "Proxy-agent: ProxyServer/1.0\n" +
                        "\r\n";
                proxyToClientWriter.write(responseHTTP);
                proxyToClientWriter.write("Error FTP SERVER : " + ftpClient.incorrectLogin());
                proxyToClientWriter.close();
                logger.setLog("FTP", ftpClient.incorrectLogin());
            }
        }
        else {
            String responseHTTP = "HTTP/1.0 401 Unauthorized \n" +
                    "Proxy-agent: ProxyServer/1.0\n" +
                    "\r\n";
            proxyToClientWriter.write(responseHTTP);
            proxyToClientWriter.write("Error Field User or Password");
            proxyToClientWriter.close();
        }
    }

    private void postRequest(String authorization, String ftpRequest, String postBody) {

        String[] array = authorization.split(" ");
        String[] arrayftp = ftpRequest.split("/");
        String ftpHost = arrayftp[0];
        String[] post = postBody.split(" ");
        String fileName = post[2];
        if (array.length == 3) {
            FtpClient ftpClient = new FtpClient(ftpHost);
            user = array[1];
            password = array[2];
            ftpClient.setUser(user);
            ftpClient.setPassword(password);

            if (ftpClient.authentication()) {
                String response = ftpClient.loadFileToServer(fileName);
                ftpClient.quit();
                logger.setLog("FTP", response);
                int code = Integer.parseInt(response.substring(0,3));
                if (code == 226) {
                    String responseHTTP = "HTTP/1.0 200 OK\n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientWriter.write(responseHTTP);
                    proxyToClientWriter.write("File Success Download");
                    proxyToClientWriter.close();
                }
                else {
                    String responseHTTP = "HTTP/1.0 404 NOT FOUND \n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientWriter.write(responseHTTP);
                    proxyToClientWriter.write("Error FTP SERVER : " + response);
                    proxyToClientWriter.close();
                }
            } else {
                String responseHTTP = "HTTP/1.0 401 Unauthorized \n" +
                        "Proxy-agent: ProxyServer/1.0\n" +
                        "\r\n";
                proxyToClientWriter.write(responseHTTP);
                proxyToClientWriter.write("Error FTP SERVER : " + ftpClient.incorrectLogin());
                proxyToClientWriter.close();
                logger.setLog("FTP", ftpClient.incorrectLogin());
            }
        }
    }



    private void goToAuthString(String type) {
        if (type.equals("GET")) {
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            authorization = proxyToClientScanner.nextLine();


        }
        else {
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            authorization = proxyToClientScanner.nextLine();

            System.out.println(proxyToClientScanner.nextLine());
            //
            System.out.println(proxyToClientScanner.nextLine());

            System.out.println(authorization);
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            System.out.println(proxyToClientScanner.nextLine());
            postBody = proxyToClientScanner.nextLine();
            System.out.println(postBody);
        }
    }
}
