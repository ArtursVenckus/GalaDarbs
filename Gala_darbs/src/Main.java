import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        //Palaist aplikaciju
        SwingUtilities.invokeLater(() -> {
            GUI app= new GUI();

            app.setVisible(true);
        });

    }
}


