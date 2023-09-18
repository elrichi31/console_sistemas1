import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Consola {

    private File currentDirectory = new File(System.getProperty("user.dir"));
    private String textoTerminal = currentDirectory.getAbsolutePath() + " : ";
    private final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private final List<String> commandHistory = new ArrayList<>();


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

                // Check if it's !#
                if (comando.equals("!#")) {
                    if (!commandHistory.isEmpty()) {
                        comando = commandHistory.get(commandHistory.size() - 1);
                    } else {
                        System.out.println("Error: No hay comandos en el historial.");
                        continue;
                    }
                } else if (!"history".equalsIgnoreCase(comando) && !comando.startsWith("!")) {
                    commandHistory.add(comando);
                }

                mensageSalida(comando);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }


    public void mensageSalida(final String cadena) {
        try {
            String[] comandos = cadena.split("@");

            for (String comando : comandos) {
                comando = comando.trim();

                // If it's a replay command, replace it with the appropriate command from the history
                if (comando.equals("!#") && !commandHistory.isEmpty()) {
                    comando = commandHistory.get(commandHistory.size() - 1);
                } else if (comando.startsWith("!")) {
                    try {
                        int index = Integer.parseInt(comando.substring(1)) - 1;
                        if (index >= 0 && index < commandHistory.size()) {
                            comando = commandHistory.get(index);
                            // Check if the extracted command contains '@' and recursively process it
                            if (comando.contains("@")) {
                                mensageSalida(comando);
                                continue;
                            }
                        } else {
                            System.out.println("Error: Índice fuera de rango.");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Comando desconocido.");
                        continue;
                    }
                }

                executeCommand(comando);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void executeCommand(String comando) throws IOException, InterruptedException {
        // History command
        if (comando.equalsIgnoreCase("history")) {
            for (int i = 0; i < commandHistory.size(); i++) {
                System.out.println((i + 1) + ". " + commandHistory.get(i));
            }
            return;
        }

        // Clear screen command
        if (comando.equals("cls") || comando.equals("clear")) {
            if (isWindows) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
            return;
        }

        // Change directory command
        if (comando.startsWith("cd ")) {
            String newDir = comando.substring(3).trim();
            File newDirectory = new File(currentDirectory, newDir);
            if (newDirectory.exists() && newDirectory.isDirectory()) {
                currentDirectory = newDirectory.getCanonicalFile();
                textoTerminal = currentDirectory.getAbsolutePath() + " : ";
                return;
            } else {
                System.out.println("Error: el directorio no existe.");
                return;
            }
        }

        ProcessBuilder pb;
        if (isWindows) {
            pb = new ProcessBuilder("cmd", "/c", comando);
        } else {
            pb = new ProcessBuilder("/bin/sh", "-c", comando);
        }
        pb.directory(currentDirectory);

        Process p = pb.start();
        InputStream salida = p.getInputStream();
        BufferedReader leer = new BufferedReader(new InputStreamReader(salida));
        String lectura;
        while ((lectura = leer.readLine()) != null) {
            System.out.println(lectura);
        }
    }
}
