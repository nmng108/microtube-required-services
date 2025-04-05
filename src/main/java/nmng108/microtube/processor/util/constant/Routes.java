package nmng108.microtube.processor.util.constant;

public interface Routes {
    interface Auth {
        String basePath = "/auth";
        String login = Auth.basePath + "/login";
        String user = Auth.basePath + "/user";
        String register = Auth.basePath + "/register";
        String forgot = Auth.basePath + "/forgot";
    }

    String users = "/users";
}
