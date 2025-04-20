package nmng108.microtube.processor.util.constant;

public interface Routes {
    interface Auth {
        String basePath = "/auth";
        String login = Auth.basePath + "/login";
        String logout = Auth.basePath + "/logout";
        String user = Auth.basePath + "/user";
        String signup = Auth.basePath + "/signup";
        String forgot = Auth.basePath + "/forgot";
    }

    String users = "/users";
}
