package br.edu.unifan.produtos;

/**
 * Classe auxiliar para executar pela IDE (IntelliJ, Eclipse, VS Code).
 * Rodar diretamente uma classe que estende Application costuma falhar com
 * "JavaFX runtime components are missing"; passar por esta classe evita o problema.
 */
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}
