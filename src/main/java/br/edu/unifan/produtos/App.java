package br.edu.unifan.produtos;

import br.edu.unifan.produtos.exception.PersistenciaException;
import br.edu.unifan.produtos.report.RelatorioService;
import br.edu.unifan.produtos.repository.CsvProdutoRepository;
import br.edu.unifan.produtos.service.ProdutoService;
import br.edu.unifan.produtos.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.nio.file.Path;

/** Ponto de entrada da aplicação JavaFX. */
public class App extends Application {

    /** Arquivo de dados, relativo à pasta de onde o programa é executado (a raiz do projeto). */
    private static final Path ARQUIVO_CSV = Path.of("data", "produtos.csv");

    @Override
    public void start(Stage stage) throws Exception {
        ProdutoService service;
        try {
            service = new ProdutoService(new CsvProdutoRepository(ARQUIVO_CSV));
        } catch (PersistenciaException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível carregar os produtos");
            alerta.showAndWait();
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(App.class.getResource("ui/main.fxml"));
        loader.setControllerFactory(tipo -> new MainController(service, new RelatorioService()));

        stage.setTitle("Gestão de Produtos - UNIFAN");
        stage.setScene(new Scene(loader.load()));
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
