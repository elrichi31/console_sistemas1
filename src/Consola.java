import java.io.*;

public class Consola {

    private File currentDirectory = new File(System.getProperty("user.dir"));
    private String textoTerminal = currentDirectory.getAbsolutePath() + " : ";
    private final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static void main(String[] args) {
        Consola consola = new Consola();
        consola.run();
    }

    public void run() {
        System.out.println("Bienvenido a la consola. Escriba 'exit' para salir.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                System.out.print(textoTerminal);
                String comando = reader.readLine();

                if ("exit".equalsIgnoreCase(comando)) {
                    break;
                }

                mensageSalida(comando);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void mensageSalida(final String cadena) {
        try {
            if (cadena.equals("cls") || cadena.equals("clear")) {
                if (isWindows) {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
                return;
            }

            if (cadena.startsWith("cd ")) {
                String newDir = cadena.substring(3).trim();
                File newDirectory = new File(currentDirectory, newDir);
                if (newDirectory.exists() && newDirectory.isDirectory()) {
                    currentDirectory = newDirectory;
                    textoTerminal = currentDirectory.getAbsolutePath() + " : ";
                    return;
                } else {
                    System.out.println("Error: el directorio no existe.");
                    return;
                }
            }

            ProcessBuilder pb;
            if (isWindows) {
                pb = new ProcessBuilder("cmd", "/c", cadena);
            } else {
                pb = new ProcessBuilder("/bin/sh", "-c", cadena);
            }
            pb.directory(currentDirectory);

            Process p = pb.start();
            InputStream salida = p.getInputStream();
            BufferedReader leer = new BufferedReader(new InputStreamReader(salida));
            String lectura;
            while ((lectura = leer.readLine()) != null) {
                System.out.println(lectura);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
