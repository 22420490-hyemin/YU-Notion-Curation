import ycuration.view.LoginView;
import ycuration.model.AlertMonitor;

public class Main {
    public static void main(String[] args) {
        AlertMonitor monitor = new AlertMonitor();
        Thread monitorThread = new Thread(monitor);

        monitorThread.setDaemon(true);

        monitorThread.start();

        LoginView loginView = new LoginView();
        loginView.displayLoginWindow();
    }
}