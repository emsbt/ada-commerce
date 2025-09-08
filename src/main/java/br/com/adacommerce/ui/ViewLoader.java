package br.com.adacommerce.ui;

import br.com.adacommerce.controller.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewLoader {

    public static class ViewTuple {
        private final Parent view;
        private final BaseController controller;
        public ViewTuple(Parent view, BaseController controller) {
            this.view = view;
            this.controller = controller;
        }
        public Parent getView() { return view; }
        public BaseController getController() { return controller; }
    }

    private static final Map<String, ViewTuple> CACHE = new HashMap<>();

    public static ViewTuple load(String resourcePath) throws IOException {
        if (CACHE.containsKey(resourcePath)) {
            return CACHE.get(resourcePath);
        }
        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(resourcePath));
        Parent parent = loader.load();
        BaseController controller = (BaseController) loader.getController();
        ViewTuple tuple = new ViewTuple(parent, controller);
        CACHE.put(resourcePath, tuple);
        return tuple;
    }
}