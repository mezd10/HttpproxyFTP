import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private String fileName;
    private FileWriter fileWriter;

    public Logger(String fileName)  {
        this.fileName = fileName;

    }

    public void setLog(String protocol, String loggerString){
        log(protocol, loggerString);
    }

    private void log(String protocol, String loggerString)  {
        try {

        fileWriter = new FileWriter(fileName, true);
        fileWriter.write(protocol + " : " + loggerString + "\n");
        fileWriter.close();
    }catch (IOException e) {
            System.out.println("Problems witch log file");
            e.printStackTrace();
        }

    }

}
