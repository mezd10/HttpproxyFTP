import java.util.Scanner;

public class Main {

    private static boolean running = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String comand;
        Proxy myProxy = new Proxy(8080);

        while (running) {
            myProxy.listen();
            comand = scanner.nextLine();
            if (comand.equals("close")) {
                running = false;
            }
        }
    }
}
